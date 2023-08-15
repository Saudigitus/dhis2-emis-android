package org.saudigitus.emis.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.DateUtils
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.model.Attendance
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.data.model.dto.withBtnSettings
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.utils.Constants.KEY
import org.saudigitus.emis.utils.DateHelper
import org.saudigitus.emis.utils.Utils.WHITE
import org.saudigitus.emis.utils.Utils.getColorByIconName
import org.saudigitus.emis.utils.Utils.getDrawableIdByName
import org.saudigitus.emis.utils.Utils.getIconByName
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel
@Inject constructor(
    private val repository: DataManager
) : ViewModel() {

    private val _teis = MutableStateFlow<List<SearchTeiModel>>(emptyList())
    val teis: StateFlow<List<SearchTeiModel>> = _teis

    private val _attendanceOptions = MutableStateFlow<List<AttendanceOption>>(emptyList())
    val attendanceOptions: StateFlow<List<AttendanceOption>> = _attendanceOptions

    private val _datastoreAttendance = MutableStateFlow<Attendance?>(null)
    private val datastoreAttendance: StateFlow<Attendance?> = _datastoreAttendance

    private val _attendanceStatus = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendanceStatus: StateFlow<List<AttendanceEntity>> = _attendanceStatus

    private val _attendanceStep = MutableStateFlow(ButtonStep.EDITING)
    val attendanceStep: StateFlow<ButtonStep> = _attendanceStep

    private val _attendanceBtnState =
        MutableStateFlow<List<AttendanceActionButtonState>>(emptyList())
    val attendanceBtnState: StateFlow<List<AttendanceActionButtonState>> = _attendanceBtnState

    private val _toolbarHeaders = MutableStateFlow(ToolbarHeaders(title = "Attendance"))
    val toolbarHeaders: StateFlow<ToolbarHeaders> = _toolbarHeaders

    private val _program = MutableStateFlow("")
    val program: StateFlow<String> = _program

    private val _infoCard = MutableStateFlow(InfoCard())
    val infoCard: StateFlow<InfoCard> = _infoCard

    init {
        viewModelScope.launch {
            val config = repository.getConfig(KEY)

            if (config != null) {
                for (c in config) {
                    _datastoreAttendance.value = c.attendance
                    break
                }
            }
            getAttendanceOptions(datastoreAttendance.value?.status ?: "")
        }
    }

    fun setTeis(teis: List<SearchTeiModel>) {
        _teis.value = teis
    }

    fun setProgram(program: String) {
        _program.value = program
        viewModelScope.launch {
            attendanceEvents()
        }
    }

    fun setInfoCard(infoCard: InfoCard) {
        _infoCard.value = infoCard
    }

    fun setAttendanceDate(date: String) {
        viewModelScope.launch {
            attendanceEvents(date)
        }
        _toolbarHeaders.update {
            it.copy(
                subtitle = DateHelper.formatDateWithWeekDay(date)
            )
        }
    }

    private suspend fun getAttendanceOptions(dataElement: String) {
        _attendanceOptions.value = repository.getOptions(dataElement).map {
            val status = datastoreAttendance.value?.attendanceStatus?.find { status ->
                status.code == it.code()
            } ?: return

            AttendanceOption(
                code = it.code(),
                name = it.displayName() ?: "",
                dataElement = dataElement,
                icon = getIconByName("${status.icon}"),
                hexColor = getColorByIconName("${status.icon}"),
                actionOrder = it.sortOrder()
            )
        }.sortedWith(compareBy { it.actionOrder })
    }

    private suspend fun attendanceEvents(
        date: String? = DateHelper.formatDate(DateUtils.getInstance().today.time)
    ) {
        teis.collect {
            val teiKeys = it.map { teiModel -> teiModel.tei.uid() }
            try {
                _attendanceStatus.value = repository.getAttendanceEvent(
                    program = program.value,
                    programStage = datastoreAttendance.value?.programStage ?: "",
                    dataElement = datastoreAttendance.value?.status ?: "",
                    reasonDataElement = datastoreAttendance.value?.absenceReason ?: "",
                    teis = teiKeys,
                    date = date.toString()
                ).map { attendanceEntity ->
                    val status = datastoreAttendance.value?.attendanceStatus?.find { status ->
                        status.code == attendanceEntity.value
                    } ?: return@collect

                    attendanceEntity.withBtnSettings(
                        icon = getDrawableIdByName("${status.icon}"),
                        iconColor = getColorByIconName("${status.icon}")
                    )
                }

                setInitialAttendanceStatus()
            } catch (e: Exception) {
                Timber.tag("ATTENDANCE_DATA").e(e)
            }
        }
    }

    private fun setInitialAttendanceStatus() {
        _attendanceBtnState.value = attendanceStatus.value.map { attendance ->
            attendanceActionButtonMapper(
                index = attendanceOptions.value.indexOfFirst { it.code == attendance.value },
                tei = attendance.tei,
                attendanceValue = attendance.value,
                containerColor = attendanceOptions.value.find {
                    it.code == attendance.value
                }?.hexColor ?: return
            )
        }
    }

    private fun attendanceActionButtonMapper(
        index: Int,
        tei: String,
        attendanceValue: String,
        containerColor: Long
    ) = AttendanceActionButtonState(
        btnIndex = index,
        btnId = tei,
        iconTint = 0,
        buttonState = AttendanceButtonSettings(
            buttonType = attendanceValue,
            containerColor = containerColor,
            contentColor = WHITE
        )
    )
}