package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import org.dhis2.data.service.SyncStatusData
import org.saudigitus.emis.data.model.EMISConfigItem

internal interface ProgramRepository {
    fun homeItems(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun programModels(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun aggregatesModels(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>>
    fun clearCache()
    suspend fun getConfigFromDataStore(id: String): List<EMISConfigItem>
}
