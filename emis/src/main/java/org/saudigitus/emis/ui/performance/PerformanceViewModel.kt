package org.saudigitus.emis.ui.performance

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.local.repository.FormRepositoryImpl
import org.saudigitus.emis.data.model.Performance
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.utils.Constants
import org.saudigitus.emis.utils.DateHelper
import javax.inject.Inject

@HiltViewModel
class PerformanceViewModel
@Inject constructor(
    private val repository: DataManager,
    private val formRepositoryImpl: FormRepositoryImpl
): BaseViewModel(repository) {

    private val _datastorePerformance = MutableStateFlow<Performance?>(null)
    private val datastorePerformance: StateFlow<Performance?> = _datastorePerformance

    private val viewModelState = MutableStateFlow(
        PerformanceUiState(
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

    init {
        _toolbarHeaders.update {
            it.copy(
                title = "Performance",
                subtitle = DateHelper.formatDateWithWeekDay(this.eventDate.value)
            )
        }
        viewModelState.update { 
            it.copy(toolbarHeaders = this.toolbarHeaders.value,)
        }
    }

    override fun setConfig(program: String) {
        viewModelScope.launch {
            val config = repository.getConfig(Constants.KEY)?.find { it.program == program }

            if (config != null) {
                _datastorePerformance.value = config.performance
            }
        }
    }

    override fun setProgram(program: String) {
       setConfig(program)
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
        TODO("Not yet implemented")
    }

    fun onClickNext(
        tei: String,
        ou: String,
        fieldData: Triple<String, String?, ValueType?>
    ) {
        save()
    }

    private fun getFields(stage: String) {
        viewModelScope.launch {
            viewModelState.update {
                it.copy(formFields = formRepositoryImpl.keyboardInputTypeByStage(stage))
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
        valueType: ValueType?
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
            it.copy(fieldsState = junkData)
        }
    }
}