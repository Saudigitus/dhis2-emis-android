package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Analytic(
    @JsonProperty("program")
    val program: String,
    @JsonProperty("widgets")
    val widgets: List<Widget>
)
