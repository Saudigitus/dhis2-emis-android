package org.saudigitus.emis.data.model

import kotlinx.serialization.Serializable
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.ui.teis.FilterType

@Serializable
data class UserPreferences(
    val filters: Map<FilterType, DropdownItem> = emptyMap(),
)
