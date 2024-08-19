package org.saudigitus.emis.data.local.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.bindings.dataElement
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.ColorUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.data.model.EMISConfig
import org.saudigitus.emis.data.model.EMISConfigItem
import org.saudigitus.emis.data.model.ProgramStage
import org.saudigitus.emis.data.model.Subject
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.data.model.dto.withBtnSettings
import org.saudigitus.emis.service.RuleEngineRepository
import org.saudigitus.emis.ui.attendance.AttendanceOption
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.utils.Constants
import org.saudigitus.emis.utils.DateHelper
import org.saudigitus.emis.utils.Utils
import org.saudigitus.emis.utils.eventsWithTrackedDataValues
import org.saudigitus.emis.utils.optionByOptionSet
import org.saudigitus.emis.utils.optionsByOptionSetAndCode
import org.saudigitus.emis.utils.optionsNotInOptionGroup
import timber.log.Timber
import java.sql.Date
import javax.inject.Inject

class DataManagerImpl
@Inject constructor(
    val d2: D2,
    val networkUtils: NetworkUtils,
    val ruleEngineRepository: RuleEngineRepository,
) : DataManager {

    private lateinit var currentProgram: String

    private fun getAttributeOptionCombo() =
        d2.categoryModule().categoryOptionCombos()
            .byDisplayName().eq(Constants.DEFAULT).one().blockingGet()?.uid()

    private fun createEventProjection(
        tei: String,
        ou: String,
        program: String,
        programStage: String,
    ): String {
        val enrollment = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(tei)
            .one().blockingGet()

        return d2.eventModule().events()
            .blockingAdd(
                EventCreateProjection.builder()
                    .organisationUnit(ou)
                    .program(program).programStage(programStage)
                    .attributeOptionCombo(getAttributeOptionCombo())
                    .enrollment(enrollment?.uid()).build(),
            )
    }

    private fun eventUid(
        tei: String,
        program: String,
        programStage: String,
        date: String?,
    ): String? {
        return d2.eventModule().events()
            .byTrackedEntityInstanceUids(listOf(tei))
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .byEventDate().eq(Date.valueOf(date.toString()))
            .one().blockingGet()?.uid()
    }

    override suspend fun save(
        ou: String,
        program: String,
        programStage: String,
        attendance: AttendanceEntity,
    ): Unit =
        withContext(Dispatchers.IO) {
            try {
                val uid = eventUid(
                    attendance.tei,
                    program,
                    programStage,
                    attendance.date,
                ) ?: createEventProjection(
                    attendance.tei,
                    ou,
                    program,
                    programStage,
                )

                d2.trackedEntityModule().trackedEntityDataValues()
                    .value(uid, attendance.dataElement)
                    .blockingSet(attendance.value)

                if (attendance.reasonDataElement != null && attendance.reasonOfAbsence != null) {
                    attendance.reasonOfAbsence.let {
                        if (it.isNotEmpty() && it.isNotBlank()) {
                            d2.trackedEntityModule().trackedEntityDataValues()
                                .value(uid, attendance.reasonDataElement).blockingSet(it)
                        }
                    }
                }

                val repository = d2.eventModule().events().uid(uid)
                repository.setEventDate(Date.valueOf(attendance.date))
            } catch (e: Exception) {
                Timber.tag("SAVE_EVENT").e(e)
            }
        }

    override suspend fun getConfig(id: String): List<EMISConfigItem>? =
        withContext(Dispatchers.IO) {
            val dataStore = d2.dataStoreModule()
                .dataStore()
                .byNamespace().eq("semis")
                .byKey().eq(id)
                .one().blockingGet()

            return@withContext EMISConfig.fromJson(dataStore?.value())
        }

    override suspend fun getOptions(
        ou: String?,
        program: String?,
        dataElement: String,
    ): List<DropdownItem> = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement)?.optionSetUid()

        val hideOptions = if (ou != null && program != null) {
            ruleEngineRepository.applyOptionRules(ou, program, dataElement)
        } else {
            emptyList()
        }

        return@withContext if (hideOptions.isEmpty()) {
            d2.optionByOptionSet(optionSet).map {
                DropdownItem(
                    id = it.uid(),
                    itemName = "${it.displayName()}",
                    code = it.code() ?: "",
                    sortOrder = it.sortOrder(),
                )
            }
        } else {
            d2.optionsNotInOptionGroup(hideOptions, optionSet).map {
                DropdownItem(
                    id = it.uid(),
                    itemName = "${it.displayName()}",
                    code = it.code() ?: "",
                    sortOrder = it.sortOrder(),
                )
            }.sortedBy { it.sortOrder }
        }
    }

    override suspend fun getOptionsByCode(
        dataElement: String,
        codes: List<String>,
    ): List<DropdownItem> = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement)?.optionSetUid()

        return@withContext d2.optionsByOptionSetAndCode(optionSet, codes).map {
            DropdownItem(
                id = it.uid(),
                itemName = "${it.displayName()}",
                code = it.code() ?: "",
                sortOrder = it.sortOrder(),
            )
        }
    }

    override suspend fun getAttendanceOptions(
        program: String,
    ) = withContext(Dispatchers.IO) {
        val config = getConfig(Constants.KEY)?.find { it.program == program }
            ?.attendance ?: return@withContext emptyList()

        val optionsCode = config.attendanceStatus?.mapNotNull { it.code } ?: emptyList()
        val colorUtils = ColorUtils()

        return@withContext getOptionsByCode("${config.status}", optionsCode).mapNotNull {
            val status = config.attendanceStatus?.find { status ->
                status.code == it.code
            }

            if (status != null) {
                AttendanceOption(
                    code = it.code,
                    key = status.key,
                    name = it.itemName,
                    dataElement = "${config.status}",
                    icon = Utils.dynamicIcons("${status.icon}"),
                    iconName = "${status.icon}",
                    color = Color(colorUtils.parseColor(status.color ?: Constants.DEFAULT_COLOR)),
                    actionOrder = it.sortOrder,
                )
            } else {
                null
            }
        }.sortedWith(compareBy { it.actionOrder })
    }

    override suspend fun getDataElement(uid: String): DataElement? =
        withContext(Dispatchers.IO) {
            return@withContext d2.dataElement(uid)
        }

    override suspend fun getTeisBy(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        options: List<String>,
    ): List<SearchTeiModel> = withContext(Dispatchers.IO) {
        return@withContext d2.eventsWithTrackedDataValues(
            ou,
            program,
            stage,
        ).filter {
            val dataElements = it.trackedEntityDataValues()?.associate { trackedEntityDataValue ->
                Pair(trackedEntityDataValue.dataElement(), trackedEntityDataValue.value())
            }
            dataElements?.keys?.containsAll(dataElementIds) == true &&
                dataElements.values.containsAll(options)
        }.mapNotNull {
            d2.enrollment("${it.enrollment()}")
        }.map {
            val tei = d2.trackedEntityModule()
                .trackedEntityInstances()
                .byUid().eq(it.trackedEntityInstance())
                .withTrackedEntityAttributeValues()
                .one().blockingGet()

            transform(tei, program, it)
        }
    }

    override suspend fun trackedEntityInstances(
        ou: String,
        program: String,
    ) = withContext(Dispatchers.IO) {
        val repository = d2.trackedEntityModule().trackedEntityInstanceQuery()

        return@withContext if (networkUtils.isOnline()) {
            repository.onlineFirst().allowOnlineCache().eq(true)
                .byOrgUnits().eq(ou)
                .byProgram().eq(program)
                .blockingGet()
                .flatMap { tei -> listOf(tei) }
                .map { tei -> transform(tei, program) }
        } else {
            repository.offlineOnly().allowOnlineCache().eq(false)
                .byOrgUnits().eq(ou)
                .byProgram().eq(program)
                .blockingGet()
                .flatMap { tei -> listOf(tei) }
                .map { tei -> transform(tei, program) }
        }
    }

    @Throws(IllegalArgumentException::class)
    override suspend fun getAttendanceEvent(
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String?,
        teis: List<String>,
        date: String?,
    ) = withContext(Dispatchers.IO) {
        val config = getConfig(Constants.KEY)?.find { it.program == program }
            ?.attendance ?: return@withContext emptyList()

        val colorUtils = ColorUtils()

        return@withContext d2.eventModule().events()
            .byTrackedEntityInstanceUids(teis)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .byEventDate().eq(
                if (date != null) {
                    Date.valueOf(date)
                } else {
                    DateUtils.getInstance().today
                },
            )
            .withTrackedEntityDataValues()
            .blockingGet()
            .mapNotNull {
                eventTransform(it, dataElement, reasonDataElement)
            }
            .map { attendanceEntity ->
                val status = config.attendanceStatus?.find { status ->
                    status.code == attendanceEntity.value
                }

                attendanceEntity.withBtnSettings(
                    icon = Utils.dynamicIcons("${status?.icon}"),
                    iconName = "${status?.icon}",
                    iconColor = Color(colorUtils.parseColor("${status?.color}")),
                )
            }
    }

    override suspend fun dateValidation(id: String): CalendarConfig? =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val dataStore = d2.dataStoreModule()
                    .dataStore()
                    .byNamespace().eq("semis")
                    .byKey().eq(id)
                    .one().blockingGet()

                EMISConfig.schoolCalendarJson(dataStore?.value())
            } catch (_: Exception) {
                null
            }
        }

    override suspend fun getSubjects(stage: String) = withContext(Dispatchers.IO) {
        return@withContext d2.programModule().programStageDataElements()
            .byProgramStage().eq(stage)
            .blockingGet()
            .map { stageDl ->
                val dl = d2.dataElement(stageDl.dataElement()?.uid() ?: "")

                Subject(
                    uid = dl?.uid() ?: "",
                    code = dl?.code()?.ifEmpty { "" },
                    color = dl?.style()?.color(),
                    displayName = dl?.displayFormName(),
                )
            }
    }

    override suspend fun getTerms(stages: List<ProgramStage>) = withContext(Dispatchers.IO) {
        val stagesIds = stages.mapNotNull { it.programStage }

        return@withContext d2.programModule().programStages()
            .byUid().`in`(stagesIds)
            .blockingGet()
            .map {
                DropdownItem(
                    id = it.uid(),
                    itemName = it.displayName() ?: "",
                    code = it.code(),
                )
            }
    }

    private fun eventTransform(
        event: Event,
        dataElement: String,
        reasonDataElement: String?,
    ): AttendanceEntity? {
        val dataValue = event.trackedEntityDataValues()?.find { it.dataElement() == dataElement }
        val reason = event.trackedEntityDataValues()?.find { it.dataElement() == reasonDataElement }

        return if (dataValue != null) {
            val tei = d2.enrollment(event.enrollment().toString())?.trackedEntityInstance() ?: ""

            AttendanceEntity(
                tei = tei,
                dataElement = dataElement,
                value = dataValue.value().toString(),
                reasonDataElement = if (reason == null) {
                    null
                } else {
                    reasonDataElement
                },
                reasonOfAbsence = reason?.value(),
                date = DateHelper.formatDate(
                    event.eventDate()?.time ?: DateUtils.getInstance().today.time,
                ).toString(),
            )
        } else {
            null
        }
    }

    private fun transform(
        tei: TrackedEntityInstance?,
        program: String?,
        enrollment: Enrollment? = null,
    ): SearchTeiModel {
        val searchTei = SearchTeiModel()
        searchTei.tei = tei
        currentProgram = program ?: ""

        if (tei?.trackedEntityAttributeValues() != null) {
            if (program != null) {
                val programAttributes = d2.programModule().programTrackedEntityAttributes()
                    .byProgram().eq(program)
                    .byDisplayInList().isTrue
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()

                for (programAttribute in programAttributes) {
                    val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                        .uid(programAttribute.trackedEntityAttribute()!!.uid())
                        .blockingGet()

                    for (attrValue in tei.trackedEntityAttributeValues()!!) {
                        if (attrValue.trackedEntityAttribute() == attribute?.uid()) {
                            addAttribute(searchTei, attrValue, attribute)
                            break
                        }
                    }
                }
            } else {
                val typeAttributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
                    .byTrackedEntityTypeUid().eq(searchTei.tei.trackedEntityType())
                    .byDisplayInList().isTrue
                    .blockingGet()
                for (typeAttribute in typeAttributes) {
                    val attribute = d2.trackedEntityModule().trackedEntityAttributes()
                        .uid(typeAttribute.trackedEntityAttribute()!!.uid())
                        .blockingGet()
                    for (attrValue in tei.trackedEntityAttributeValues()!!) {
                        if (attrValue.trackedEntityAttribute() == attribute?.uid()) {
                            addAttribute(searchTei, attrValue, attribute)
                            break
                        }
                    }
                }
            }
        }

        if (enrollment != null) {
            searchTei.addEnrollment(enrollment)
        }

        searchTei.displayOrgUnit = displayOrgUnit()
        return searchTei
    }

    private fun addAttribute(
        searchTei: SearchTeiModel,
        attrValue: TrackedEntityAttributeValue,
        attribute: TrackedEntityAttribute?,
    ) {
        val friendlyValue = attrValue.userFriendlyValue(d2)

        val attrValueBuilder = TrackedEntityAttributeValue.builder()
        attrValueBuilder.value(friendlyValue)
            .created(attrValue.created())
            .lastUpdated(attrValue.lastUpdated())
            .trackedEntityAttribute(attrValue.trackedEntityAttribute())
            .trackedEntityInstance(searchTei.tei.uid())
        searchTei.addAttributeValue(attribute?.displayFormName(), attrValueBuilder.build())
    }

    private fun displayOrgUnit(): Boolean {
        return d2.organisationUnitModule().organisationUnits()
            .byProgramUids(listOf(currentProgram))
            .blockingGet().size > 1
    }
}
