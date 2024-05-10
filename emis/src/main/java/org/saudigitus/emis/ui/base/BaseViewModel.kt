package org.saudigitus.emis.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.ui.attendance.ButtonStep
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.utils.Constants
import org.saudigitus.emis.utils.DateHelper

abstract class BaseViewModel(
    private val repository: DataManager,
) : ViewModel() {

    private val _teis = MutableStateFlow<List<SearchTeiModel>>(emptyList())
    val teis: StateFlow<List<SearchTeiModel>> = _teis

    private val _teiUIds = MutableStateFlow<List<String>>(emptyList())
    protected val teiUIds: StateFlow<List<String>> = _teiUIds

    protected val _toolbarHeaders = MutableStateFlow(
        ToolbarHeaders(
            title = "",
            subtitle = DateHelper.formatDateWithWeekDay("${DateHelper.formatDate(System.currentTimeMillis())}"),
        ),
    )
    val toolbarHeaders: StateFlow<ToolbarHeaders> = _toolbarHeaders

    private val _schoolCalendar = MutableStateFlow<CalendarConfig?>(null)
    val schoolCalendar: StateFlow<CalendarConfig?> = _schoolCalendar

    protected val _eventDate = MutableStateFlow(DateHelper.formatDate(System.currentTimeMillis()) ?: "")
    val eventDate: StateFlow<String> = _eventDate

    protected val _program = MutableStateFlow("")
    val program: StateFlow<String> = _program

    protected val _ou = MutableStateFlow("")
    val ou: StateFlow<String> = _ou

    private val _infoCard = MutableStateFlow(InfoCard())
    val infoCard: StateFlow<InfoCard> = _infoCard

    protected val _buttonStep = MutableStateFlow(ButtonStep.EDITING)
    val buttonStep: StateFlow<ButtonStep> = _buttonStep

    init {
        viewModelScope.launch {
            _schoolCalendar.value = repository.dateValidation(Constants.CALENDAR_KEY)
        }
    }

    protected abstract fun setConfig(program: String)
    abstract fun setProgram(program: String)
    abstract fun setDate(date: String)
    abstract fun save()

    fun setOU(ou: String) {
        _ou.value = ou
    }

    fun setTeis(teis: List<SearchTeiModel>) {
        _teis.value = teis
        _teiUIds.value = teis.mapNotNull { it.tei.uid() }
    }

    fun setTeis(
        teis: List<SearchTeiModel>,
        run: () -> Unit,
    ) {
        _teis.value = teis
        _teiUIds.value = teis.mapNotNull { it.tei.uid() }
        run()
    }

    fun setInfoCard(infoCard: InfoCard) {
        _infoCard.value = infoCard
    }

    fun setButtonStep(buttonStep: ButtonStep) {
        _buttonStep.value = buttonStep
    }
}
