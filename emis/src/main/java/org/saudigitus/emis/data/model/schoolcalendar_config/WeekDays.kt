package org.saudigitus.emis.data.model.schoolcalendar_config


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeekDays(
    @JsonProperty("friday")
    val friday: Boolean?,
    @JsonProperty("monday")
    val monday: Boolean?,
    @JsonProperty("saturday")
    val saturday: Boolean?,
    @JsonProperty("sunday")
    val sunday: Boolean?,
    @JsonProperty("thursday")
    val thursday: Boolean?,
    @JsonProperty("tuesday")
    val tuesday: Boolean?,
    @JsonProperty("wednesday")
    val wednesday: Boolean?
)