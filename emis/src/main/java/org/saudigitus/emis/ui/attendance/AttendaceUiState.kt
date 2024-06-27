package org.saudigitus.emis.ui.attendance

import androidx.compose.ui.graphics.Color
import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.data.model.dto.Absence
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ToolbarHeaders

data class AttendanceUiState(
    val toolbarHeaders: ToolbarHeaders = ToolbarHeaders(""),
    val infoCard: InfoCard = InfoCard(),
    val students: List<SearchTeiModel> = emptyList(),
    val attendanceOptions: List<AttendanceOption> = emptyList(),
    val attendanceBtnState: List<AttendanceActionButtonState> = emptyList(),
    val attendanceStep: ButtonStep = ButtonStep.EDITING,
    val attendanceStatus: List<AttendanceEntity> = emptyList(),
    val reasonOfAbsence: List<DropdownItem> = emptyList(),
    val absence: List<Absence>  = emptyList(),
    val schoolCalendar: CalendarConfig? = null
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
