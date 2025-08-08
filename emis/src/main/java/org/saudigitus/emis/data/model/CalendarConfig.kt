package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class CalendarConfig(
    @JsonProperty("academicYear")
    val academicYear: AcademicYear,
    @JsonProperty("classPeriods")
    val classPeriods: List<ClassPeriod>,
    @JsonProperty("holidays")
    val holidays: List<Holiday>,
    @JsonProperty("weekDays")
    val weekDays: WeekDays,
)
