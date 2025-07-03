package org.saudigitus.emis.ui.attendance

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.date.DateUtils
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.local.FormRepository
import org.saudigitus.emis.data.model.Attendance
import org.saudigitus.emis.data.model.Summary
import org.saudigitus.emis.data.model.dto.Absence
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.form.FormField
import org.saudigitus.emis.utils.Constants.KEY
import org.saudigitus.emis.utils.DateHelper
import org.saudigitus.emis.utils.Utils.WHITE
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel
@Inject constructor(
    private val repository: DataManager,
    private val formRepository: FormRepository,
) : BaseViewModel(repository) {

    private val _datastoreAttendance = MutableStateFlow<Attendance?>(null)
    private val datastoreAttendance: StateFlow<Attendance?> = _datastoreAttendance

    private val _attendanceOptions = MutableStateFlow<List<AttendanceOption>>(emptyList())
    val attendanceOptions: StateFlow<List<AttendanceOption>> = _attendanceOptions

    private val _attendanceStatus = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendanceStatus: StateFlow<List<AttendanceEntity>> = _attendanceStatus

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

    private val _formFields = MutableStateFlow<List<FormField>>(emptyList())
    val formFields: StateFlow<List<FormField>> = _formFields

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isOnlyAbsence = MutableStateFlow(false)
    private val isOnlyAbsence: StateFlow<Boolean> = _isOnlyAbsence

    private val _options = MutableStateFlow<List<String>>(emptyList())
    private val options: StateFlow<List<String>> = _options

    fun setDefaults(title: String, onlyAbsence: Boolean = false) {
        _toolbarHeaders.update {
            it.copy(
                title = title,
            )
        }
        _isOnlyAbsence.value = onlyAbsence
    }

    fun setOptions(academicYear: String, grade: String, section: String) {
        _options.value = listOf(academicYear, grade, section)
    }

    override fun setConfig(program: String) {
        viewModelScope.launch {
            val config = repository.getConfig(KEY)?.find { it.program == program }

            if (config != null) {
                _datastoreAttendance.value = config.attendance
            }
            getAttendanceOptions(program)
            getFields(
                datastoreAttendance.value?.programStage.orEmpty(),
                datastoreAttendance.value?.absenceReason.orEmpty(),
            )
        }
    }

    override fun setProgram(program: String) {
        _program.value = program

        setConfig(program)

        if (isOnlyAbsence.value) {
            geTeiByAttendanceStatus()
        } else {
            attendanceEvents()
        }
    }

    fun setAttendanceStep(attendanceStep: ButtonStep) {
        _attendanceStep.value = attendanceStep
    }

    override fun setDate(date: String) {
        _eventDate.value = date
        if (isOnlyAbsence.value) {
            geTeiByAttendanceStatus(date)
        } else {
            attendanceEvents(date)
        }
        _toolbarHeaders.update {
            it.copy(
                subtitle = DateHelper.formatDateWithWeekDay(date),
            )
        }
    }

    private suspend fun getAttendanceOptions(
        program: String,
    ) {
        _attendanceOptions.value = repository.getAttendanceOptions(program)
    }

    private fun getFields(stage: String, dl: String) {
        viewModelScope.launch {
            _formFields.value = formRepository.keyboardInputTypeByStage(program.value,stage, dl)
        }
    }

    private fun attendanceEvents(
        date: String? = DateHelper.formatDate(DateUtils.getInstance().today.time),
    ) {
        viewModelScope.launch {
            try {
                _attendanceStatus.value = async {
                    repository.getAttendanceEvent(
                        program = program.value,
                        programStage = datastoreAttendance.value?.programStage ?: "",
                        dataElement = datastoreAttendance.value?.status ?: "",
                        reasonDataElement = datastoreAttendance.value?.absenceReason ?: "",
                        teis = teiUIds.value.map { it.first },
                        date = date.toString(),
                    )
                }.await()

                clearCache()
                attendanceCache.addAll(attendanceStatus.value)

                setInitialAttendanceStatus()
            } catch (e: Exception) {
                if (e is CancellationException) {
                    Timber.tag("ATTENDANCE_DATA").d("Coroutine cancelled: ${e.message}")
                    throw e
                }
                Timber.tag("ATTENDANCE_DATA").e(e)
            }
        }
    }

    private fun geTeiByAttendanceStatus(
        date: String? = DateHelper.formatDate(DateUtils.getInstance().today.time),
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val config = repository.getConfig(KEY)?.find { it.program == program.value }
            val registration = config?.registration

            val response = async {
                repository.geTeiByAttendanceStatus(
                    ou = ou.value,
                    program = program.value,
                    stage = registration?.programStage ?: "",
                    attendanceStage = datastoreAttendance.value?.programStage ?: "",
                    attendanceDataElement = datastoreAttendance.value?.status ?: "",
                    reasonDataElement = datastoreAttendance.value?.absenceReason ?: "",
                    date = date,
                    dataElementIds = listOf(
                        "${registration?.academicYear}",
                        "${registration?.grade}",
                        "${registration?.section}",
                    ),
                    options = options.value,
                )
            }
            val data = response.await()

            setTeis(data.keys.toList())
            _attendanceStatus.value = data.values.toList()

            clearCache()
            attendanceCache.addAll(attendanceStatus.value)

            setInitialAttendanceStatus()

            _isLoading.value = false
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
                }?.color ?: Color.Black,
            )
        }.toMutableList()

        _attendanceBtnState.value = attendanceBtnStateCache
    }

    private fun attendanceActionButtonMapper(
        index: Int,
        tei: String,
        attendanceValue: String,
        containerColor: Color,
    ) = AttendanceActionButtonState(
        btnIndex = index,
        btnId = tei,
        iconTint = 0,
        buttonState = AttendanceButtonSettings(
            buttonType = attendanceValue,
            containerColor = containerColor,
            contentColor = WHITE,
        ),
    )

    private fun getAttendanceUiState(
        index: Int,
        tei: String,
        value: String,
        color: Color?,
    ): MutableList<AttendanceActionButtonState> {
        val uiCache = attendanceBtnStateCache.find { it.btnId == tei }

        val uiCacheItem = attendanceActionButtonMapper(
            index = index,
            tei = tei,
            attendanceValue = value,
            containerColor = color ?: Color.LightGray,
        )

        if (uiCache == null) {
            attendanceBtnStateCache.add(uiCacheItem)
        } else {
            attendanceBtnStateCache.remove(uiCache)
            attendanceBtnStateCache.add(uiCacheItem)
        }

        return attendanceBtnStateCache
    }

    fun bulkAttendance(
        index: Int,
        value: String,
        reasonOfAbsence: String? = null,
        color: Color? = null,
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            teiUIds.value.forEach {
                setAttendance(
                    index = index,
                    ou = ou.value,
                    tei = it.first,
                    enrollment = it.second,
                    value = value,
                    reasonOfAbsence = reasonOfAbsence,
                    color = color,
                    hasPersisted = false,
                )
            }
        }
    }

    fun setAttendance(
        index: Int,
        ou: String,
        tei: String,
        enrollment: String,
        value: String,
        reasonOfAbsence: String? = null,
        color: Color? = null,
        hasPersisted: Boolean = true,
    ) {
        _attendanceBtnState.value = getAttendanceUiState(index, tei, value, color)

        val attendance = AttendanceEntity(
            tei = tei,
            enrollment = enrollment,
            dataElement = datastoreAttendance.value?.status ?: "",
            value = value,
            reasonDataElement = datastoreAttendance.value?.absenceReason,
            reasonOfAbsence = reasonOfAbsence,
            date = eventDate.value,
        )

        val cacheItem = attendanceCache.find { it.tei == attendance.tei }

        if (cacheItem == null) {
            attendanceCache.add(attendance)
        } else {
            attendanceCache.remove(cacheItem)
            attendanceCache.add(attendance)
        }

        if (hasPersisted) {
            viewModelScope.launch {
                repository.save(
                    ou = ou,
                    program = program.value,
                    programStage = datastoreAttendance.value?.programStage ?: "",
                    attendance = attendance,
                )
            }
        }
    }

    fun setAbsence(
        index: Int? = null,
        ou: String? = null,
        tei: String? = null,
        enrollment: String? = null,
        value: String? = null,
        color: Color? = null,
        reasonOfAbsence: String? = null,
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
        if (enrollment != null) {
            _absenceState.update {
                it.copy(enrollment = enrollment)
            }
        }
        if (value != null) {
            _absenceState.update {
                it.copy(value = value)
            }
        }
        if (color != null) {
            _absenceState.update {
                it.copy(color = color)
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
            enrollment = absenceState.value.enrollment,
            tei = absenceState.value.tei,
            value = absenceState.value.value,
            color = absenceState.value.color,
            reasonOfAbsence = absenceState.value.reasonOfAbsence,
        )

        val cache = mutableListOf<Absence>()

        cache.addAll(absenceStateCache.value)
        cache.add(absenceState.value)

        _absenceStateCache.value = cache
    }

    fun bulkSave(
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            async {
                attendanceCache.forEach { attendance ->
                    repository.save(
                        ou = ou.value,
                        program = program.value,
                        programStage = datastoreAttendance.value?.programStage ?: "",
                        attendance = attendance,
                    )
                }
            }.await()

            clearCache()
            setAttendanceStep(ButtonStep.EDITING)
            if (isOnlyAbsence.value) {
                geTeiByAttendanceStatus(eventDate.value)
            } else {
                attendanceEvents(eventDate.value)
            }
            onSuccess()
        }
    }

    fun getSummary(): List<Summary> {
        val summaries = attendanceOptions.value.map { Pair(it.code, Triple(it.iconName, it.icon, it.color)) }
            .map { status ->

                val count = attendanceCache.count { it.value.equals(status.first, true) }

                Summary(
                    count,
                    status.second.first,
                    status.second.second,
                    status.second.third,
                )
            }

        return summaries
    }

    fun clearCache() {
        attendanceCache.clear()
        attendanceBtnStateCache.clear()
        _absenceStateCache.value = emptyList()
    }

    fun refreshOnSave() {
        setAttendanceStep(ButtonStep.EDITING)
        if (isOnlyAbsence.value) {
            geTeiByAttendanceStatus(eventDate.value)
        } else {
            attendanceEvents(eventDate.value)
        }
    }
}
