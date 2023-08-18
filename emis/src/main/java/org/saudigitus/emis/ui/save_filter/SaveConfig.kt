package org.saudigitus.emis.ui.save_filter

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dhis2.commons.Constants
import org.saudigitus.emis.R
import org.saudigitus.emis.data.local.DataManager
import org.saudigitus.emis.ui.components.DropDownOu
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.MetadataItem
import org.saudigitus.emis.ui.components.SumaryCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.teis.FilterType
import org.saudigitus.emis.ui.teis.TeiViewModel


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
        ){ paddingValues -> Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
        ){
           Column {
               Spacer(modifier = Modifier.size(20.dp))
               DropDownOu(
                   placeholder = stringResource(R.string.school),
                   leadingIcon = ImageVector.vectorResource(R.drawable.ic_location_on),
                   selectedSchool = filterState.school,
                   program = programSettings?.getString(Constants.PROGRAM_UID) ?: " ",
                   onItemClick = {
                       viewModel.setSchool(it)
                   }
               )
               Column(modifier = Modifier
                   .fillMaxWidth()
                   .padding(16.dp)){
                   Spacer(modifier = Modifier.size(10.dp))
                   Text("Grade", color = Color.Gray)
                   Spacer(modifier = Modifier.size(10.dp))

                   LazyRow(){
                       items(dataElementFilters[FilterType.GRADE]!!) { grade ->
                           TextChipWithIconVisibility(
                               false,
                               "${grade.itemName}",
                               "${grade.code}",
                               onChecked = { checked, code ->
                                   println("CHECKED: ${checked} GRADE CODE: $code")
                               },
                           )
                       }
                   }
                   Spacer(modifier = Modifier.size(10.dp))
                   Text("Class/Section", color = Color.Gray)
                   Spacer(modifier = Modifier.size(10.dp))
                   LazyRow(){
                       items(dataElementFilters[FilterType.SECTION]!!) { section ->
                           TextChipWithIconVisibility(
                               false,
                               "${section.itemName}",
                               "${section.code}",
                               onChecked = { checked, code ->
                                   println("CHECKED: ${checked} SECTION CODE: $code")
                               },
                           )
                       }
                   }
                   Spacer(modifier = Modifier.size(10.dp))
                   Text("Summary", color = Color.Gray)
                   SumaryCard(
                       InfoCard(),
                   )
               }

           }
        }
    }
}