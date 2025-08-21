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
import org.saudigitus.emis.data.model.DefaultConfig
import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.data.model.Registration
import org.saudigitus.emis.helper.ISEMISSync
import org.saudigitus.emis.ui.base.BaseViewModel
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.ui.components.DropdownState
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.utils.Constants
import javax.inject.Inject


private data class FilterSpec(
    val type: FilterType,
    val idProvider: Registration.() -> String?
)


@HiltViewModel
class HomeViewModel
@Inject constructor(
    private val repository: DataManager,
    private val semisSync: ISEMISSync,
) : BaseViewModel(repository) {

    private val _registration = MutableStateFlow<Registration?>(null)
    private val registration: StateFlow<Registration?> = _registration

    private val _isFilterOpened = MutableStateFlow(true)
    private val isFilterOpened: StateFlow<Boolean> = _isFilterOpened

    private val viewModelState = MutableStateFlow(
        HomeUiState(
            toolbarHeaders = this.toolbarHeaders.value,
        ),
    )

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value,
        )

    override fun setConfig(program: String) {
    }

    private suspend fun loadFiltersSequentially(
        defaultConfig: DefaultConfig? = null
    ) {
        val specs = listOf(
            FilterSpec(FilterType.ACADEMIC_YEAR) { academicYear },
            FilterSpec(FilterType.GRADE)       { grade },
            FilterSpec(FilterType.SECTION)     { section }
        )

        val results = mutableListOf<DropdownState>()

        specs.forEach { spec ->
            val elementId = registration.value?.run(spec.idProvider).orEmpty()
            if (elementId.isBlank()) return@forEach

            val options = options(elementId)
            val displayName = getDataElementName(elementId)

            val state = DropdownState(
                spec.type,
                null,
                null,
                displayName,
                options,
            )

            results.add(state)
        }
        viewModelState.update {
            it.copy(dataElementFilters = results)
        }
        setAcademicYear(
            results.find { it.filterType == FilterType.ACADEMIC_YEAR }
                ?.data?.find { it.code == defaultConfig?.currentAcademicYear },
        )
    }


    override fun setProgram(program: String) {
        _program.value = program

        viewModelScope.launch {
            val config = repository.getConfig(Constants.KEY)?.find { it.program == program }

            if (config != null) {
                val defaultConfig = config.default
                _registration.value = config.registration
                viewModelState.update {
                    it.copy(
                        key = config.key,
                        trackedEntityType = repository.getTrackedEntityType(program).orEmpty(),
                        modules = config.modules
                    )
                }
                loadFiltersSequentially(defaultConfig)
            }
        }
    }

    override fun setDate(date: String) {}
    override fun save() {}

    private suspend fun getDataElementName(uid: String) =
        repository.getDataElement(uid)?.displayFormName() ?: ""

    private fun getTeis() {
        viewModelScope.launch {
            if (!viewModelState.value.isNull) {
                val dataElements = listOfNotNull(
                    registration.value?.academicYear,
                    registration.value?.grade,
                    registration.value?.section,
                )

                repository.getTeisBy(
                    ou = "${viewModelState.value.school?.uid}",
                    program = "${uiState.value.programSettings?.getString(PROGRAM_UID)}",
                    stage = "${registration.value?.programStage}",
                    dataElementIds = dataElements,
                    dataValues = viewModelState.value.options,
                ).collect { teiList ->
                    setTeis(teiList)
                }

                setInfoCard(
                    viewModelState.updateAndGet {
                        it.copy(
                            isLoading = false,
                            infoCard = InfoCard(
                                grade = viewModelState.value.grade?.itemName ?: "",
                                section = viewModelState.value.section?.itemName ?: "",
                                academicYear = viewModelState.value.academicYear?.itemName ?: "",
                                orgUnitName = viewModelState.value.school?.displayName ?: "",
                                teiCount = teis.value.size,
                                isStaff = viewModelState.value.isStaff,
                            ),
                        )
                    }.infoCard,
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
                programSettings = bundle,
            )
        }
    }

    private fun <T>updateToolbar(obj: T?): ToolbarHeaders {
        return when (obj) {
            is DropdownItem -> {
                val subtitle = if (viewModelState.value.school?.displayName != null) {
                    "${obj.itemName} | ${viewModelState.value.school?.displayName}"
                } else {
                    obj.itemName
                }

                _toolbarHeaders.update {
                    it.copy(subtitle = subtitle)
                }

                toolbarHeaders.value
            }
            is OU -> {
                val subtitle = if (viewModelState.value.academicYear?.itemName != null) {
                    "${viewModelState.value.academicYear?.itemName} | ${obj.displayName}"
                } else {
                    obj.displayName
                }

                _toolbarHeaders.update {
                    it.copy(subtitle = subtitle)
                }

                toolbarHeaders.value
            }
            else -> {
                toolbarHeaders.value
            }
        }
    }

    private fun setAcademicYear(academicYear: DropdownItem?) {
        viewModelState.update {
            it.copy(
                toolbarHeaders = updateToolbar(academicYear),
                academicYear = academicYear,
            )
        }
        invokeInFilters()
    }

    private suspend fun reloadFilters(): MutableList<DropdownState> {
        val filters = uiState.value.dataElementFilters.toMutableList()

        if (filters.size > 1) {
            filters.removeAt(1)
            filters.add(
                index = 1,
                DropdownState(
                    FilterType.GRADE,
                    null,
                    null,
                    getDataElementName("${registration.value?.grade}"),
                    options("${registration.value?.grade}"),
                ),
            )
        }

        return filters
    }

    private fun setSchool(ou: OU?) {
        viewModelScope.launch {
            setOU("${ou?.uid}")

            viewModelState.update {
                it.copy(
                    toolbarHeaders = updateToolbar(ou),
                    school = ou,
                )
            }
            val updatedFilters = async { reloadFilters() }.await()
            viewModelState.update { it.copy(dataElementFilters = updatedFilters) }
        }
        invokeInFilters()
    }

    private fun setGrade(grade: DropdownItem?) {
        viewModelState.update {
            it.copy(grade = grade)
        }
        invokeInFilters()
    }

    private fun setSection(section: DropdownItem?) {
        viewModelState.update {
            it.copy(section = section)
        }
        invokeInFilters()
    }

    private suspend fun options(uid: String) = repository.getOptions(
        ou = viewModelState.value.school?.uid,
        program = program.value,
        dataElement = uid,
    )

    private fun closeFilterSection() {
        if (infoCard.value.hasData() && isFilterOpened.value) {
            viewModelState.update {
                it.copy(displayFilters = false)
            }
        }
    }

    private fun onFilterClick() {
        _isFilterOpened.value = !isFilterOpened.value
        viewModelState.update {
            it.copy(displayFilters = !it.displayFilters)
        }
    }

    private fun <T>onFilterItemClick(filterType: FilterType, filterItem: T) {
        when (filterType) {
            FilterType.ACADEMIC_YEAR -> {
                setAcademicYear(filterItem as DropdownItem)
            }
            FilterType.GRADE -> {
                setGrade(filterItem as DropdownItem)
            }
            FilterType.SECTION -> {
                setSection(filterItem as DropdownItem)
            }
            FilterType.SCHOOL -> {
                setSchool(filterItem as OU)
            }
            FilterType.NONE -> {}
        }
    }

    fun onUiEvent(homeUiEvent: HomeUiEvent) {
        when (homeUiEvent) {
            is HomeUiEvent.HideShowFilter -> {
                onFilterClick()
            }
            is HomeUiEvent.OnFilterChange<*> -> {
                onFilterItemClick(homeUiEvent.filterType, homeUiEvent.obj)
            }
            is HomeUiEvent.OnDownloadStudent -> {
                viewModelScope.launch {
                    val dataElementIds = listOf(
                        registration.value?.academicYear,
                        registration.value?.grade,
                        registration.value?.section,
                    ).mapNotNull { it }

                    val dataValues = viewModelState.value.options

                    if (dataValues.isNotEmpty() && program.value.isNotEmpty()) {
                        semisSync.downloadTEIsByUids(
                            ou = viewModelState.value.school?.uid.orEmpty(),
                            program = program.value,
                            dataElementIds = dataElementIds,
                            dataValues = dataValues,
                        )
                    }
                }
            }
            else -> {}
        }
    }

    private fun invokeInFilters() {
        closeFilterSection()
        getTeis()
    }
}
