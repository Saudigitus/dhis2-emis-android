package org.saudigitus.emis.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Summary(
    val count: Int,
    val iconName: String?,
    val icon: ImageVector? = null,
    val color: Color? = null,
)
