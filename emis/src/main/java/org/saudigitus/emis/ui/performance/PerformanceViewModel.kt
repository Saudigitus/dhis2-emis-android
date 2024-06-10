package org.saudigitus.emis.ui.performance

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
import org.saudigitus.emis.data.local.FormRepository
import org.saudigitus.emis.data.model.EventTuple
import org.saudigitus.emis.ui.attendance.ButtonStep
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.utils.DateHelper
import javax.inject.Inject

@HiltViewModel
class PerformanceViewModel
@Inject constructor(
    private val repository: DataManager,
    private val formRepository: FormRepository,
) : BaseViewModel(repository) {

    private val viewModelState = MutableStateFlow(
        PerformanceUiState(
            toolbarHeaders = this.toolbarHeaders.value,
            students = this.teis.value,
        ),
    )

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value,
        )

    private val _cache = MutableStateFlow<List<EventTuple>>(emptyList())
    val cache: StateFlow<List<EventTuple>> = _cache

    private val _programStage = MutableStateFlow("")
    private val programStage: StateFlow<String> = _programStage

    private val _dataElement = MutableStateFlow("")
    private val dataElement: StateFlow<String> = _dataElement

    private val _saveOnce = MutableStateFlow(0)
    private val saveOnce: StateFlow<Int> = _saveOnce

    init {
        _toolbarHeaders.update {
            it.copy(
                title = "Performance",
                subtitle = null,
            )
        }
        viewModelState.update {
            it.copy(toolbarHeaders = this.toolbarHeaders.value)
        }
    }

    override fun setConfig(program: String) {}

    override fun setProgram(program: String) {
        _program.value = program
    }

    fun loadSubjects(stage: String) {
        viewModelScope.launch {
            viewModelState.update {
                it.copy(subjects = repository.getSubjects(stage))
            }
        }
    }

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
                cache.value.forEach { formRepository.save(it) }
                setButtonStep(ButtonStep.HOLD_SAVING)
            }
        }
    }

    fun onClickNext(
        tei: String,
        ou: String,
        fieldData: Triple<String, String?, ValueType?>,
    ) {
        val data = mutableListOf<EventTuple>()
        data.addAll(cache.value)

        val eventTuple = EventTuple(
            ou,
            program.value,
            programStage.value,
            tei,
            RowAction(
                id = dataElement.value.ifEmpty { fieldData.first },
                type = ActionType.ON_NEXT,
                value = fieldData.second,
                valueType = fieldData.third,
            ),
            eventDate.value,
        )

        data.add(eventTuple)

        _cache.value = data
    }

    fun getFields(stage: String, dl: String) {
        viewModelScope.launch {
            _programStage.value = stage
            viewModelState.update {
                it.copy(formFields = formRepository.keyboardInputTypeByStage(stage, dl))
            }
        }
    }

    fun setDefault(
        stage: String,
        dl: String,
    ) {
        if (saveOnce.value == 0) {
            _saveOnce.value = 1
            viewModelScope.launch {
                _programStage.value = stage
                viewModelState.update {
                    it.copy(
                        formData = formRepository
                            .getEvents(
                                ou = ou.value,
                                program = program.value,
                                programStage = stage,
                                dataElement = dl,
                                teis = teiUIds.value,
                            ),
                    )
                }
            }
        }
    }

    fun updateDataFields(dl: String) {
        viewModelScope.launch {
            _dataElement.value = dl
            viewModelState.update {
                it.copy(
                    formData = formRepository
                        .getEvents(
                            ou = ou.value,
                            program = program.value,
                            programStage = programStage.value,
                            dataElement = dl,
                            teis = teiUIds.value,
                        ),
                )
            }
        }
    }

    fun updateTEISList() {
        viewModelState.update {
            it.copy(students = this.teis.value)
        }
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
}
