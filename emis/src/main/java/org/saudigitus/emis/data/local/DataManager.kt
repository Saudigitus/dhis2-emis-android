package org.saudigitus.emis.data.local

import org.dhis2.commons.data.SearchTeiModel
import org.hisp.dhis.android.core.dataelement.DataElement
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.data.model.EMISConfigItem
import org.saudigitus.emis.data.model.ProgramStage
import org.saudigitus.emis.data.model.Subject
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.ui.attendance.AttendanceOption
import org.saudigitus.emis.ui.components.DropdownItem

interface DataManager {

    suspend fun save(
        ou: String,
        program: String,
        programStage: String,
        attendance: AttendanceEntity,
    )
    suspend fun getConfig(id: String): List<EMISConfigItem>?

    /**
     * @param ou OrganizationUnit uid
     * @param program Program uid
     * @param dataElement DataElement uid
     *
     * Set ou uid and program uid to apply the program rules
     */
    suspend fun getOptions(
        ou: String?,
        program: String?,
        dataElement: String,
    ): List<DropdownItem>

    suspend fun getOptionsByCode(
        dataElement: String,
        codes: List<String>,
    ): List<DropdownItem>

    suspend fun getAttendanceOptions(
        program: String,
    ): List<AttendanceOption>

    suspend fun getDataElement(uid: String): DataElement?

    suspend fun getTeisBy(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        options: List<String>,
    ): List<SearchTeiModel>

    suspend fun trackedEntityInstances(
        ou: String,
        program: String,
    ): List<SearchTeiModel>

    suspend fun getAttendanceEvent(
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String? = null,
        teis: List<String>,
        date: String?,
    ): List<AttendanceEntity>

    suspend fun dateValidation(id: String): CalendarConfig?

    suspend fun getSubjects(stage: String): List<Subject>

    suspend fun getTerms(stages: List<ProgramStage>): List<DropdownItem>
}
