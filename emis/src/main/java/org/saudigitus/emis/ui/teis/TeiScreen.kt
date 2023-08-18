package org.saudigitus.emis.ui.teis

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants
import org.saudigitus.emis.AppRoutes
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.DropDown
import org.saudigitus.emis.ui.components.DropDownOu
import org.saudigitus.emis.ui.components.MetadataItem
import org.saudigitus.emis.ui.components.NoResults
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import timber.log.Timber

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeiScreen(
    viewModel: TeiViewModel,
    onBack: () -> Unit,
    navToFavorite: () -> Unit,
    navTo: () -> Unit

) {
    var displayFilters by remember { mutableStateOf(true) }
    val dataElementFilters by viewModel.dataElementFilters.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val students by viewModel.teis.collectAsStateWithLifecycle()
    val toolbarHeaders by viewModel.toolbarHeader.collectAsStateWithLifecycle()
    val programSettings by viewModel.programSettings.collectAsStateWithLifecycle()
    val infoCard by viewModel.infoCard.collectAsStateWithLifecycle()

    val snackState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if(filterState.isNotNull()){
        LaunchedEffect(snackState) {
            coroutineScope.launch {
                snackState.showSnackbar("Save the filter as Favorite?")
            }
        }
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackState
            ) { snackbarData: SnackbarData ->
                CustomSnackBar(
                    navTo = navToFavorite,
                    snackbarData.message,
                    containerColor = Color.Gray
                )
            }
        },
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
                actionState = ToolbarActionState(syncVisibility = false),
                syncAction = {  },
                filterAction = { displayFilters = !displayFilters }
            )
        },
        floatingActionButton = {
            if(filterState.isNotNull() && students.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = stringResource(R.string.attendance),
                            color = Color(0xFF2C98F0),
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.rubik_medium))
                            )
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF2C98F0)
                        )
                    },
                    onClick = { navTo.invoke() }
                )
            }
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
                    DropDown(
                        placeholder = stringResource(R.string.academic_year),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_book),
                        data = dataElementFilters[FilterType.ACADEMIC_YEAR],
                        selectedItemName = filterState.academicYear?.itemName ?: "",
                        onItemClick = {
                            viewModel.setAcademicYear(it)
                        }
                    )

                    DropDownOu(
                        placeholder = stringResource(R.string.school),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
                        selectedSchool = filterState.school,
                        program = programSettings?.getString(Constants.PROGRAM_UID) ?: "",
                        onItemClick = {
                            viewModel.setSchool(it)
                        }
                    )

                    DropDown(
                        placeholder = stringResource(R.string.grade),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_school),
                        data = dataElementFilters[FilterType.GRADE],
                        selectedItemName = filterState.grade?.itemName ?: "",
                        onItemClick = {
                            viewModel.setGrade(it)
                        }
                    )

                    DropDown(
                        placeholder = stringResource(R.string.cls),
                        leadingIcon = ImageVector.vectorResource(R.drawable.ic_category),
                        data = dataElementFilters[FilterType.SECTION],
                        selectedItemName = filterState.section?.itemName ?: "",
                        onItemClick = {
                            viewModel.setSection(it)
                        }
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
                if (filterState.isNull() && students.isEmpty()) {
                    NoResults(message = stringResource(R.string.start_search))
                } else if (!filterState.isNull() && students.isEmpty()) {
                    NoResults(message = stringResource(R.string.search_no_results))
                } else {
                    ShowCard(infoCard)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(students) { student ->
                            MetadataItem(
                                displayName = "${
                                    student.attributeValues?.values?.toList()?.get(2)?.value()
                                } ${student.attributeValues?.values?.toList()?.get(1)?.value()}",
                                attrValue = "${
                                    student.attributeValues?.values?.toList()?.get(0)?.value()
                                }"
                            )
                        }
                    }
                }
            }
        }

    }
}

@Preview
@Composable
fun TestSnackBar(){
    CustomSnackBar(
        navTo = {

        },
        "Save the filter as Favorite?",
        containerColor = Color.Gray
    )
}


@Composable
fun CustomSnackBar(
    navTo: () -> Unit,
    message: String,
    containerColor: Color = Color.Black
) {
    val navController = rememberNavController()

    Snackbar( contentColor = containerColor) {
        CompositionLocalProvider(
            LocalLayoutDirection provides  LayoutDirection.Ltr
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(message,  color = Color.White)
                TextButton(
                    onClick = {
                        // Handle the action when clicked
                        println("SAVE button clicked in Snackbar")
                        navTo.invoke()
                        //navController.navigate(AppRoutes.FAVORITE_ROUTE)
                    }
                ) {
                    Text("SAVE", color = Color(0xFF2C98F0))
                }
            }
        }
    }
}