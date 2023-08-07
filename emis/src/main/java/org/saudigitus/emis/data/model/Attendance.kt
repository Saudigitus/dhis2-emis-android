package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class Attendance(
    @JsonProperty("absenceReason")
    val absenceReason: String?,
    @JsonProperty("programStage")
    val programStage: String?,
    @JsonProperty("status")
    val status: String?
)