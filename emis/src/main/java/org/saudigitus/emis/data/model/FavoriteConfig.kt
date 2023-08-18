package org.saudigitus.emis.data.model


import com.fasterxml.jackson.annotation.JsonProperty

data class FavoriteConfig(
    @JsonProperty("favorites")
    val favorites: List<Favorite?>?
)