package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class ProgramStage(
    @JsonProperty("programStage")
    val programStage: String?
)