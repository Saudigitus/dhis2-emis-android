package org.saudigitus.emis.data.model.app_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Defaults(
    @JsonProperty("allowSearching")
    val allowSearching: Boolean?,
    @JsonProperty("defaultOrder")
    val defaultOrder: String?
)