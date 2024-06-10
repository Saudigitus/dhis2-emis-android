package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Attendance(
    @JsonProperty("absenceReason")
    val absenceReason: String?,
    @JsonProperty("programStage")
    val programStage: String?,
    @JsonProperty("status")
    val status: String?,
    @JsonProperty("statusOptions")
    val attendanceStatus: List<AttendanceStatus>?,
)
