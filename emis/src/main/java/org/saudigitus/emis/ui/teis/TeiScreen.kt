package org.saudigitus.emis.ui.teis

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.rounded.Book
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.DropDownAcademicYear
import org.saudigitus.emis.ui.components.DropDownClass
import org.saudigitus.emis.ui.components.DropDownGrade
import org.saudigitus.emis.ui.components.DropDownOu
import org.saudigitus.emis.ui.components.MetadataItem
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.components.ToolbarHeaders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeiScreen(
    viewModel: TeiViewModel,
    onBack: () -> Unit
) {
    var displayFilters by remember { mutableStateOf(false) }
    val academicYear by viewModel.academicYear.collectAsStateWithLifecycle()
    val grades by viewModel.grade.collectAsStateWithLifecycle()
    val sections by viewModel.section.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Toolbar(
                headers = ToolbarHeaders(
                    title = stringResource(R.string.app_name),
                    subtitle = ""
                ),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C98F0),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationAction = { onBack.invoke() },
                actionState = ToolbarActionState(syncVisibility = false),
                syncAction = {  },
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
                    DropDownAcademicYear(
                        placeholder = "Academic year...",
                        leadingIcon = Icons.Rounded.Book,
                        data = academicYear,
                        onItemClick = {}
                    )

                    DropDownOu(
                        placeholder = "School...",
                        leadingIcon = Icons.Default.LocationOn,
                        onItemClick = {}
                    )

                    DropDownGrade(
                        placeholder = "Grade...",
                        leadingIcon = Icons.Default.School,
                        data = grades,
                        onItemClick = {}
                    )

                    DropDownClass(
                        placeholder = "Class...",
                        leadingIcon = Icons.Default.Category,
                        data = sections,
                        onItemClick = {}
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
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start
            ) {
                LazyColumn(
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
                        )
                        .padding(top = 16.dp),
                ) {
                    items(listOf("Alpha Beta", "Omega Beta")) {
                        MetadataItem(displayName = it)
                    }
                }
            }
        }
    }
}