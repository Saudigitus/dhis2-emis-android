package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class Favorite(
    @JsonProperty("school")
    val school: String? = null,
    @JsonProperty("stream")
    val stream: List<Stream> = emptyList()
)