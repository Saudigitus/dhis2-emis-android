package org.saudigitus.emis.ui.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.attendance.ButtonStep
import org.saudigitus.emis.ui.components.DetailsWithOptions
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.MetadataItem
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.theme.light_success

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    state: PerformanceUiState,
    onNavBack: () -> Unit,
    infoCard: InfoCard,
    defaultSelection: String = "",
    setPerformanceState: (
        key: String,
        dataElement: String,
        value: String,
        valueType: ValueType?
    ) -> Unit,
    setDate: (String) -> Unit,
    performanceStats: Pair<String, String>,
    onNext: (
        tei: String,
        ou: String,
        fieldData: Triple<String, String?, ValueType?>
    ) -> Unit,
    performanceStep: ButtonStep,
    step: (ButtonStep) -> Unit,
    onFilterClick: (dataElement: String) -> Unit,
    onSave: () -> Unit,
    dateValidator: (Long) -> Boolean = { true }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isCompleted by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (performanceStep == ButtonStep.SAVING) {
        PerformanceSummaryDialog(
            title = stringResource(R.string.performance_summary),
            data = performanceStats,
            themeColor = Color(0xFF2C98F0),
            onCancel = { step.invoke(ButtonStep.HOLD_SAVING) }
        ) {
            onSave()
            isCompleted = true
        }
    }

    if (isCompleted) {
        LaunchedEffect(key1 = snackbarHostState) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.marks_saved),
                duration = SnackbarDuration.Short
            )
        }
    }

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
                navigationAction = onNavBack,
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = false,
                    filterVisibility = false,
                    showCalendar = true
                ),
                calendarAction = setDate,
                dateValidator = dateValidator
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = if (performanceStep == ButtonStep.EDITING) {
                            stringResource(R.string.update)
                        } else {
                            stringResource(R.string.save)
                        },
                        color = Color(0xFF2C98F0),
                        style = LocalTextStyle.current.copy(
                            fontFamily = FontFamily(Font(R.font.rubik_medium))
                        )
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (performanceStep == ButtonStep.EDITING) {
                            Icons.Default.Edit
                        } else {
                            Icons.Default.Save
                        },
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
                DetailsWithOptions(
                    modifier = Modifier.fillMaxWidth(),
                    infoCard = infoCard,
                    placeholder = stringResource(R.string.subject),
                    leadingIcon = ImageVector.vectorResource(R.drawable.ic_category),
                    trailingIcon = Icons.TwoTone.Edit,
                    data = state.subjects,
                    defaultSelection = defaultSelection,
                    onItemClick = { onFilterClick.invoke(it.uid) }
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 108.dp)
                ) {
                    items(state.students) { student ->
                        MetadataItem(
                            displayName = "${
                                student.attributeValues?.values?.toList()?.getOrNull(2)?.value() ?: "-"
                            } ${student.attributeValues?.values?.toList()?.getOrNull(1)?.value() ?: ""}",
                            attrValue = student.attributeValues?.values?.toList()?.getOrNull(0)?.value() ?: "-",
                            enableClickAction = performanceStep == ButtonStep.HOLD_SAVING,
                            onClick = {}
                        ) {
                            PerformanceForm(
                                modifier = Modifier
                                    .fillMaxWidth(.3f)
                                    .padding(bottom = 2.dp)
                                    .align(Alignment.End),
                                state = state.fieldsState,
                                key = student.uid(),
                                fields = state.formFields,
                                formData = state.formData,
                                onNext = {
                                     onNext.invoke(
                                         student.uid(),
                                         student.tei.organisationUnit() ?: "",
                                         it
                                     )
                                },
                                setFormState = setPerformanceState
                            )
                        }
                    }
                }
            }
        }
    }
}
