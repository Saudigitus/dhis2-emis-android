package org.saudigitus.emis.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataElement
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.network.NetworkUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.local.util.SqlRaw
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.data.model.EMISConfig
import org.saudigitus.emis.data.model.EMISConfigItem
import org.saudigitus.emis.data.model.ProgramStage
import org.saudigitus.emis.data.model.SearchTeiModel
import org.saudigitus.emis.data.model.Subject
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.data.model.dto.withBtnSettings
import org.saudigitus.emis.service.RuleEngineRepository
import org.saudigitus.emis.ui.attendance.AttendanceOption
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.utils.Constants
import org.saudigitus.emis.utils.Transformations
import org.saudigitus.emis.utils.Utils
import org.saudigitus.emis.utils.Utils.getAttendanceStatusColor
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
    private val transformations: Transformations,
    val networkUtils: NetworkUtils,
    val ruleEngineRepository: RuleEngineRepository,
) : DataManager {

    private fun getAttributeOptionCombo() =
        d2.categoryModule().categoryOptionCombos()
            .byDisplayName().eq(Constants.DEFAULT).one().blockingGet()?.uid()

    private fun createEventProjection(
        enrollment: String,
        ou: String,
        program: String,
        programStage: String,
    ): String {
        return d2.eventModule().events()
            .blockingAdd(
                EventCreateProjection.builder()
                    .organisationUnit(ou)
                    .program(program).programStage(programStage)
                    .attributeOptionCombo(getAttributeOptionCombo())
                    .enrollment(enrollment).build(),
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
                    attendance.enrollment,
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

    override suspend fun getTrackedEntityType(program: String) = withContext(Dispatchers.IO) {
        return@withContext d2.programModule().programs()
            .byUid().eq(program)
            .withTrackedEntityType()
            .one().blockingGet()
            ?.trackedEntityType()
            ?.displayName()
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
                    color = getAttendanceStatusColor("${status.key}", "${status.color}"),
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

    override fun getTeisBy(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        dataValues: List<String>,
    ): Flow<List<SearchTeiModel>> = flow {
        emit(
            d2.eventsWithTrackedDataValues(
                ou,
                program,
                stage,
            ).filter {
                val dataElements = it.trackedEntityDataValues()?.associate { trackedEntityDataValue ->
                    Pair(trackedEntityDataValue.dataElement(), trackedEntityDataValue.value())
                }
                dataElements?.keys?.containsAll(dataElementIds) == true &&
                    dataElements.values.containsAll(dataValues)
            }.mapNotNull {
                d2.enrollment("${it.enrollment()}")
            }.map {
                val tei = d2.trackedEntityModule()
                    .trackedEntityInstances()
                    .byUid().eq(it.trackedEntityInstance())
                    .withTrackedEntityAttributeValues()
                    .one().blockingGet()

                transformations.transform(tei, program, it)
            },
        )
    }.buffer()
        .conflate()
        .flowOn(Dispatchers.IO)

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

        val deferredEvents = async {
            d2.eventModule().events()
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
                    transformations.eventTransform(it, dataElement, reasonDataElement)
                }
                .map { attendanceEntity ->
                    val status = config.attendanceStatus?.find { status ->
                        status.code == attendanceEntity.value
                    }

                    attendanceEntity.withBtnSettings(
                        icon = Utils.dynamicIcons("${status?.icon}"),
                        iconName = "${status?.icon}",
                        iconColor = getAttendanceStatusColor("${status?.key}", "${status?.color}"),
                    )
                }
        }

        return@withContext deferredEvents.await()
    }

    override suspend fun geTeiByAttendanceStatus(
        ou: String,
        program: String,
        stage: String,
        attendanceStage: String,
        attendanceDataElement: String,
        reasonDataElement: String,
        date: String?,
        dataElementIds: List<String>,
        options: List<String>,
    ) = withContext(Dispatchers.IO) {
        val config = getConfig(Constants.KEY)?.find { it.program == program }
            ?.attendance ?: return@withContext emptyMap()

        val attendanceStatus = config.attendanceStatus?.find { status ->
            status.key == Constants.ABSENT
        }?.code ?: ""

        val data = mutableMapOf<SearchTeiModel, AttendanceEntity>()

        return@withContext try {
            val cursor = d2.databaseAdapter().rawQuery(
                SqlRaw.geTeiByAttendanceStatusQuery(
                    ou,
                    program,
                    stage,
                    attendanceStage,
                    attendanceStatus,
                    attendanceDataElement,
                    reasonDataElement,
                    date,
                    dataElementIds,
                    options,
                ),
            )

            if (cursor.count > 0) {
                cursor.moveToFirst()

                do {
                    if (!cursor.isNull(0) &&
                        !cursor.isNull(1) && !cursor.isNull(2)
                    ) {
                        val response = async {
                            transformations.teiEventTransform(
                                teiUid = cursor.getString(1),
                                eventUid = cursor.getString(0),
                                program = program,
                                attendanceDataElement = attendanceDataElement,
                                reasonDataElement = reasonDataElement,
                                config = config,
                            )
                        }

                        val result = response.await()

                        data[result.first] = result.second
                    }
                } while (cursor.moveToNext())

                data
            } else {
                emptyMap()
            }
        } catch (_: Exception) {
            emptyMap()
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
}
