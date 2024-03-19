package org.saudigitus.emis.ui.attendance

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.date.DateUtils
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.model.Attendance
import org.saudigitus.emis.data.model.dto.Absence
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.data.model.dto.withBtnSettings
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.components.Item
import org.saudigitus.emis.utils.Constants.ABSENT
import org.saudigitus.emis.utils.Constants.KEY
import org.saudigitus.emis.utils.Constants.LATE
import org.saudigitus.emis.utils.Constants.PRESENT
import org.saudigitus.emis.utils.DateHelper
import org.saudigitus.emis.utils.Utils.WHITE
import org.saudigitus.emis.utils.Utils.getColorByAttendanceType
import org.saudigitus.emis.utils.Utils.getColorByIconName
import org.saudigitus.emis.utils.Utils.getDrawableIdByName
import org.saudigitus.emis.utils.Utils.getIconByName
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel
@Inject constructor(
    private val repository: DataManager
) : BaseViewModel(repository) {

    private val _datastoreAttendance = MutableStateFlow<Attendance?>(null)
    private val datastoreAttendance: StateFlow<Attendance?> = _datastoreAttendance

    private val _attendanceOptions = MutableStateFlow<List<AttendanceOption>>(emptyList())
    val attendanceOptions: StateFlow<List<AttendanceOption>> = _attendanceOptions

    private val _attendanceStatus = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendanceStatus: StateFlow<List<AttendanceEntity>> = _attendanceStatus

    private val _reasonOfAbsence = MutableStateFlow<List<Item>>(emptyList())
    val reasonOfAbsence: StateFlow<List<Item>> = _reasonOfAbsence

    private val _attendanceStep = MutableStateFlow(ButtonStep.EDITING)
    val attendanceStep: StateFlow<ButtonStep> = _attendanceStep

    private val _attendanceBtnState =
        MutableStateFlow<List<AttendanceActionButtonState>>(emptyList())
    val attendanceBtnState: StateFlow<List<AttendanceActionButtonState>> = _attendanceBtnState

    private val attendanceCache = mutableSetOf<AttendanceEntity>()
    private var attendanceBtnStateCache = mutableListOf(AttendanceActionButtonState())

    private val _absenceState = MutableStateFlow(Absence())
    private val absenceState: StateFlow<Absence> = _absenceState

    private val _absenceStateCache = MutableStateFlow<List<Absence>>(emptyList())
    val absenceStateCache: StateFlow<List<Absence>> = _absenceStateCache

    init {
        _toolbarHeaders.update {
            it.copy(
                title = "Attendance"
            )
        }
    }

    override fun setConfig(program: String) {
        viewModelScope.launch {
            val config = repository.getConfig(KEY)?.find { it.program == program }

            if (config != null) {
                _datastoreAttendance.value = config.attendance
            }
            getAttendanceOptions(datastoreAttendance.value?.status ?: "")
            getReasonForAbsence(datastoreAttendance.value?.absenceReason ?: "")
        }
    }

    override fun setProgram(program: String) {
        _program.value = program

        setConfig(program)

        viewModelScope.launch {
            attendanceEvents()
        }
    }

    fun setAttendanceStep(attendanceStep: ButtonStep) {
        _attendanceStep.value = attendanceStep
    }

    override fun setDate(date: String) {
        _eventDate.value = date
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

                clearCache()
                attendanceCache.addAll(attendanceStatus.value)

                setInitialAttendanceStatus()
            } catch (e: Exception) {
                Timber.tag("ATTENDANCE_DATA").e(e)
            }
        }
    }

    private fun setInitialAttendanceStatus() {
        attendanceBtnStateCache = attendanceStatus.value.map { attendance ->
            attendanceActionButtonMapper(
                index = attendanceOptions.value.indexOfFirst { it.code == attendance.value },
                tei = attendance.tei,
                attendanceValue = attendance.value,
                containerColor = attendanceOptions.value.find {
                    it.code == attendance.value
                }?.hexColor ?: return
            )
        }.toMutableList()

        _attendanceBtnState.value = attendanceBtnStateCache
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

    private fun getAttendanceUiState(
        index: Int,
        tei: String,
        value: String
    ): MutableList<AttendanceActionButtonState> {
        val uiCache = attendanceBtnStateCache.find { it.btnId == tei }

        val uiCacheItem = attendanceActionButtonMapper(
            index = index,
            tei = tei,
            attendanceValue = value,
            containerColor = getColorByAttendanceType(value)
        )

        if (uiCache == null) {
            attendanceBtnStateCache.add(uiCacheItem)
        } else {
            attendanceBtnStateCache.remove(uiCache)
            attendanceBtnStateCache.add(uiCacheItem)
        }

        return attendanceBtnStateCache
    }

    private fun getReasonForAbsence(dataElement: String) {
        viewModelScope.launch {
            _reasonOfAbsence.value = repository.getOptions(dataElement).map {
                Item(
                    id = it.uid(),
                    itemName = "${it.displayName()}",
                    code = it.code() ?: ""
                )
            }
        }
    }

    fun setAttendance(
        index: Int,
        ou: String,
        tei: String,
        value: String,
        reasonOfAbsence: String? = null
    ) {
        _attendanceBtnState.value = getAttendanceUiState(index, tei, value)

        val attendance = AttendanceEntity(
            tei = tei,
            dataElement = datastoreAttendance.value?.status ?: "",
            value = value,
            reasonDataElement = datastoreAttendance.value?.absenceReason,
            reasonOfAbsence = reasonOfAbsence,
            date = eventDate.value
        )

        val cacheItem = attendanceCache.find { it.tei == attendance.tei }

        if (cacheItem == null) {
            attendanceCache.add(attendance)
        } else {
            attendanceCache.remove(cacheItem)
            attendanceCache.add(attendance)
        }

        viewModelScope.launch {
            repository.save(
                ou = ou,
                program = program.value,
                programStage = datastoreAttendance.value?.programStage ?: "",
                attendance = attendance
            )
        }
    }

    fun setAbsence(
        index: Int? = null,
        ou: String? = null,
        tei: String? = null,
        value: String? = null,
        reasonOfAbsence: String? = null
    ) {
        if (index != null) {
            _absenceState.update {
                it.copy(index = index)
            }
        }
        if (ou != null) {
            _absenceState.update {
                it.copy(ou = ou)
            }
        }
        if (tei != null) {
            _absenceState.update {
                it.copy(tei = tei)
            }
        }
        if (value != null) {
            _absenceState.update {
                it.copy(value = value)
            }
        }
        if (reasonOfAbsence != null) {
            _absenceState.update {
                it.copy(reasonOfAbsence = reasonOfAbsence)
            }
        }
    }
    override fun save() {
        setAttendance(
            index = absenceState.value.index,
            ou = absenceState.value.ou,
            tei = absenceState.value.tei,
            value = absenceState.value.value,
            reasonOfAbsence = absenceState.value.reasonOfAbsence
        )

        val cache = mutableListOf<Absence>()

        cache.addAll( absenceStateCache.value)
        cache.add(absenceState.value)

        _absenceStateCache.value = cache
    }

    fun getSummary(): Triple<String, String, String> {
        val presence = "${attendanceCache.count { it.value == PRESENT }}"
        val lateness = "${attendanceCache.count { it.value == LATE }}"
        val absence = "${attendanceCache.count { it.value == ABSENT }}"

        return Triple(presence, lateness, absence)
    }

    fun clearCache() {
        attendanceCache.clear()
        attendanceBtnStateCache.clear()
        _absenceStateCache.value = emptyList()
    }

    fun refreshOnSave() {
        setAttendanceStep(ButtonStep.EDITING)
        viewModelScope.launch {
            attendanceEvents(eventDate.value)
        }
    }
}