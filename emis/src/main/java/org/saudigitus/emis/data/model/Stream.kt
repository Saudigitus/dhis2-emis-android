package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class Stream(
    @JsonProperty("grade")
    val grade: String?,
    @JsonProperty("sections")
    val sections: List<Section?>?
)