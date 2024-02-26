package org.saudigitus.emis.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dhis2.commons.Constants
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.DropDownOu
import org.saudigitus.emis.ui.components.FavoriteAlertDialog
import org.saudigitus.emis.ui.components.SumaryCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.ui.teis.TeiViewModel
import org.saudigitus.emis.utils.getByType
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveFavoriteFilterScreen(
    viewModel: TeiViewModel,
    favoriteViewModel: FavoriteViewModel,
    onBack: () -> Unit,
) {

    val context = LocalContext.current

    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val programSettings by viewModel.programSettings.collectAsStateWithLifecycle()
    val dataElementFilters by viewModel.dataElementFilters.collectAsStateWithLifecycle()
    val favorites by favoriteViewModel.favorites.collectAsStateWithLifecycle()

    var selectedStatesGrade = emptyList<MutableState<Boolean>>()
    var selectedStatesSection = emptyList<MutableState<Boolean>>()

    if(dataElementFilters.getByType(FilterType.GRADE)!!.data.isNotEmpty()){
        selectedStatesGrade =
            remember { List(dataElementFilters.getByType(FilterType.GRADE)!!.data.size) {
                mutableStateOf(false) }
            }
    }

    if(dataElementFilters.getByType(FilterType.SECTION)!!.data.isNotEmpty()) {
        selectedStatesSection =
            remember {
                List(dataElementFilters.getByType(FilterType.SECTION)!!.data.size) {
                    mutableStateOf(false)
                }
            }.toMutableList()
    }

    //val clearFavorite by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val hasGrade = remember { mutableStateOf(false) }
    val hasClass = remember { mutableStateOf(false) }

    /**  TODO("Remove this line, is not important to log in production") */
    Timber.tag("MY_FAVORITES").e("$favorites")

    if (filterState.school?.uid != null) {
        favoriteViewModel.setFavorite(
            schoolUid = filterState.school?.uid!!,
            school = filterState.school!!.displayName
        )
    }

    fun resetSelectedStates(selectedStates: List<MutableState<Boolean>>) {
        selectedStates.forEach { it.value = false }
    }

    Scaffold(
        topBar = {
            Toolbar(
                /**  TODO("Set string resource) */
                headers = ToolbarHeaders(title = "Favorites", subtitle = null),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C98F0),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationAction = { onBack.invoke() },
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = false,
                    filterVisibility = false,
                    showFavorite = false
                ),
                favoriteAction = {},
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    text = {
                        Text(
                            text = stringResource(R.string.reset),
                            color = Color(0xFF2C98F0),
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.rubik_medium))
                            )
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Rounded.CleaningServices,
                            contentDescription = null,
                            tint = Color(0xFF2C98F0)
                        )
                    },
                    onClick = {
                        showDialog.value = true
                    }
                )
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = stringResource(R.string.update),
                            color = Color(0xFF2C98F0),
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.rubik_medium))
                            )
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = Color(0xFF2C98F0)
                        )
                    },
                    onClick = {
                        if(filterState.school?.uid != null) {

                            if(hasGrade.value and hasClass.value){
                                favoriteViewModel.save()
                                resetSelectedStates(selectedStatesGrade)
                                resetSelectedStates(selectedStatesSection)
                                hasGrade.value = false
                                hasClass.value = false
                            } else {
                                favoriteViewModel.showToast(
                                    context = context,
                                    /**  TODO(" set string resource ) */
                                    message = "Please select both Grade and Class!"
                                )
                            }

                        }else{
                            favoriteViewModel.showToast(
                                context = context,
                                /**  TODO(" set string resource ") */
                                message = "Please select one School to save!"
                            )
                        }
                        showDialog.value = false
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // show alert dialog when the value is true
            if(showDialog.value){
                FavoriteAlertDialog(
                    /**  TODO(" set string resource ") */
                    title = "warning",
                    message = "Would you like to clear the saved favorites?",
                    onYes = {
                        favoriteViewModel.reset()
                        resetSelectedStates(selectedStatesGrade)
                        resetSelectedStates(selectedStatesSection)
                        hasGrade.value = false
                        hasClass.value = false
                    },
                    onDismiss = {
                        showDialog.value = true
                    },
                    openDialog = showDialog
                )
            }
            Column {
                Spacer(modifier = Modifier.size(20.dp))
                DropDownOu(
                    placeholder = stringResource(R.string.school),
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
                    selectedSchool = filterState.school,
                    program = programSettings?.getString(Constants.PROGRAM_UID) ?: " ",
                    onItemClick = {
                        viewModel.setSchool(it)
                        favoriteViewModel.setFavorite(
                            schoolUid = filterState.school?.uid ?: "",
                            school = filterState.school?.displayName ?: "",
                        )
                    }
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    /**  TODO("emove commented code") */
                    //Spacer(modifier = Modifier.size(6.dp))
                    Text(stringResource(R.string.gradeSection), color = Color.Gray)
                    //Spacer(modifier = Modifier.size(6.dp))
                    LazyRow {
                        itemsIndexed(dataElementFilters.getByType(FilterType.GRADE)!!.data) {
                                index, grade ->
                            TextChipWithIconVisibility(
                                isSelected = selectedStatesGrade[index],
                                "${grade.itemName}",
                                "${grade.code}",
                            ) { checked, code, displayName ->
                                resetSelectedStates(selectedStatesGrade) // reset the states
                                selectedStatesGrade[index].value = checked
                                /**  TODO(" remove the print ") */
                                println("CHECKED: ${checked} GRADE CODE: $code")

                                if(checked){
                                    hasGrade.value = true
                                    favoriteViewModel.setFavorite(gradeCode = code, gradeName = displayName)
                                } else {
                                    hasGrade.value = false
                                }
                            }
                        }
                    }
                    /**  TODO(" remove commented code ") */
                    //Spacer(modifier = Modifier.size(6.dp))
                    Text(stringResource(R.string.section), color = Color.Gray)
                    //Spacer(modifier = Modifier.size(6.dp))
                    LazyRow {
                        itemsIndexed(dataElementFilters.getByType(FilterType.SECTION)!!.data) { index, section ->
                            TextChipWithIconVisibility(
                                isSelected = selectedStatesSection[index],
                                /**  TODO("Remove sigle expressions") */
                                "${section.itemName}",
                                "${section.code}",
                            ) { checked, code, dispayName ->
                                /**  TODO("Remove commented code and printLn) */
                                //selectedStatesSection[index] = checked
                                println("CHECKED: ${checked} SECTION CODE: $code")
                                if(hasGrade.value){
                                    favoriteViewModel.setFavorite(sectionCode = code, sectionName = dispayName, isSelected = checked)
                                    hasClass.value = true
                                } else {
                                    hasClass.value = false
                                    resetSelectedStates(selectedStatesSection)
                                    favoriteViewModel.showToast(
                                        context = context,
                                        /**  TODO(" set string resource ") */
                                        message = "Please select the Grade!"
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(stringResource(R.string.summary), color = Color.Gray)

                    if (favorites.isEmpty()) {
                        Text(
                            stringResource(R.string.emptyFavorites),
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyColumn (modifier = Modifier.padding(bottom =  60.dp)) {
                            itemsIndexed(favorites.reversed()) { index, favorite ->
                                SumaryCard(
                                    school = favorite.school,
                                    streams = favorite.stream
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}