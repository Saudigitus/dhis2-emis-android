package org.saudigitus.emis.data.model.schoolcalendar_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SchoolCalendarConfig(
    @JsonProperty("academicYear")
    val academicYear: String?,
    @JsonProperty("defaults")
    val defaults: Defaults?,
    @JsonProperty("schoolCalendar")
    val schoolCalendar: List<SchoolCalendar?>?
)