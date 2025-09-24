package org.saudigitus.emis.data.model.app_config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EMISConfigItem(
    @JsonProperty("absenteeism")
    val absenteeism: Absenteeism?,
    @JsonProperty("attendance")
    val attendance: Attendance?,
    @JsonProperty("defaults")
    val defaults: Defaults?,
    @JsonProperty("filters")
    val filters: Filters?,
    @JsonProperty("final-result")
    val finalResult: FinalResult?,
    @JsonProperty("key")
    val key: String?,
    @JsonProperty("lastUpdate")
    val lastUpdate: String?,
    @JsonProperty("performance")
    val performance: Performance?,
    @JsonProperty("program")
    val program: String?,
    @JsonProperty("reenroll")
    val reenroll: Reenroll?,
    @JsonProperty("registration")
    val registration: Registration?,
    @JsonProperty("socio-economics")
    val socioEconomics: SocioEconomics?,
    @JsonProperty("trackedEntityType")
    val trackedEntityType: String?,
    @JsonProperty("transfer")
    val transfer: Transfer?
)
