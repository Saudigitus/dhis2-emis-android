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
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.dhis2.commons.Constants
import org.saudigitus.emis.AppRoutes
import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.ui.components.DropDown
import org.saudigitus.emis.ui.components.DropDownOu
import org.saudigitus.emis.ui.components.DropDownWithSelectionByCode
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.utils.getByType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onBack: () -> Unit,
    navTo: (route: String) -> Unit,
    onFilterClick: () -> Unit,
    onFilterItemClick: (FilterType, DropdownItem) -> Unit,
    onOUClick: (OU) -> Unit
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
                navigationAction = { onBack.invoke() },
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = false,
                    showFavorite = true,
                ),
                filterAction = onFilterClick::invoke,
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
                    uiState.dataElementFilters.getByType(FilterType.ACADEMIC_YEAR)?.let {
                        DropDownWithSelectionByCode(
                            dropdownState = it,
                            onItemClick = { item ->
                                onFilterItemClick(FilterType.ACADEMIC_YEAR, item)
                            },
                        )
                    }

                    DropDownOu(
                        placeholder = stringResource(R.string.school),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
                        selectedSchool = uiState.filterState.school,
                        program = uiState.programSettings?.getString(Constants.PROGRAM_UID) ?: "",
                        onItemClick = onOUClick::invoke,
                    )

                    uiState.dataElementFilters.getByType(FilterType.GRADE)?.let {
                        DropDown(
                            dropdownState = it,
                            onItemClick = { item ->
                                onFilterItemClick(FilterType.GRADE, item)
                            },
                        )
                    }

                    uiState.dataElementFilters.getByType(FilterType.SECTION)?.let {
                        DropDown(
                            dropdownState = it,
                            onItemClick = { item ->
                                onFilterItemClick(FilterType.SECTION, item)
                            },
                        )
                    }
                }
            }
            if(uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
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
                    onClick = { navTo.invoke(AppRoutes.TEI_LIST_ROUTE) },
                )

                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Adaptive(128.dp),
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
                            onClick = { navTo.invoke("${AppRoutes.ATTENDANCE_ROUTE}/${uiState.filterState.school?.uid}") },
                        )
                    }
                    if (!uiState.infoCard.isStaff) {
                        item {
                            HomeItem(
                                modifier = Modifier.fillMaxWidth(),
                                icon = painterResource(R.drawable.performance),
                                label = stringResource(R.string.performance),
                                enabled = uiState.infoCard.hasData(),
                                onClick = { navTo.invoke("${AppRoutes.SUBJECT_ROUTE}/${uiState.filterState.school?.uid}") },
                            )
                        }
                    }
                }
            }
        }
    }
}
