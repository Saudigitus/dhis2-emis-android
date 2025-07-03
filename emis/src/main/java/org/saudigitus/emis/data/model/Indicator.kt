package org.saudigitus.emis.data.model

import androidx.compose.ui.graphics.Color

data class Indicator(
    val uid: String,
    val programIndicator: String,
    val name: String?,
    val label: String,
    val type: String,
    val value: String?,
)
