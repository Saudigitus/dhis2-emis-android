package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class Favorite(
    @JsonProperty("school")
    val school: String?,
    @JsonProperty("stream")
    val stream: List<Stream?>?
)