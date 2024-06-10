package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class Favorite(
    val uid: String = "",
    @JsonProperty("school")
    val school: String? = null,
    @JsonProperty("stream")
    val stream: List<Stream> = emptyList(),
)
