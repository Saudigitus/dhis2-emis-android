package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class Performance(
    @JsonProperty("programStages")
    val programStages: List<ProgramStage?>?
)