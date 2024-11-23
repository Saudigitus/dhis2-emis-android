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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.dhis2.commons.Constants
import org.saudigitus.emis.AppRoutes
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.DropDown
import org.saudigitus.emis.ui.components.DropDownOu
import org.saudigitus.emis.ui.components.DropDownWithSelectionByCode
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.utils.getByType

@Composable
fun HomeRoute(
    isExpandedScreen: Boolean,
    viewModel: HomeViewModel,
    navController: NavHostController,
    navBack: () -> Unit,
    sync: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeUI(isExpandedScreen, uiState = uiState) {
        when (it) {
            is HomeUiEvent.OnBack -> navBack()
            is HomeUiEvent.NavTo -> navController.navigate(it.route)
            is HomeUiEvent.Sync -> sync()
            else -> {
                viewModel.onUiEvent(it)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUI(
    isExpandedScreen: Boolean,
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            Toolbar(
                headers = uiState.toolbarHeaders,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C98F0),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                navigationAction = { onEvent(HomeUiEvent.OnBack) },
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = true,
                    showFavorite = true,
                ),
                filterAction = {
                    onEvent(HomeUiEvent.HideShowFilter)
                },
                syncAction = { onEvent(HomeUiEvent.Sync) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFF2C98F0))
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            AnimatedVisibility(visible = uiState.displayFilters) {
                Column(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                    horizontalAlignment = Alignment.Start,
                ) {
                    DropDownWithSelectionByCode(
                        dropdownState = uiState.dataElementFilters.getByType(FilterType.ACADEMIC_YEAR),
                        defaultSelection = uiState.filterSelection.first,
                        onItemClick = { item ->
                            onEvent(HomeUiEvent.OnFilterChange(FilterType.ACADEMIC_YEAR, item))
                        },
                    )

                    DropDownOu(
                        placeholder = stringResource(R.string.school),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
                        selectedSchool = uiState.school,
                        program = uiState.programSettings?.getString(Constants.PROGRAM_UID) ?: "",
                        onItemClick = {
                            onEvent(HomeUiEvent.OnFilterChange(FilterType.SCHOOL, it))
                        },
                    )

                    DropDown(
                        dropdownState = uiState.dataElementFilters.getByType(FilterType.GRADE),
                        defaultSelection = uiState.filterSelection.second,
                        onItemClick = { item ->
                            onEvent(HomeUiEvent.OnFilterChange(FilterType.GRADE, item))
                        },
                    )

                    DropDown(
                        dropdownState = uiState.dataElementFilters.getByType(FilterType.SECTION),
                        defaultSelection = uiState.filterSelection.third,
                        onItemClick = { item ->
                            onEvent(HomeUiEvent.OnFilterChange(FilterType.SECTION, item))
                        },
                    )
                }
            }
            if (!uiState.infoCard.hasData()) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
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
                                bottomEnd = CornerSize(0.dp),
                            ),
                    ),
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
            ) {
                ShowCard(
                    infoCard = uiState.infoCard,
                    onClick = { onEvent(HomeUiEvent.NavTo(AppRoutes.TEI_LIST_ROUTE)) },
                )

                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = if (!isExpandedScreen) {
                        GridCells.Adaptive(128.dp)
                    } else {
                        GridCells.Adaptive(200.dp)
                    },
                    contentPadding = PaddingValues(vertical = 5.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterHorizontally,
                    ),
                ) {
                    item {
                        HomeItem(
                            modifier = Modifier.fillMaxWidth(),
                            icon = painterResource(R.drawable.s_calendar),
                            label = stringResource(R.string.attendance),
                            enabled = uiState.infoCard.hasData(),
                            onClick = {
                                onEvent(HomeUiEvent.NavTo("${AppRoutes.ATTENDANCE_ROUTE}/${uiState.school?.uid}"))
                            },
                        )
                    }
                    if (!uiState.isStaff) {
                        item {
                            HomeItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = painterResource(R.drawable.performance),
                                label = stringResource(R.string.performance),
                                enabled = uiState.infoCard.hasData(),
                                onClick = { onEvent(HomeUiEvent.NavTo("${AppRoutes.SUBJECT_ROUTE}/${uiState.school?.uid}")) },
                            )
                        }
                    }
                }
            }
        }
    }
}
