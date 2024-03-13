package org.saudigitus.emis.data.local

import org.dhis2.commons.data.SearchTeiModel
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.option.Option
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.data.model.EMISConfigItem
import org.saudigitus.emis.data.model.dto.AttendanceEntity

interface DataManager {

    suspend fun save(
        ou: String,
        program: String,
        programStage: String,
        attendance: AttendanceEntity
    )
    suspend fun getConfig(id: String): List<EMISConfigItem>?

    suspend fun getOptions(
        dataElement: String
    ): List<Option>

    suspend fun getDataElement(uid: String): DataElement

    suspend fun getTeisBy(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        options: List<String>
    ): List<SearchTeiModel>

    suspend fun trackedEntityInstances(
        ou: String,
        program: String
    ): List<SearchTeiModel>

    suspend fun getAttendanceEvent(
        program: String,
        programStage: String,
        dataElement: String,
        reasonDataElement: String? = null,
        teis: List<String>,
        date: String?
    ): List<AttendanceEntity>

    suspend fun dateValidation(id: String): CalendarConfig?
}