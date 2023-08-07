package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class Registration(
    @JsonProperty("academicYear")
    val academicYear: String?,
    @JsonProperty("grade")
    val grade: String?,
    @JsonProperty("programStage")
    val programStage: String?,
    @JsonProperty("section")
    val section: String?
)