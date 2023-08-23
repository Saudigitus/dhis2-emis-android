package org.saudigitus.emis.ui.save_filter

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dhis2.commons.Constants
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.DropDownOu
import org.saudigitus.emis.ui.components.SumaryCard
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.ui.teis.TeiViewModel
import timber.log.Timber


/*@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(){
    SaveFavoriteFilterScreen(
        //viewModel = null,
        onBack= {},
    )
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveFavoriteFilterScreen(
    viewModel: TeiViewModel,
    favoriteViewModel: FavoriteViewModel,
    onBack: () -> Unit,
) {

    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val programSettings by viewModel.programSettings.collectAsStateWithLifecycle()
    val dataElementFilters by viewModel.dataElementFilters.collectAsStateWithLifecycle()
    val favorites by favoriteViewModel.favorites.collectAsStateWithLifecycle()

    val selectedStatesGrade =
        remember { List(dataElementFilters[FilterType.GRADE]!!.size) { mutableStateOf(false) } }
    val selectedStatesSection =
        remember { List(dataElementFilters[FilterType.SECTION]!!.size) { mutableStateOf(false) } }.toMutableList()

    Timber.tag("MY_FAVORITES").e("$favorites")

    if (filterState.school?.uid != null) {
        favoriteViewModel.setFavorite(
            schoolUid = filterState.school?.uid!!,
            school = filterState.school!!.displayName
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C98F0),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    Text(
                        "Favorite Filter",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
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
                        favoriteViewModel.reset()
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
                        favoriteViewModel.save()
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
                            school = filterState.school?.displayName ?: ""
                        )
                    }
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(stringResource(R.string.gradeSection), color = Color.Gray)
                    Spacer(modifier = Modifier.size(10.dp))

                    LazyRow {
                        itemsIndexed(dataElementFilters[FilterType.GRADE]!!) { index, grade ->
                            TextChipWithIconVisibility(
                                isSelected = selectedStatesGrade[index],
                                "${grade.itemName}",
                                "${grade.code}",
                            ) { checked, code ->
                                selectedStatesGrade[index].value = checked
                                println("CHECKED: ${checked} GRADE CODE: $code")
                                favoriteViewModel.setFavorite(gradeCode = code)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(stringResource(R.string.section), color = Color.Gray)
                    Spacer(modifier = Modifier.size(10.dp))
                    LazyRow {
                        itemsIndexed(dataElementFilters[FilterType.SECTION]!!) { index, section ->
                            TextChipWithIconVisibility(
                                isSelected = selectedStatesSection[index],
                                "${section.itemName}",
                                "${section.code}",
                            ) { checked, code ->
                                //selectedStatesSection[index] = checked
                                println("CHECKED: ${checked} SECTION CODE: $code")
                                favoriteViewModel.setFavorite(sectionCode = code)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(stringResource(R.string.summary), color = Color.Gray)
                    /*SumaryCard(
                        //InfoCard(),
                    )*/

                    if (favorites.isEmpty()) {
                        Text(
                            stringResource(R.string.emptyFavorites),
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        LazyColumn {
                            itemsIndexed(favorites) { index, favorite ->
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