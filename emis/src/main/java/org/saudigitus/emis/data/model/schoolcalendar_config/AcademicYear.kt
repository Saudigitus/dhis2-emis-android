package org.saudigitus.emis.data.model.schoolcalendar_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AcademicYear(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("description")
    val description: String?,
    @JsonProperty("endDate")
    val endDate: String?,
    @JsonProperty("label")
    val label: String?,
    @JsonProperty("startDate")
    val startDate: String?
)