package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Visualization(
    @JsonProperty("programIndicator")
    val programIndicator: String,
    @JsonProperty("type")
    val type: String
)