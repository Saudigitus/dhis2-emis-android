package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class Section(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("displayName")
    val displayName: String?
)