package org.saudigitus.emis.ui.attendance

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class AttendanceOption(
    val code: String? = null,
    val name: String? = null,
    val dataElement: String? = null,
    val icon: ImageVector? = null,
    val iconName: String? = null,
    val color: Color? = null,
    val actionOrder: Int? = null
)