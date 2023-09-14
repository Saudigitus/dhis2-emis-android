package org.saudigitus.emis.data.local.repository

import android.content.Context
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.saudigitus.emis.data.local.FavoriteConfigRepository
import org.saudigitus.emis.data.local.FavoriteSerialization
import org.saudigitus.emis.data.model.FavoriteConfig
import javax.inject.Inject

val Context.dataStore by dataStore("favorites.json", FavoriteSerialization)

class FavoriteConfigRepositoryImpl
@Inject constructor(
    @ApplicationContext val context: Context
): FavoriteConfigRepository {
    override suspend fun save(favorite: FavoriteConfig) {
        withContext(Dispatchers.IO) {
            context.dataStore.updateData {
                it.copy(favorites = favorite.favorites)
            }
        }
    }

    override fun getFavorites(): Flow<FavoriteConfig> {
        return context.dataStore.data
    }
}