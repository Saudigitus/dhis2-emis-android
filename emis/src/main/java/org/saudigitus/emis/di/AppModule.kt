package org.saudigitus.emis.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.ui.provider.HintProvider
import org.dhis2.form.ui.provider.HintProviderImpl
import org.hisp.dhis.android.core.D2
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.local.FormRepository
import org.saudigitus.emis.data.local.repository.DataManagerImpl
import org.saudigitus.emis.data.local.repository.FormRepositoryImpl
import org.saudigitus.emis.service.RuleEngineRepository
import org.saudigitus.emis.ui.teis.mapper.TEICardMapper
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesNetworkUtils(
        @ApplicationContext context: Context,
    ): NetworkUtils = NetworkUtils(context)

    @Provides
    @Singleton
    fun providesRuleEngineRepository(d2: D2) = RuleEngineRepository(d2)

    @Provides
    @Singleton
    fun providesTEICardMapper(
        @ApplicationContext context: Context,
        resourcesManager: ResourceManager,
    ) = TEICardMapper(context, resourcesManager)

    @Provides
    @Singleton
    fun providesDataManager(
        d2: D2,
        networkUtils: NetworkUtils,
        ruleEngineRepository: RuleEngineRepository,
    ): DataManager = DataManagerImpl(d2, networkUtils, ruleEngineRepository)

    @Provides
    @Singleton
    fun providesHintProvider(@ApplicationContext context: Context): HintProvider =
        HintProviderImpl(context)

    @Provides
    @Singleton
    fun providesFormRepository(d2: D2, hintProvider: HintProvider): FormRepository {
        return FormRepositoryImpl(d2, hintProvider)
    }
}
