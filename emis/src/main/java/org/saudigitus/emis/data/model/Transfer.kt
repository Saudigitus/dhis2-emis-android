package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Transfer(
    @JsonProperty("destinySchool")
    val destinySchool: String?,
    @JsonProperty("programStage")
    val programStage: String?,
    @JsonProperty("status")
    val status: String?
)