package org.saudigitus.emis.data.model.dto

import androidx.compose.ui.graphics.Color

data class Absence(
    val index: Int = -1,
    val ou: String = "",
    val tei: String = "",
    val enrollment: String = "",
    val value: String = "",
    val color: Color? = null,
    val reasonOfAbsence: String = "",
)
