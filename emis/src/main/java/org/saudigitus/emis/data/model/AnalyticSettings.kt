package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.saudigitus.emis.utils.Mapper.translateJsonToObject

@JsonIgnoreProperties(ignoreUnknown = true)
data class AnalyticSettings(
    @JsonProperty("tei")
    val tei: List<Analytic>
)
