package org.saudigitus.emis.data.model.dto

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class AttendanceEntity(
    val tei: String,
    val enrollment: String,
    val dataElement: String,
    val value: String,
    val reasonDataElement: String? = null,
    val reasonOfAbsence: String? = null,
    val date: String,
    val setting: Settings? = null,
)

data class Settings(
    val icon: ImageVector? = null,
    val iconName: String?,
    val iconColor: Color,
)
fun AttendanceEntity.withBtnSettings(
    icon: ImageVector?,
    iconName: String?,
    iconColor: Color,
) = AttendanceEntity(
    this.tei,
    this.enrollment,
    this.dataElement,
    this.value,
    this.reasonDataElement,
    this.reasonOfAbsence,
    this.date,
    Settings(
        icon,
        iconName,
        iconColor,
    ),
)
