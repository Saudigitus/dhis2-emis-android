package org.saudigitus.emis.ui.attendance

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.date.DateUtils
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.local.FormRepository
import org.saudigitus.emis.data.model.Attendance
import org.saudigitus.emis.data.model.dto.Absence
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.form.Field
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

    private val viewModelState = MutableStateFlow(
        AttendanceUiState(
            toolbarHeaders = this.toolbarHeaders.value,
            students = this.teis.value,
        ),
    )
    val uiState = viewModelState.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        viewModelState.value,
    )

    private val attendanceCache = mutableSetOf<AttendanceEntity>()
    private var attendanceBtnStateCache = mutableListOf(AttendanceActionButtonState())

    private val _absenceState = MutableStateFlow(Absence())
    private val absenceState: StateFlow<Absence> = _absenceState

    private val _selectedAbsence = MutableStateFlow<Set<Pair<String, String>>>(emptySet())
    val selectedAbsence: StateFlow<Set<Pair<String, String>>> = _selectedAbsence

    init {
        _toolbarHeaders.update {
            it.copy(
                title = "Attendance",
            )
        }

        viewModelState.update {
            it.copy(toolbarHeaders = toolbarHeaders.value)
        }
    }

    override fun setConfig(program: String) {
        viewModelScope.launch {
            val config = repository.getConfig(KEY)?.find { it.program == program }

            if (config != null) {
                _datastoreAttendance.value = config.attendance
            }
            getAttendanceOptions(program)
            getReasonForAbsence(datastoreAttendance.value?.absenceReason ?: "")
            getFields(
                datastoreAttendance.value?.programStage ?: "",
                datastoreAttendance.value?.absenceReason ?: "",
            )
        }
    }

    override fun setProgram(program: String) {
        _program.value = program

        setConfig(program)

        viewModelScope.launch {
            attendanceEvents()
        }
    }

    fun updateTEISList() {
        viewModelState.update {
            it.copy(students = this.teis.value)
        }
    }

    fun setAttendanceStep(attendanceStep: ButtonStep) {
        viewModelState.update {
            it.copy(attendanceStep = attendanceStep)
        }
    }

    override fun setDate(date: String) {
        _eventDate.value = date
        viewModelScope.launch {
            attendanceEvents(date)
        }
        _toolbarHeaders.update {
            it.copy(
                subtitle = DateHelper.formatDateWithWeekDay(date),
            )
        }
        viewModelState.update {
            it.copy(toolbarHeaders = toolbarHeaders.value)
        }
    }

    private suspend fun getAttendanceOptions(
        program: String,
    ) {
        viewModelState.update {
            it.copy(attendanceOptions = repository.getAttendanceOptions(program))
        }
    }

    private suspend fun attendanceEvents(
        date: String? = DateHelper.formatDate(DateUtils.getInstance().today.time),
    ) {
        teis.collect {
            try {
                viewModelState.update {
                    it.copy(
                        attendanceStatus = repository.getAttendanceEvent(
                            program = program.value,
                            programStage = datastoreAttendance.value?.programStage ?: "",
                            dataElement = datastoreAttendance.value?.status ?: "",
                            reasonDataElement = datastoreAttendance.value?.absenceReason ?: "",
                            teis = teiUIds.value,
                            date = date.toString(),
                        ),
                    )
                }

                clearCache()
                attendanceCache.addAll(uiState.value.attendanceStatus)

                setInitialAttendanceStatus()
            } catch (e: Exception) {
                Timber.tag("ATTENDANCE_DATA").e(e)
            }
        }
    }

    private fun getFields(stage: String, dl: String) {
        viewModelScope.launch {
            viewModelState.update {
                it.copy(formFields = formRepository.keyboardInputTypeByStage(stage, dl))
            }
        }
    }

    private fun setInitialAttendanceStatus() {
        attendanceBtnStateCache = uiState.value.attendanceStatus.map { attendance ->
            attendanceActionButtonMapper(
                index = uiState.value.attendanceOptions.indexOfFirst { it.code == attendance.value },
                tei = attendance.tei,
                attendanceValue = attendance.value,
                containerColor = uiState.value.attendanceOptions.find {
                    it.code == attendance.value
                }?.color ?: Color.Black,
            )
        }.toMutableList()

        viewModelState.update {
            it.copy(attendanceBtnState = attendanceBtnStateCache)
        }
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

    private fun getReasonForAbsence(dataElement: String) {
        viewModelScope.launch {
            viewModelState.update {
                it.copy(reasonOfAbsence = repository.getOptions(null, null, dataElement))
            }
        }
    }

    fun setTeiAbsence(tei: String, value: String) {
        val cachedPos = mutableSetOf<Pair<String, String>>()
        cachedPos.addAll(selectedAbsence.value)

        cachedPos.add(Pair(tei, value))

        _selectedAbsence.value = cachedPos
    }

    fun bulkAttendance(
        index: Int,
        value: String,
        reasonOfAbsence: String? = null,
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            teiUIds.value.forEach {
                setAttendance(
                    index = index,
                    ou = ou.value,
                    tei = it,
                    value = value,
                    reasonOfAbsence = reasonOfAbsence,
                    hasPersisted = false,
                )
            }
        }
    }

    private fun removeTeiAbsence(tei: String) {
        val cachedPos = mutableSetOf<Pair<String, String>>()
        cachedPos.addAll(selectedAbsence.value)
        val data = cachedPos.find { it.first == tei }
        cachedPos.remove(data)
        _selectedAbsence.value = cachedPos
    }

    fun setAttendance(
        index: Int,
        ou: String,
        tei: String,
        value: String,
        reasonOfAbsence: String? = null,
        color: Color? = null,
        hasPersisted: Boolean = true,
    ) {
        removeTeiAbsence(tei)

        viewModelState.update {
            it.copy(attendanceBtnState = getAttendanceUiState(index, tei, value, color))
        }

        val attendance = AttendanceEntity(
            tei = tei,
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
        value: String? = null,
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
            reasonOfAbsence = absenceState.value.reasonOfAbsence,
        )

        val cache = mutableListOf<Absence>()

        cache.addAll(uiState.value.absence)
        cache.add(absenceState.value)

        viewModelState.update {
            it.copy(absence = cache)
        }
    }

    fun bulkSave() {
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
            attendanceEvents(eventDate.value)
        }
    }

    fun getSummary(): List<Triple<Int, ImageVector?, Color?>> {
        val summaries = uiState.value.attendanceOptions.map { Triple(it.code, it.icon, it.color) }
            .map { status ->
                val count = attendanceCache.count { it.value.equals(status.first, true) }

                Triple(count, status.second, status.third)
            }

        return summaries
    }

    fun fieldState(
        key: String,
        dataElement: String,
        value: String,
        valueType: ValueType?,
    ) {
        val junkData = mutableListOf<Field>()

        val formState = viewModelState.value.fieldsState

        if (formState.isNotEmpty()) {
            junkData.addAll(formState)
        }

        val index = junkData.indexOfFirst { it.key == key && it.dataElement == dataElement }

        if (index >= 0) {
            junkData.removeAt(index)
            junkData.add(
                index,
                Field(
                    key = key,
                    dataElement = dataElement,
                    value = value,
                    valueType = valueType,
                ),
            )
        } else {
            junkData.add(
                Field(
                    key = key,
                    dataElement = dataElement,
                    value = value,
                    valueType = valueType,
                ),
            )
        }

        viewModelState.update {
            it.copy(fieldsState = junkData)
        }
    }

    fun onClickNext(
        tei: String,
        ou: String,
        fieldData: Triple<String, String?, ValueType?>,
    ) {
        viewModelScope.launch {
            val data = selectedAbsence.value.find { it.first == tei }
            if (data != null) {
                val attendance = AttendanceEntity(
                    tei = tei,
                    dataElement = datastoreAttendance.value?.status ?: "",
                    value = data.second,
                    reasonDataElement = datastoreAttendance.value?.absenceReason,
                    reasonOfAbsence = fieldData.second,
                    date = eventDate.value,
                )

                repository.save(
                    ou = ou,
                    program = program.value,
                    programStage = datastoreAttendance.value?.programStage ?: "",
                    attendance = attendance,
                )
            }
        }
    }

    fun clearCache() {
        attendanceCache.clear()
        attendanceBtnStateCache.clear()
    }

    fun refreshOnSave() {
        setAttendanceStep(ButtonStep.EDITING)
        viewModelScope.launch {
            attendanceEvents(eventDate.value)
        }
    }
}
