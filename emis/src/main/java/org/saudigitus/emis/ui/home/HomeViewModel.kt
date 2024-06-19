package org.saudigitus.emis.ui.home

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants.DATA_SET_NAME
import org.dhis2.commons.Constants.PROGRAM_UID
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.model.DefaultConfig
import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.data.model.Registration
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.ui.components.DropdownState
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.ui.teis.FilterState
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.utils.Constants
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val repository: DataManager,
) : BaseViewModel(repository) {

    private val _dataElementFilters = MutableStateFlow<List<DropdownState>>(emptyList())
    val dataElementFilters: StateFlow<List<DropdownState>> = _dataElementFilters

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    private val _registration = MutableStateFlow<Registration?>(null)
    private val registration: StateFlow<Registration?> = _registration

    private val _defaultConfig = MutableStateFlow<DefaultConfig?>(null)
    val defaultConfig: StateFlow<DefaultConfig?> = _defaultConfig

    private val _programSettings = MutableStateFlow<Bundle?>(null)
    val programSettings: StateFlow<Bundle?> = _programSettings

    private val _toolbarHeader = MutableStateFlow(ToolbarHeaders(""))
    val toolbarHeader: StateFlow<ToolbarHeaders> = _toolbarHeader

    private val _schoolOptions = MutableStateFlow<List<DropdownItem>>(emptyList())
    val schoolOptions: StateFlow<List<DropdownItem>> = _schoolOptions

    private val _gradeOptions = MutableStateFlow<List<DropdownItem>>(emptyList())
    val gradeOptions: StateFlow<List<DropdownItem>> = _gradeOptions

    override fun setConfig(program: String) {
        viewModelScope.launch {
            val config = repository.getConfig(Constants.KEY)?.find { it.program == program }

            if (config != null) {
                _defaultConfig.value = config.default
                _registration.value = config.registration
                _filterState.update {
                    it.copy(key = config.key)
                }

                _dataElementFilters.value = listOf(
                    DropdownState(
                        FilterType.ACADEMIC_YEAR,
                        getDataElementName("${registration.value?.academicYear}"),
                        options("${registration.value?.academicYear}"),
                    ),
                    DropdownState(
                        FilterType.GRADE,
                        getDataElementName("${registration.value?.grade}"),
                        options("${registration.value?.grade}"),
                    ),
                    DropdownState(
                        FilterType.SECTION,
                        getDataElementName("${registration.value?.section}"),
                        options("${registration.value?.section}"),
                    ),
                )
            }
        }
    }

    override fun setProgram(program: String) {
        _program.value = program
    }

    override fun setDate(date: String) {}
    override fun save() {}

    private suspend fun getDataElementName(uid: String) =
        repository.getDataElement(uid)?.displayFormName() ?: ""

    private fun getTeis() {
        viewModelScope.launch {
            if (!filterState.value.isNull()) {
                setTeis(
                    repository.getTeisBy(
                        ou = "${filterState.value.school?.uid}",
                        program = "${programSettings.value?.getString(PROGRAM_UID)}",
                        stage = "${registration.value?.programStage}",
                        dataElementIds = listOf(
                            "${registration.value?.academicYear}",
                            "${registration.value?.grade}",
                            "${registration.value?.section}",
                        ),
                        options = filterState.value.options(),
                    ),
                )

                setInfoCard(
                    InfoCard(
                        grade = filterState.value.grade?.itemName ?: "",
                        section = filterState.value.section?.itemName ?: "",
                        academicYear = filterState.value.academicYear?.itemName ?: "",
                        orgUnitName = filterState.value.school?.displayName ?: "",
                        teiCount = teis.value.size,
                        isStaff = filterState.value.isStaff(),
                    ),
                )
            }
        }
    }

    fun setBundle(bundle: Bundle?) {
        _programSettings.value = bundle

        setConfig(programSettings.value?.getString(PROGRAM_UID) ?: "")

        _toolbarHeader.update {
            it.copy(title = "${programSettings.value?.getString(DATA_SET_NAME)}")
        }
    }

    fun setAcademicYear(academicYear: DropdownItem?) {
        _filterState.update {
            it.copy(academicYear = academicYear)
        }

        val subtitle = if (filterState.value.school?.displayName != null) {
            "${academicYear?.itemName} | ${filterState.value.school?.displayName}"
        } else {
            academicYear?.itemName
        }

        _toolbarHeader.update {
            it.copy(subtitle = subtitle)
        }
        getTeis()
    }

    fun setSchool(ou: OU?) {
        viewModelScope.launch {
            _filterState.update {
                it.copy(school = ou)
            }
            setOU("${ou?.uid}")

            val subtitle = if (filterState.value.academicYear?.itemName != null) {
                "${filterState.value.academicYear?.itemName} | ${ou?.displayName}"
            } else {
                ou?.displayName
            }

            _toolbarHeader.update {
                it.copy(subtitle = subtitle)
            }

            val filters = dataElementFilters.value.toMutableList()
            filters.removeAt(1)
            filters.add(
                index = 1,
                DropdownState(
                    FilterType.GRADE,
                    getDataElementName("${registration.value?.grade}"),
                    options("${registration.value?.grade}"),
                ),
            )

            _dataElementFilters.value = filters
        }

        getTeis()
    }

    fun setGrade(grade: DropdownItem?) {
        _filterState.update {
            it.copy(grade = grade)
        }
        getTeis()
    }

    fun setSection(section: DropdownItem?) {
        _filterState.update {
            it.copy(section = section)
        }
        getTeis()
    }

    private suspend fun options(uid: String) = repository.getOptions(
        ou = filterState.value.school?.uid,
        program = program.value,
        dataElement = uid,
    )
}
