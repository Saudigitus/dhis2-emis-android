package org.saudigitus.emis.data.local


import org.hisp.dhis.android.core.option.Option
import org.saudigitus.emis.data.model.Saving
import org.saudigitus.emis.ui.form.FormData
import org.saudigitus.emis.ui.form.FormField

interface FormRepository {
    suspend fun save(saving: Saving)
    suspend fun keyboardInputTypeByStage(stage: String): List<FormField>?
    suspend fun getOptions(dataElement: String): List<Option>
    suspend fun getEventsByDate(
        ou: String,
        program: String,
        programStage: String,
        dataElement: String,
        teis: List<String>,
        date: String
    ): List<FormData>
}