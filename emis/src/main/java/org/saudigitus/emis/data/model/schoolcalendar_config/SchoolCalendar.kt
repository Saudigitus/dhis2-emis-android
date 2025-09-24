package org.saudigitus.emis.data.model.schoolcalendar_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SchoolCalendar(
    @JsonProperty("academicYear")
    val academicYear: AcademicYear?,
    @JsonProperty("classPeriods")
    val classPeriods: List<ClassPeriod?>?,
    @JsonProperty("holidays")
    val holidays: List<Holiday?>?,
    @JsonProperty("id")
    val id: String?,
    @JsonProperty("weekDays")
    val weekDays: WeekDays?
)