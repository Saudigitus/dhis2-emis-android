package org.saudigitus.emis.data.model.app_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DataElement(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("dataElement")
    val dataElement: String?,
    @JsonProperty("label")
    val label: String?,
    @JsonProperty("order")
    val order: Int?,
    @JsonProperty("ulrParam")
    val ulrParam: String?
)