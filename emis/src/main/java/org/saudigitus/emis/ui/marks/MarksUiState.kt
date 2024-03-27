package org.saudigitus.emis.ui.marks

import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.ui.form.FormData
import org.saudigitus.emis.ui.form.FormField

data class MarksUiState(
    val toolbarHeaders: ToolbarHeaders,
    val students: List<SearchTeiModel> = emptyList(),
    val marksState: List<Field> = emptyList(),
    val marksFields: List<FormField> = emptyList(),
    val marksData: List<FormData>? = emptyList(),
)