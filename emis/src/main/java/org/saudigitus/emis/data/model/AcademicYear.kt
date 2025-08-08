package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AcademicYear(
    @JsonProperty("startDate")
    val startDate: String,
    @JsonProperty("endDate")
    val endDate: String,
)
