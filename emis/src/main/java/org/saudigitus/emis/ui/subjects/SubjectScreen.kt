package org.saudigitus.emis.ui.subjects

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
import androidx.compose.material.icons.filled.Pin
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
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.DropDown
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen(
    state: SubjectUIState,
    onBack: () -> Unit,
    onFilterClick: (String) -> Unit,
    onClick: (stage: String, dl: String) -> Unit
) {

    var displayFilters by remember { mutableStateOf(true) }
    var selectedFilters by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Toolbar(
                headers = state.toolbarHeaders,
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
                    filterVisibility = true,
                    showCalendar = false
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
                    DropDown(
                        placeholder = stringResource(R.string.select_term),
                        leadingIcon = Icons.Default.Pin,
                        data = state.filters,
                        selectedItemName = state.filters.getOrNull(0)?.itemName ?: "",
                        onItemClick = {
                            selectedFilters = it.id
                            onFilterClick.invoke(it.id)
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                        .padding(vertical = 12.dp),
                ) {
                    items(state.subjects) { subject ->
                        SubjectItem(
                            displayName = subject.displayName ?: "-",
                            onClick = { onClick.invoke(selectedFilters, subject.uid) }
                        )
                    }
                }
            }
        }
    }
}
