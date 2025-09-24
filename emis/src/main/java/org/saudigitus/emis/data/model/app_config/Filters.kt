package org.saudigitus.emis.data.model.app_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Filters(
    @JsonProperty("dataElements")
    val dataElements: List<DataElement?>?
)