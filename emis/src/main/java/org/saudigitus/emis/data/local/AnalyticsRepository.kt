package org.saudigitus.emis.data.local

import org.saudigitus.emis.data.model.AnalyticGroup

interface AnalyticsRepository {
    suspend fun getAnalyticsGroup(
        tei: String,
        program: String,
    ): List<AnalyticGroup>
}