package org.saudigitus.emis.ui.teis

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.ui.components.DropDownItem
import org.saudigitus.emis.utils.Constants
import javax.inject.Inject

@HiltViewModel
class TeiViewModel
@Inject constructor(
    val repository: DataManager
) : ViewModel() {

    private val _academicYear = MutableStateFlow<List<DropDownItem>>(listOf())
    val academicYear: StateFlow<List<DropDownItem>> = _academicYear

    private val _grade = MutableStateFlow<List<DropDownItem>>(listOf())
    val grade: StateFlow<List<DropDownItem>> = _grade

    private val _section = MutableStateFlow<List<DropDownItem>>(listOf())
    val section: StateFlow<List<DropDownItem>> = _section

    init {
        viewModelScope.launch {
            val config = repository.getConfig(Constants.KEY)

            if (config != null) {
                var academic = ""
                var grade = ""
                var cls = ""

                for (c in config) {
                    academic = "${c.registration?.academicYear}"
                    grade = "${c.registration?.grade}"
                    cls = "${c.registration?.section}"
                    break
                }

                val optionsYears = repository.getOptions(academic).map {
                    DropDownItem(
                        leadingIcon = Icons.Default.Book,
                        id = it.uid(),
                        itemName =  "${it.displayName()}",
                        code = it.code()
                    )
                }

                val optionsGrade = repository.getOptions(grade).map {
                    DropDownItem(
                        leadingIcon = Icons.Default.Book,
                        id = it.uid(),
                        itemName =  "${it.displayName()}",
                        code = it.code()
                    )
                }

                val optionsCls = repository.getOptions(cls).map {
                    DropDownItem(
                        leadingIcon = Icons.Default.Book,
                        id = it.uid(),
                        itemName =  "${it.displayName()}",
                        code = it.code()
                    )
                }

                _academicYear.value = optionsYears
                _grade.value = optionsGrade
                _section.value = optionsCls
            }
        }
    }
}