package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AttendanceStatus(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("icon")
    val icon: String?,
    @JsonProperty("color")
    val color: String?,
    @JsonProperty("key")
    val key: String?,
)
