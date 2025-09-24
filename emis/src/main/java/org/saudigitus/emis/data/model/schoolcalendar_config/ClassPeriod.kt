package org.saudigitus.emis.data.model.schoolcalendar_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClassPeriod(
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("endDate")
    val endDate: String?,
    @JsonProperty("key")
    val key: String?,
    @JsonProperty("startDate")
    val startDate: String?
)