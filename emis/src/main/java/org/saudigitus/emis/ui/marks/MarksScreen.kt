package org.saudigitus.emis.ui.marks

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.dhis2.commons.data.SearchTeiModel
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.MetadataItem
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.components.ToolbarHeaders
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.ui.form.FormData
import org.saudigitus.emis.ui.form.FormField
import org.saudigitus.emis.ui.theme.light_success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksScreen(
    state: MarksUiState,
    onNavBack: () -> Unit,
    setMarksState: (
        key: String,
        dataElement: String,
        value: String,
        valueType: ValueType?
    ) -> Unit,
    onNext: (Triple<String, String?, ValueType?>) -> Unit,
    onSave: () -> Unit,
    dateValidator: (Long) -> Boolean = { true }
) {
    val snackbarHostState = remember { SnackbarHostState() }


    Scaffold(
        topBar = {
            Toolbar(
                headers = (state as MarksUiState.Screen).toolbarHeaders,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C98F0),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationAction = onNavBack,
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = false,
                    filterVisibility = false,
                    showCalendar = true
                ),
                calendarAction = state::setDate,
                dateValidator = dateValidator
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = stringResource(R.string.save),
                        color = Color(0xFF2C98F0),
                        style = LocalTextStyle.current.copy(
                            fontFamily = FontFamily(Font(R.font.rubik_medium))
                        )
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color(0xFF2C98F0)
                    )
                },
                onClick = onSave
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    containerColor = light_success,
                    contentColor = Color.White
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.success_icon),
                            contentDescription = it.visuals.message
                        )

                        Text(
                            text = it.visuals.message,
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.rubik_regular))
                            )
                        )
                    }
                }
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
                //ShowCard(infoCard)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items((state as MarksUiState.Screen).students) { student ->
                        MetadataItem(
                            displayName = "${
                                student.attributeValues?.values?.toList()?.get(2)?.value()
                            } ${student.attributeValues?.values?.toList()?.get(1)?.value()}",
                            attrValue = "${
                                student.attributeValues?.values?.toList()?.get(0)?.value()
                            }",
                            enableClickAction = false,
                            onClick = {}
                        ) {
                            MarksForm(
                                state = (state as MarksUiState.MarksForm).marksState,
                                key = state.marksKey,
                                fields = state.marksFields,
                                formData = state.marksData,
                                onNext = onNext,
                                setFormState = setMarksState
                            )
                        }
                    }
                }
            }
        }
    }
}
