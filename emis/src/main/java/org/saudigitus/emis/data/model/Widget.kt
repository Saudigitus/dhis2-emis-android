package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Widget(
    @JsonProperty("displayName")
    val displayName: String,
    @JsonProperty("visualizations")
    val visualizations: List<Visualization>
)
