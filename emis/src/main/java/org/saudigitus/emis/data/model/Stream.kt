package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class Stream(
    @JsonProperty("grade")
    var grade: String? = null,
    @JsonProperty("code")
    val code: String? = null,
    @JsonProperty("sections")
    val sections: List<Section> = emptyList()
)