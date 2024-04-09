package org.saudigitus.emis.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dhis2.commons.Constants
import org.saudigitus.emis.AppRoutes
import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.ui.components.DropDown
import org.saudigitus.emis.ui.components.DropDownOu
import org.saudigitus.emis.ui.components.DropDownWithSelectionByCode
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.utils.getByType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    navToTeiList: () -> Unit,
    navTo: (route: String) -> Unit
) {

    var displayFilters by remember { mutableStateOf(true) }
    val dataElementFilters by viewModel.dataElementFilters.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val toolbarHeaders by viewModel.toolbarHeader.collectAsStateWithLifecycle()
    val programSettings by viewModel.programSettings.collectAsStateWithLifecycle()
    val infoCard by viewModel.infoCard.collectAsStateWithLifecycle()
    val defaultConfig by viewModel.defaultConfig.collectAsStateWithLifecycle()

    val schoolOptions by viewModel.schoolOptions.collectAsStateWithLifecycle()
    val gradeOptions by viewModel.gradeOptions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Toolbar(
                headers = toolbarHeaders,
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
                    showFavorite = true
                ),
                filterAction = { displayFilters = !displayFilters }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFF2C98F0))
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            AnimatedVisibility(visible = displayFilters) {
                Column(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start
                ) {
                    DropDownWithSelectionByCode(
                        placeholder = dataElementFilters.getByType(FilterType.ACADEMIC_YEAR)?.displayName
                            ?: stringResource(R.string.academic_year),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_book),
                        data = dataElementFilters.getByType(FilterType.ACADEMIC_YEAR)?.data,
                        selectedCodeItem = filterState.academicYear?.code ?: defaultConfig?.currentAcademicYear ?: "",
                        onItemClick = viewModel::setAcademicYear
                    )

                    if(schoolOptions.isNotEmpty()) {
                        DropDown(
                            placeholder = stringResource(R.string.school),
                            leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
                            data =  schoolOptions,
                            selectedItemName =  schoolOptions[0].itemName,
                            onItemClick = {
                                viewModel.setSchool(OU(uid= it.id, displayName = it.itemName))
                            }
                        )
                    } else {
                        DropDownOu(
                            placeholder = stringResource(R.string.school),
                            leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
                            selectedSchool = filterState.school,
                            program = programSettings?.getString(Constants.PROGRAM_UID) ?: "",
                            onItemClick =  viewModel::setSchool
                        )
                    }

                    DropDown(
                        placeholder = dataElementFilters.getByType(FilterType.GRADE)?.displayName
                            ?: stringResource(R.string.grade),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_school),
                        data = gradeOptions.ifEmpty { dataElementFilters.getByType(FilterType.GRADE)?.data },
                        selectedItemName = filterState.grade?.itemName ?: "",
                        onItemClick = viewModel::setGrade
                    )

                    DropDown(
                        placeholder = dataElementFilters.getByType(FilterType.SECTION)?.displayName
                            ?: stringResource(R.string.cls),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_category),
                        data = dataElementFilters.getByType(FilterType.SECTION)?.data,
                        selectedItemName = filterState.section?.itemName ?: "",
                        onItemClick = viewModel::setSection
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.White,
                        shape = MaterialTheme.shapes.medium
                            .copy(
                                topStart = CornerSize(16.dp),
                                topEnd = CornerSize(16.dp),
                                bottomStart = CornerSize(0.dp),
                                bottomEnd = CornerSize(0.dp)
                            )
                    ),
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                ShowCard(
                    infoCard = infoCard,
                    onClick = navToTeiList
                )

                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(128.dp),
                    contentPadding = PaddingValues(vertical = 5.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    item {
                        HomeItem(
                            modifier = Modifier.fillMaxWidth(),
                            icon = painterResource(R.drawable.s_calendar),
                            label = stringResource(R.string.attendance),
                            syncTime = "2 hours ago",
                            onClick = { navTo.invoke(AppRoutes.ATTENDANCE_ROUTE) }
                        )
                    }
                    item {
                        HomeItem(
                            modifier = Modifier.fillMaxWidth(),
                            icon = painterResource(R.drawable.s_calendar),
                            label = stringResource(R.string.absenteeism),
                            syncTime = "2 hours ago",
                            onClick = { navTo.invoke(AppRoutes.ABSENTEEISM_ROUTE) }
                        )
                    }
                    item {
                        HomeItem(
                            modifier = Modifier.fillMaxWidth(),
                            icon = painterResource(R.drawable.performance),
                            label = stringResource(R.string.performance),
                            syncTime = "2 hours ago",
                            onClick = { navTo.invoke(AppRoutes.SUBJECT_ROUTE) }
                        )
                    }
                }
            }
        }
    }
}