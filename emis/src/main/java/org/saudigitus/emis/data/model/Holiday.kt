package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Holiday(
    @JsonProperty("date")
    val date: String,
    @JsonProperty("event")
    val event: String,
)
