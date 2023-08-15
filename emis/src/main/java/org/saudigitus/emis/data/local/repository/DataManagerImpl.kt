package org.saudigitus.emis.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.Bindings.userFriendlyValue
import org.dhis2.commons.bindings.dataElement
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.network.NetworkUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.model.EMISConfig
import org.saudigitus.emis.data.model.EMISConfigItem
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.utils.DateHelper
import org.saudigitus.emis.utils.eventsWithTrackedDataValues
import org.saudigitus.emis.utils.optionByOptionSet
import java.sql.Date
import javax.inject.Inject
import kotlin.jvm.Throws

class DataManagerImpl
@Inject constructor(
    val d2: D2,
    val networkUtils: NetworkUtils
) : DataManager {
    override suspend fun downloadDatastore() {
        withContext(Dispatchers.IO)  {
            d2.dataStoreModule().dataStoreDownloader().blockingDownload()
        }
    }

    override suspend fun getConfig(id: String): List<EMISConfigItem>? =
        withContext(Dispatchers.IO) {
            val dataStore = d2.dataStoreModule()
                .dataStore()
                .byNamespace().eq("semis")
                .byKey().eq(id)
                .one().blockingGet()

            return@withContext EMISConfig.fromJson(dataStore.value())
        }


    override suspend fun getOptions(
        dataElement: String
    ): List<Option> = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement).optionSetUid()

        return@withContext d2.optionByOptionSet(optionSet)
    }

    override suspend fun getTeisBy(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        options: List<String>
    ): List<SearchTeiModel> = withContext(Dispatchers.IO) {
        return@withContext d2.eventsWithTrackedDataValues(
            ou, program, stage
        ).filter {
            val dataElements = it.trackedEntityDataValues()?.associate { trackedEntityDataValue ->
                Pair(trackedEntityDataValue.dataElement(), trackedEntityDataValue.value())
            }
            dataElements?.keys?.containsAll(dataElementIds) == true &&
                dataElements.values.containsAll(options)
        }.map {
            d2.enrollment("${it.enrollment()}")
        }.map {
            val tei = d2.trackedEntityModule()
                .trackedEntityInstances()
                .byUid().eq(it.trackedEntityInstance())
                .withTrackedEntityAttributeValues()
                .one().blockingGet()

            transform(tei, program)
        }
    }

    override suspend fun trackedEntityInstances(
        ou: String,
        program: String
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
        date: String?
    ) = withContext(Dispatchers.IO) {
        return@withContext d2.eventModule().events()
            .byTrackedEntityInstanceUids(teis)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .byEventDate().eq(
                if (date != null) {
                    Date.valueOf(date)
                } else {
                    DateUtils.getInstance().today
                }
            )
            .withTrackedEntityDataValues()
            .blockingGet()
            .map {
                eventTransform(it, dataElement, reasonDataElement)
            }.requireNoNulls()
    }

    private fun eventTransform(
        event: Event,
        dataElement: String,
        reasonDataElement: String?,
    ): AttendanceEntity? {
        val dataValue = event.trackedEntityDataValues()?.find { it.dataElement() == dataElement }
        val reason = event.trackedEntityDataValues()?.find { it.dataElement() == reasonDataElement }

        return if (dataValue != null) {
            val tei = d2.enrollment(event.enrollment().toString()).trackedEntityInstance() ?: ""

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
                    event.eventDate()?.time ?: DateUtils.getInstance().today.time
                ).toString()
            )
        } else null
    }

    private fun transform(
        tei: TrackedEntityInstance,
        program: String?
    ): SearchTeiModel {
        val searchTei = SearchTeiModel()
        searchTei.tei = tei

        if (tei.trackedEntityAttributeValues() != null) {
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
                        if (attrValue.trackedEntityAttribute() == attribute.uid()) {
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
                        if (attrValue.trackedEntityAttribute() == attribute.uid()) {
                            addAttribute(searchTei, attrValue, attribute)
                            break
                        }
                    }
                }
            }
        }
        return searchTei
    }

    private fun addAttribute(
        searchTei: SearchTeiModel,
        attrValue: TrackedEntityAttributeValue,
        attribute: TrackedEntityAttribute
    ) {
        val friendlyValue = attrValue.userFriendlyValue(d2)

        val attrValueBuilder = TrackedEntityAttributeValue.builder()
        attrValueBuilder.value(friendlyValue)
            .created(attrValue.created())
            .lastUpdated(attrValue.lastUpdated())
            .trackedEntityAttribute(attrValue.trackedEntityAttribute())
            .trackedEntityInstance(searchTei.tei.uid())
        searchTei.addAttributeValue(attribute.displayFormName(), attrValueBuilder.build())
    }
}