package org.saudigitus.emis.data.model.app_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Registration(
    @JsonProperty("enabled")
    val enabled: Boolean?,
    @JsonProperty("grade")
    val grade: String?,
    @JsonProperty("lastUpdate")
    val lastUpdate: String?,
    @JsonProperty("programStage")
    val programStage: String?,
    @JsonProperty("section")
    val section: String?
)