package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class Section(
    @JsonProperty("code")
    val code: String?,
    @JsonProperty("displayName")
    val displayName: String?
)