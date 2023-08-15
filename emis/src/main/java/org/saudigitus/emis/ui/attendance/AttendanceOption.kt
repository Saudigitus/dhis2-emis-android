package org.saudigitus.emis.ui.attendance

data class AttendanceOption(
    val code: String? = null,
    val name: String? = null,
    val dataElement: String? = null,
    val icon: Int = 0,
    val hexColor: Long? = 0xFF00FF00,
    val actionOrder: Int? = null
)