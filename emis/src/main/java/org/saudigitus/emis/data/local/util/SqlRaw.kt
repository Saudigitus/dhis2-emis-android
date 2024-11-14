package org.saudigitus.emis.data.local.util

import org.dhis2.commons.date.DateUtils
import java.sql.Date

object SqlRaw {

    fun geTeiByAttendanceStatusQuery(
        ou: String,
        program: String,
        stage: String,
        attendanceStage: String,
        attendanceStatus: String,
        attendanceDataElement: String,
        reasonDataElement: String,
        eventDate: String?,
        dataElements: List<String>,
        options: List<String>,
    ): String {
        val formattedDataElements = dataElements.joinToString { "'$it'" }
        val formattedOptions = options.joinToString { "'$it'" }

        val date = if (eventDate != null) {
            Date.valueOf(eventDate)
        } else {
            DateUtils.getInstance().today
        }

        val subQuery = """
            select distinct event.uid as event, trackedEntityInstance.uid as tei, 
            trackedentitydatavalue.value as status  from event join  enrollment 
            on event.enrollment = enrollment.uid join trackedentityinstance 
            on enrollment.trackedEntityInstance = trackedEntityInstance.uid 
            join trackedentityattributevalue 
            on  trackedEntityInstance.uid = trackedentityattributevalue.trackedEntityInstance 
            join trackedentitydatavalue on event.uid = trackedentitydatavalue.event 
            where event.eventDate = '${date}T00:00:00.000' and event.organisationUnit = '$ou' 
            and event.program = '$program' and event.programStage in ('$stage','$attendanceStage') 
            and trackedentitydatavalue.dataElement in ($formattedDataElements, '$attendanceDataElement', '$reasonDataElement') 
            and trackedentitydatavalue.value in ($formattedOptions, '$attendanceStatus')
        """.trimIndent()

        return """
            select event, tei, status from ($subQuery) where status = '$attendanceStatus'
        """.trimIndent()
    }
}
