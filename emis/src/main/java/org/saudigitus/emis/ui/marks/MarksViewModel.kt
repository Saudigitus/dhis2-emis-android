package org.saudigitus.emis.ui.marks

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.RowAction
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.local.repository.FormRepositoryImpl
import org.saudigitus.emis.data.model.EventTuple
import org.saudigitus.emis.ui.attendance.ButtonStep
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.utils.DateHelper
import javax.inject.Inject

@HiltViewModel
class MarksViewModel
@Inject constructor(
    private val repository: DataManager,
    private val formRepositoryImpl: FormRepositoryImpl
): BaseViewModel(repository) {

    private val viewModelState = MutableStateFlow(
        MarksUiState(
            toolbarHeaders = this.toolbarHeaders.value,
            students = this.teis.value
        )
    )

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value
        )

    private val _marksCache = MutableStateFlow<List<EventTuple>>(emptyList())
    val marksCache: StateFlow<List<EventTuple>> = _marksCache

    private val _programStage = MutableStateFlow("")
    private val programStage: StateFlow<String> = _programStage

    init {
        _toolbarHeaders.update {
            it.copy(
                title = "Marks",
                subtitle = DateHelper.formatDateWithWeekDay(this.eventDate.value)
            )
        }
        viewModelState.update { 
            it.copy(toolbarHeaders = this.toolbarHeaders.value,)
        }
    }

    override fun setConfig(program: String) {  }

    override fun setProgram(program: String) {  }

    override fun setDate(date: String) {
        _toolbarHeaders.update {
            it.copy(subtitle = DateHelper.formatDateWithWeekDay(date))
        }
        viewModelState.update {
            it.copy(toolbarHeaders = toolbarHeaders.value)
        }
    }

    override fun save() {
        viewModelScope.launch {
            if (buttonStep.value == ButtonStep.HOLD_SAVING) {
                setButtonStep(ButtonStep.SAVING)
            } else {
                marksCache.value.forEach { formRepositoryImpl.save(it) }
                setButtonStep(ButtonStep.HOLD_SAVING)
            }
        }
    }

    fun onClickNext(
        tei: String,
        ou: String,
        fieldData: Triple<String, String?, ValueType?>
    ) {
        val data = mutableListOf<EventTuple>()
        data.addAll(marksCache.value)

        val eventTuple = EventTuple(
            ou,
            program.value,
            programStage.value,
            tei,
            RowAction(
                id = fieldData.first,
                type = ActionType.ON_NEXT,
                value = fieldData.second,
                valueType = fieldData.third
            ),
            eventDate.value
        )

        data.add(eventTuple)

        _marksCache.value = data
    }

    fun getFields(stage: String, dl: String) {
        viewModelScope.launch {
            _programStage.value = stage
            viewModelState.update {
                it.copy(marksFields = formRepositoryImpl.keyboardInputTypeByStage(stage, dl))
            }
        }
    }

    fun updateTEISList() {
        viewModelState.update {
            it.copy(students = this.teis.value)
        }
    }

    fun marksState(
        key: String,
        dataElement: String,
        value: String,
        valueType: ValueType?
    ) {
        val junkData = mutableListOf<Field>()

        val formState = viewModelState.value.marksState

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
                    valueType = valueType
                )
            )
        } else {
            junkData.add(
                Field(
                    key = key,
                    dataElement = dataElement,
                    value = value,
                    valueType = valueType
                )
            )
        }

        viewModelState.update {
            it.copy(marksState = junkData)
        }
    }
}