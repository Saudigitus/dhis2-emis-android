package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteConfig(
    @JsonProperty("favorites")
    val favorites: List<Favorite>? = emptyList(),
)
