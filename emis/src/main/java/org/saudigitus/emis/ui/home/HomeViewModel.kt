package org.saudigitus.emis.ui.home

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants.DATA_SET_NAME
import org.dhis2.commons.Constants.PROGRAM_UID
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.data.model.Registration
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.ui.components.DropdownState
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.teis.FilterState
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.utils.Constants
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val repository: DataManager,
) : BaseViewModel(repository) {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    private val _registration = MutableStateFlow<Registration?>(null)
    private val registration: StateFlow<Registration?> = _registration


    private val viewModelState = MutableStateFlow(
        HomeUiState(
            toolbarHeaders = this.toolbarHeaders.value,
            filterState = filterState.value
        ),
    )

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value,
        )

    override fun setConfig(program: String) {
        viewModelScope.launch {
            val config = repository.getConfig(Constants.KEY)?.find { it.program == program }

            if (config != null) {
                val defaultConfig = config.default
                _registration.value = config.registration
                _filterState.update {
                    it.copy(key = config.key)
                }

                val filterData = listOf(
                    DropdownState(
                        FilterType.ACADEMIC_YEAR,
                        null,
                        null,
                        getDataElementName("${registration.value?.academicYear}"),
                        options("${registration.value?.academicYear}"),
                        filterState.value.academicYear?.code ?: defaultConfig?.currentAcademicYear ?: "",
                    ),
                    DropdownState(
                        FilterType.GRADE,
                        null,
                        null,
                        getDataElementName("${registration.value?.grade}"),
                        options("${registration.value?.grade}"),
                        filterState.value.grade?.itemName ?: "",
                    ),
                    DropdownState(
                        FilterType.SECTION,
                        null,
                        null,
                        getDataElementName("${registration.value?.section}"),
                        options("${registration.value?.section}"),
                        filterState.value.section?.itemName ?: "",
                    ),
                )

                viewModelState.update {
                    it.copy(dataElementFilters = filterData)
                }
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
                val teiList = async {
                    repository.getTeisBy(
                        ou = "${filterState.value.school?.uid}",
                        program = "${uiState.value.programSettings?.getString(PROGRAM_UID)}",
                        stage = "${registration.value?.programStage}",
                        dataElementIds = listOf(
                            "${registration.value?.academicYear}",
                            "${registration.value?.grade}",
                            "${registration.value?.section}",
                        ),
                        options = filterState.value.options(),
                    )
                }.await()

                setTeis(teiList)

                setInfoCard(
                    viewModelState.updateAndGet {
                        it.copy(
                            isLoading = false,
                            infoCard = InfoCard(
                                grade = filterState.value.grade?.itemName ?: "",
                                section = filterState.value.section?.itemName ?: "",
                                academicYear = filterState.value.academicYear?.itemName ?: "",
                                orgUnitName = filterState.value.school?.displayName ?: "",
                                teiCount = teis.value.size,
                                isStaff = filterState.value.isStaff(),
                            )
                        )
                    }.infoCard
                )
            }
        }
    }

    fun setBundle(bundle: Bundle?) {
        setConfig(bundle?.getString(PROGRAM_UID) ?: "")

        _toolbarHeaders.update {
            it.copy(title = "${bundle?.getString(DATA_SET_NAME)}")
        }
        viewModelState.update {
            it.copy(
                toolbarHeaders = toolbarHeaders.value,
                programSettings = bundle
            )
        }
    }

    private fun setAcademicYear(academicYear: DropdownItem?) {
        _filterState.update {
            it.copy(academicYear = academicYear)
        }

        val subtitle = if (filterState.value.school?.displayName != null) {
            "${academicYear?.itemName} | ${filterState.value.school?.displayName}"
        } else {
            academicYear?.itemName
        }

        _toolbarHeaders.update {
            it.copy(subtitle = subtitle)
        }
        viewModelState.update {
            it.copy(
                toolbarHeaders = toolbarHeaders.value,
                filterState = filterState.value
            )
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

            _toolbarHeaders.update {
                it.copy(subtitle = subtitle)
            }

            val filters = uiState.value.dataElementFilters.toMutableList()
            filters.removeAt(1)
            filters.add(
                index = 1,
                DropdownState(
                    FilterType.GRADE,
                    null,
                    null,
                    getDataElementName("${registration.value?.grade}"),
                    options("${registration.value?.grade}"),
                    filterState.value.grade?.itemName ?: "",
                ),
            )

            viewModelState.update {
                it.copy(
                    toolbarHeaders = toolbarHeaders.value,
                    dataElementFilters = filters,
                    filterState = filterState.value
                )
            }
        }
        getTeis()
    }

    private fun setGrade(grade: DropdownItem?) {
        _filterState.update {
            it.copy(grade = grade)
        }
        viewModelState.update {
            it.copy(filterState = filterState.value)
        }
        getTeis()
    }

    private fun setSection(section: DropdownItem?) {
        _filterState.update {
            it.copy(section = section)
        }
        viewModelState.update {
            it.copy(filterState = filterState.value)
        }
        getTeis()
    }

    private suspend fun options(uid: String) = repository.getOptions(
        ou = filterState.value.school?.uid,
        program = program.value,
        dataElement = uid,
    )

    fun onFilterClick() {
        if (viewModelState.value.displayFilters) {
            viewModelState.update {
                it.copy(displayFilters = false)
            }
        } else {
            viewModelState.update {
                it.copy(displayFilters = true)
            }
        }
    }

    fun onFilterItemClick(filterType: FilterType, filterItem: DropdownItem) {
        when (filterType) {
            FilterType.ACADEMIC_YEAR -> {
                setAcademicYear(filterItem)
            }
            FilterType.GRADE -> {
                setGrade(filterItem)
            }
            FilterType.SECTION -> {
                setSection(filterItem)
            }
            else -> {}
        }
    }
}
