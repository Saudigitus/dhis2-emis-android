package org.saudigitus.emis.data.local

import org.hisp.dhis.android.core.option.Option
import org.saudigitus.emis.data.model.EventTuple
import org.saudigitus.emis.ui.form.FormData
import org.saudigitus.emis.ui.form.FormField

interface FormRepository {
    suspend fun save(eventTuple: EventTuple)
    suspend fun keyboardInputTypeByStage(stage: String, dl: String): List<FormField>
    suspend fun getOptions(dataElement: String): List<Option>
    suspend fun getEvents(
        ou: String,
        program: String,
        programStage: String,
        dataElement: String,
        teis: List<String>,
    ): List<FormData>
}
