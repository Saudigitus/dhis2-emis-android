package org.saudigitus.emis.ui.attendance

import androidx.compose.ui.graphics.Color
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.data.model.SearchTeiModel
import org.saudigitus.emis.data.model.dto.Absence
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.ui.form.FormData
import org.saudigitus.emis.ui.form.FormField

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val students: List<SearchTeiModel> = emptyList(),
    val attendanceOptions: List<AttendanceOption> = emptyList(),
    val attendanceBtnState: List<AttendanceActionButtonState> = emptyList(),
    val attendanceStep: ButtonStep = ButtonStep.EDITING,
    val attendanceStatus: List<AttendanceEntity> = emptyList(),
    val reasonOfAbsence: List<DropdownItem> = emptyList(),
    val absence: List<Absence> = emptyList(),
    val fieldsState: List<Field> = emptyList(),
    val formFields: List<FormField> = emptyList(),
    val formData: List<FormData>? = emptyList(),
    val schoolCalendar: CalendarConfig? = null,
)

data class AttendanceActionButtonState(
    val btnIndex: Int = -1,
    val btnId: String? = null,
    val iconTint: Long? = null,
    val buttonState: AttendanceButtonSettings? = null,
)

data class AttendanceButtonSettings(
    val buttonType: String? = null,
    val containerColor: Color? = null,
    val contentColor: Long? = null,
)

enum class ButtonStep {
    EDITING,
    HOLD_SAVING,
    SAVING,
}
