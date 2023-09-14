package org.saudigitus.emis.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dhis2.commons.network.NetworkUtils
import org.hisp.dhis.android.core.D2
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.local.FavoriteConfigRepository
import org.saudigitus.emis.data.local.repository.DataManagerImpl
import org.saudigitus.emis.data.local.repository.FavoriteConfigRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesNetworkUtils(
        @ApplicationContext context: Context
    ): NetworkUtils = NetworkUtils(context)

    @Provides
    @Singleton
    fun providesDataManager(
        d2: D2,
        networkUtils: NetworkUtils
    ): DataManager = DataManagerImpl(d2, networkUtils)

    @Provides
    @Singleton
    fun providesFavoriteRepository(
        @ApplicationContext context: Context
    ): FavoriteConfigRepository = FavoriteConfigRepositoryImpl(context)
}