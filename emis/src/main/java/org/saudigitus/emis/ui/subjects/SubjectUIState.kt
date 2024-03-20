package org.saudigitus.emis.ui.subjects

import org.saudigitus.emis.data.model.Subject
import org.saudigitus.emis.ui.components.Item
import org.saudigitus.emis.ui.components.ToolbarHeaders

data class SubjectUIState(
    val toolbarHeaders: ToolbarHeaders,
    val filters: List<Item> = emptyList(),
    val subjects: List<Subject> = emptyList()
)
