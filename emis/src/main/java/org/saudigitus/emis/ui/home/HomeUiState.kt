package org.saudigitus.emis.ui.home

import android.os.Bundle
import androidx.compose.runtime.Stable
import org.saudigitus.emis.ui.components.DropdownState
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.ui.teis.FilterState

@Stable
data class HomeUiState(
    val isLoading: Boolean = true,
    val displayFilters: Boolean = true,
    val dataElementFilters: List<DropdownState> = emptyList(),
    val filterState: FilterState = FilterState(),
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val programSettings: Bundle? = null,
    val infoCard: InfoCard = InfoCard(),
)
