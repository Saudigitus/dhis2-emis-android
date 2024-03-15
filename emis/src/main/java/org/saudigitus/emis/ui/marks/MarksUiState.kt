package org.saudigitus.emis.ui.marks

import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.ui.form.FormData
import org.saudigitus.emis.ui.form.FormField

sealed interface MarksUiState {
    data class Screen(
        val toolbarHeaders: ToolbarHeaders,
        private var attendanceDate: String,
        val students: List<SearchTeiModel>,
    ): MarksUiState {
        fun setDate(date: String) {
            attendanceDate = date
        }
    }

    data class MarksForm(
        val marksState: List<Field>,
        val marksKey: String,
        val marksFields: List<FormField>,
        val marksData: List<FormData>?,
    ): MarksUiState
}