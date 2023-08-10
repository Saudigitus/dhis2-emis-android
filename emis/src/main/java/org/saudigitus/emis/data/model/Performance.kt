package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Performance(
    @JsonProperty("programStages")
    val programStages: List<ProgramStage?>?
)