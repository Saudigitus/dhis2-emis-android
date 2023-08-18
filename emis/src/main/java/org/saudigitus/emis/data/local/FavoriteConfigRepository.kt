package org.saudigitus.emis.data.local

import kotlinx.coroutines.flow.Flow
import org.saudigitus.emis.data.model.FavoriteConfig

interface FavoriteConfigRepository {
    suspend fun save(favorite: FavoriteConfig)
    fun getFavorites(): Flow<FavoriteConfig>
}