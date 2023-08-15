package org.saudigitus.emis.data.model.dto

data class AttendanceEntity(
    val tei: String,
    val dataElement: String,
    val value: String,
    val reasonDataElement: String? = null,
    val reasonOfAbsence: String? = null,
    val date: String,
    val setting: Settings? = null
)

data class Settings(
    val icon: Int,
    val iconColor: Long
)
fun AttendanceEntity.withBtnSettings(
    icon: Int,
    iconColor: Long
) = AttendanceEntity(
    this.tei,
    this.dataElement,
    this.value,
    this.reasonDataElement,
    this.reasonOfAbsence,
    this.date,
    Settings(
        icon,
        iconColor
    )
)
