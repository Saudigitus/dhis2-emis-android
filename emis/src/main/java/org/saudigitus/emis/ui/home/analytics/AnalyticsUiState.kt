package org.saudigitus.emis.ui.home.analytics

import org.saudigitus.emis.data.model.AnalyticGroup

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val analyticsGroup: List<AnalyticGroup> = emptyList()
)
