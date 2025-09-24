package org.saudigitus.emis.data.model.app_config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class StatusOption(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("color")
    val color: String?,
    @JsonProperty("ConfigKey")
    val configKey: String?,
    @JsonProperty("icon")
    val icon: String?,
    @JsonProperty("key")
    val key: String?
)
