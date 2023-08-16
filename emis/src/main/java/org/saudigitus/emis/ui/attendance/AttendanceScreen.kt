package org.saudigitus.emis.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.MetadataItem
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.utils.Constants.ABSENT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    onBack: () -> Unit
) {
    val students by viewModel.teis.collectAsStateWithLifecycle()
    val attendanceOptions by viewModel.attendanceOptions.collectAsStateWithLifecycle()
    val attendanceBtnState by viewModel.attendanceBtnState.collectAsStateWithLifecycle()
    val attendanceStep by viewModel.attendanceStep.collectAsStateWithLifecycle()
    val attendanceStatus by viewModel.attendanceStatus.collectAsStateWithLifecycle()
    val toolbarHeaders by viewModel.toolbarHeaders.collectAsStateWithLifecycle()
    val infoCard by viewModel.infoCard.collectAsStateWithLifecycle()
    val reasonOfAbsence by viewModel.reasonOfAbsence.collectAsStateWithLifecycle()
    val absence by viewModel.absenceStateCache.collectAsStateWithLifecycle()

    var isAbsent by remember { mutableStateOf(false) }
    var isAttendanceCompleted by remember { mutableStateOf(false) }

    var cachedTEIId by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    if (isAbsent) {
        ReasonForAbsenceDialog(
            reasons = reasonOfAbsence,
            title = stringResource(R.string.reason_absence),
            themeColor = Color(0xFF2C98F0),
            selectedItemCode = if (absence.find { it.tei == cachedTEIId } != null) {
                absence.find { it.tei == cachedTEIId }!!.reasonOfAbsence
            } else null,
            onItemClick = {
                viewModel.setAbsence(reasonOfAbsence = it.code)
            },
            onCancel = {
                isAbsent = false
            },
            onDone = {
                isAbsent = false
                viewModel.saveAbsenceState()
            }
        )
    }

    if (attendanceStep == ButtonStep.SAVING) {
        AttendanceSummaryDialog(
            title = stringResource(R.string.attendance_summary),
            data = viewModel.getSummary(),
            themeColor = Color(0xFF2C98F0),
            onCancel = { viewModel.setAttendanceStep(ButtonStep.HOLD_SAVING) }
        ) {
            viewModel.clearCache()
            viewModel.setAttendanceStep(ButtonStep.EDITING)
            isAttendanceCompleted = true
        }
    }

    if (isAttendanceCompleted) {
        LaunchedEffect(key1 = snackbarHostState) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.attendance_saved),
                duration = SnackbarDuration.Short
            )

            delay(200)
            onBack.invoke()
        }
    }

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
                    filterVisibility = false,
                    showCalendar = true
                ),
                calendarAction = {
                    viewModel.setAttendanceDate(it)
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                   Text(
                       text = if (attendanceStep == ButtonStep.EDITING) {
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
                       imageVector = if (attendanceStep == ButtonStep.EDITING) {
                           Icons.Default.Edit
                       } else {
                           Icons.Default.Save
                       },
                       contentDescription = null,
                       tint = Color(0xFF2C98F0)
                   )
                },
                onClick = {
                    if (attendanceStep == ButtonStep.HOLD_SAVING) {
                        viewModel.setAttendanceStep(ButtonStep.SAVING)
                    } else {
                        viewModel.setAttendanceStep(ButtonStep.HOLD_SAVING)
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
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
                            }",
                            enableClickAction = false,
                            onClick = {}
                        ) {
                            if (attendanceStep == ButtonStep.EDITING) {
                                AttendanceItemState(
                                    tei = student.tei.uid(),
                                    attendanceState = attendanceStatus
                                )
                            } else {
                                AttendanceButtons(
                                    tei = student.tei.uid(),
                                    btnState = attendanceBtnState,
                                    actions = attendanceOptions
                                ) { index, tei, attendance ->
                                    if (attendance != ABSENT) {
                                        viewModel.setAttendance(
                                            index = index,
                                            ou = student.tei.organisationUnit() ?: "",
                                            tei = tei ?: student.tei.uid(),
                                            value = attendance
                                        )
                                    } else {
                                        cachedTEIId = tei ?: student.tei.uid()
                                        isAbsent = true
                                        viewModel.setAbsence(
                                            index = index,
                                            ou = student.tei.organisationUnit() ?: "",
                                            tei = tei ?: student.tei.uid(),
                                            value = attendance
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}