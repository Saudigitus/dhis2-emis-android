package org.saudigitus.emis.data.local.repository

import android.content.Context
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.saudigitus.emis.data.local.UserPreferencesRepository
import org.saudigitus.emis.data.local.UserPreferencesSerializer
import org.saudigitus.emis.data.model.UserPreferences
import javax.inject.Inject

val Context.dataStore by dataStore("preferences.json", UserPreferencesSerializer)

class UserPreferencesRepositoryImpl
@Inject constructor(
    @ApplicationContext val context: Context
): UserPreferencesRepository {
    override suspend fun save(userPreferences: UserPreferences) {
        withContext(Dispatchers.IO) {
            context.dataStore.updateData {
                it.copy(filters = userPreferences.filters)
            }
        }
    }

    override fun getPreferences(): Flow<UserPreferences> {
        return context.dataStore.data
    }
}