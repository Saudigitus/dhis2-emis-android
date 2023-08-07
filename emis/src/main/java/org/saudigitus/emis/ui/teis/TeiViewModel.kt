package org.saudigitus.emis.ui.teis

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants.DATA_SET_NAME
import org.dhis2.commons.Constants.PROGRAM_UID
import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.data.model.Registration
import org.saudigitus.emis.ui.components.DropDownItem
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.utils.Constants
import javax.inject.Inject

@HiltViewModel
class TeiViewModel
@Inject constructor(
    val repository: DataManager
) : ViewModel() {

    private val _dataElementFilters = MutableStateFlow<Map<FilterType, List<DropDownItem>>>(mapOf())
    val dataElementFilters: StateFlow<Map<FilterType, List<DropDownItem>>> = _dataElementFilters

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    private val _teis = MutableStateFlow<List<SearchTeiModel>>(listOf())
    val teis: StateFlow<List<SearchTeiModel>> = _teis

    private val _registration = MutableStateFlow<Registration?>(null)
    private val registration: StateFlow<Registration?> = _registration

    private val _programSettings = MutableStateFlow<Bundle?>(null)
    val programSettings: StateFlow<Bundle?> = _programSettings

    private val _toolbarHeader = MutableStateFlow(ToolbarHeaders(""))
    val toolbarHeader: StateFlow<ToolbarHeaders> = _toolbarHeader

    init {
        viewModelScope.launch {
            val config = repository.getConfig(Constants.KEY)

            if (config != null) {

                for (c in config) {
                    _registration.value = c.registration
                    break
                }

                _dataElementFilters.value = mapOf(
                    Pair(FilterType.ACADEMIC_YEAR, options("${registration.value?.academicYear}")),
                    Pair(FilterType.GRADE, options("${registration.value?.grade}")),
                    Pair(FilterType.SECTION, options("${registration.value?.section}")),
                )
            }
        }
    }

    private fun getTeis() {
        viewModelScope.launch {
            if (!filterState.value.isNull()) {
                _teis.value = repository.getTeisBy(
                    ou = "${filterState.value.school?.uid}",
                    program = "${programSettings.value?.getString(PROGRAM_UID)}",
                    stage = "${registration.value?.programStage}",
                    dataElementIds = listOf(
                        "${registration.value?.academicYear}",
                        "${registration.value?.grade}",
                        "${registration.value?.section}",
                    ),
                    options = listOf(
                        "${filterState.value.academicYear?.code}",
                        "${filterState.value.grade?.code}",
                        "${filterState.value.section?.code}",
                    )
                )
            }
        }
    }

    fun setBundle(bundle: Bundle?) {
        _programSettings.value = bundle
        _toolbarHeader.update {
            it.copy(title = "${programSettings.value?.getString(DATA_SET_NAME)}")
        }
    }

    fun setAcademicYear(academicYear: DropDownItem?) {
        _filterState.update {
            it.copy(academicYear = academicYear)
        }
        _toolbarHeader.update {
            it.copy(subtitle = academicYear?.itemName)
        }
        getTeis()
    }

    fun setSchool(ou: OU?) {
        _filterState.update {
            it.copy(school = ou)
        }
        val academicYear = toolbarHeader.value.subtitle
        _toolbarHeader.update {
            it.copy(subtitle = "$academicYear | ${ou?.displayName}")
        }

        getTeis()
    }

    fun setGrade(grade: DropDownItem?) {
        _filterState.update {
            it.copy(grade = grade)
        }
        getTeis()
    }

    fun setSection(section: DropDownItem?) {
        _filterState.update {
            it.copy(section = section)
        }
        getTeis()
    }

    private suspend fun options(uid: String) = repository.getOptions(uid).map {
        DropDownItem(
            id = it.uid(),
            itemName =  "${it.displayName()}",
            code = it.code()
        )
    }
}