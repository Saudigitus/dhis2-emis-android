package org.saudigitus.emis.data.model


data class AnalyticGroup(
    val uid: String,
    val displayName: String,
    val type: String,
    val attendanceIndicators: List<AttendanceIndicator>
)