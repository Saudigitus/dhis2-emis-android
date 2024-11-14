package org.saudigitus.emis.data.local

import kotlinx.coroutines.flow.Flow
import org.saudigitus.emis.data.model.UserPreferences

interface UserPreferencesRepository {
    suspend fun save(userPreferences: UserPreferences)
    fun getPreferences(): Flow<UserPreferences>
}