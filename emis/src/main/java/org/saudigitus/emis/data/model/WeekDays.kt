package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeekDays(
    @JsonProperty("monday")
    val monday: Boolean,
    @JsonProperty("tuesday")
    val tuesday: Boolean,
    @JsonProperty("wednesday")
    val wednesday: Boolean,
    @JsonProperty("thursday")
    val thursday: Boolean,
    @JsonProperty("friday")
    val friday: Boolean,
    @JsonProperty("saturday")
    val saturday: Boolean,
    @JsonProperty("sunday")
    val sunday: Boolean,
)
