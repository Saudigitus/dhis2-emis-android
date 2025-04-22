package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Module(
    @JsonProperty("key")
    val key: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("display")
    val display: Boolean
)
