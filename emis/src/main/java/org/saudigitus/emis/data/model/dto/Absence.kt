package org.saudigitus.emis.data.model.dto

data class Absence(
    val index: Int = -1,
    val ou: String = "",
    val tei: String = "",
    val value: String = "",
    val reasonOfAbsence: String = "",
)
