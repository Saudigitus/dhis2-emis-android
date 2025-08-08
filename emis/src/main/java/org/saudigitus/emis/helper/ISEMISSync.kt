package org.saudigitus.emis.helper

interface ISEMISSync {

    suspend fun downloadTEIsByUids(
        ou: String,
        program: String,
        dataElementIds: List<String>,
        dataValues: List<String>,
    )
}
