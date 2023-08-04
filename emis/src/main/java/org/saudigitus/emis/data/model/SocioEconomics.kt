package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class SocioEconomics(
    @JsonProperty("programStage")
    val programStage: String?
)