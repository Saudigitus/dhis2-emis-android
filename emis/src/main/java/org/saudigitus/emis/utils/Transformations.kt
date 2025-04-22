package org.saudigitus.emis.utils

import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.saudigitus.emis.data.model.Attendance
import org.saudigitus.emis.data.model.SearchTeiModel
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.data.model.dto.withBtnSettings
import org.saudigitus.emis.utils.Utils.getAttendanceStatusColor

class Transformations(private val d2: D2) {

    private lateinit var currentProgram: String

    fun teiEventTransform(
        teiUid: String,
        eventUid: String,
        program: String,
        attendanceDataElement: String,
        reasonDataElement: String,
        config: Attendance,
    ): Pair<SearchTeiModel, AttendanceEntity> {
        val tei = d2.trackedEntityModule()
            .trackedEntityInstances()
            .byUid().eq(teiUid)
            .withTrackedEntityAttributeValues()
            .one().blockingGet()

        val event = d2.eventModule().events()
            .byUid().eq(eventUid)
            .withTrackedEntityDataValues()
            .one().blockingGet()
        val enrollment = d2.enrollment(event!!.enrollment() ?: "")

        val teiModel = transform(tei, program, enrollment)
        val transformedEvent = eventTransform(event, attendanceDataElement, reasonDataElement)

        val status = config.attendanceStatus?.find { status ->
            status.code == transformedEvent?.value
        }

        val attendanceEntity = transformedEvent?.withBtnSettings(
            icon = Utils.dynamicIcons("${status?.icon}"),
            iconName = "${status?.icon}",
            iconColor = getAttendanceStatusColor("${status?.key}", "${status?.color}"),
        )

        return Pair(teiModel, attendanceEntity!!)
    }

    fun eventTransform(
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
                enrollment = event.enrollment()!!,
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

    fun transform(
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
            searchTei.setCurrentEnrollment(enrollment)
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
