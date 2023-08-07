package org.saudigitus.emis.data.local

import org.dhis2.commons.data.SearchTeiModel
import org.hisp.dhis.android.core.option.Option
import org.saudigitus.emis.data.model.EMISConfigItem

interface DataManager {

    suspend fun downloadDatastore()
    suspend fun getConfig(id: String): List<EMISConfigItem>?

    suspend fun getOptions(
        dataElement: String
    ): List<Option>
    suspend fun getTeisBy(
        ou: String,
        program: String,
        stage: String,
        dataElementIds: List<String>,
        options: List<String>
    ): List<SearchTeiModel>

    suspend fun trackedEntityInstances(
        ou: String,
        program: String
    ): List<SearchTeiModel>
}