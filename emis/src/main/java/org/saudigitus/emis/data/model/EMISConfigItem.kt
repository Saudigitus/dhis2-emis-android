package org.saudigitus.emis.data.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EMISConfigItem(
    @JsonProperty("attendance")
    val attendance: Attendance?,
    @JsonProperty("modules")
    val modules: List<Module> = emptyList(),
    @JsonProperty("defaults")
    val default: DefaultConfig?,
    @JsonProperty("key")
    val key: String?,
    @JsonProperty("lastUpdate")
    val lastUpdate: String?,
    @JsonProperty("performance")
    val performance: Performance?,
    @JsonProperty("program")
    val program: String?,
    @JsonProperty("registration")
    val registration: Registration?,
    @JsonProperty("socio-economics")
    val socioEconomics: SocioEconomics?,
    @JsonProperty("transfer")
    val transfer: Transfer?,
)
