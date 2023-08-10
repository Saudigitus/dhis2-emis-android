package org.saudigitus.emis.ui.attendance

data class AttendanceActionButtonState(
    val btnIndex: Int = -1,
    val btnId: String? = null,
    val iconTint: Long? = null,
    val buttonState: AttendanceButtonSettings? = null
)

data class AttendanceButtonSettings(
    val buttonType: String? = null,
    val containerColor: Long? = null,
    val contentColor: Long? = null
)


enum class ButtonStep {
    EDITING,
    HOLD_SAVING,
    SAVING
}