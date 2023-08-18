package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class Stream(
    @JsonProperty("grade")
    val grade: String?,
    @JsonProperty("sections")
    val sections: List<Section?>?
)