package org.saudigitus.emis.ui.home

import androidx.compose.runtime.Immutable
import org.saudigitus.emis.ui.teis.FilterType

@Immutable
sealed class HomeUiEvent {
    data class OnFilterChange<T>(val filterType: FilterType, val obj: T) : HomeUiEvent()
    data class NavTo(val route: String) : HomeUiEvent()
    data object OnBack : HomeUiEvent()
    data object HideShowFilter : HomeUiEvent()
    data object Sync: HomeUiEvent()
}
