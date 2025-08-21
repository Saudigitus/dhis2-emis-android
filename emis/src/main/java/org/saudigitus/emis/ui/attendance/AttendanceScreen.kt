package org.saudigitus.emis.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.dhis2.ui.theme.colorPrimary
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.mapper.map
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.teis.mapper.TEICardMapper
import org.saudigitus.emis.ui.theme.light_success
import org.saudigitus.emis.utils.DateHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel,
    teiCardMapper: TEICardMapper,
    infoCard: InfoCard,
    onBack: () -> Unit,
    sync: () -> Unit,
) {
    val students by viewModel.teis.collectAsStateWithLifecycle()
    val attendanceOptions by viewModel.attendanceOptions.collectAsStateWithLifecycle()
    val attendanceBtnState by viewModel.attendanceBtnState.collectAsStateWithLifecycle()
    val attendanceStep by viewModel.attendanceStep.collectAsStateWithLifecycle()
    val attendanceStatus by viewModel.attendanceStatus.collectAsStateWithLifecycle()
    val toolbarHeaders by viewModel.toolbarHeaders.collectAsStateWithLifecycle()
    val schoolCalendar by viewModel.schoolCalendar.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val formFields by viewModel.formFields.collectAsStateWithLifecycle()
    val fieldState by viewModel.fieldState.collectAsStateWithLifecycle()
    val formData by viewModel.formData.collectAsStateWithLifecycle()

    var isAttendanceCompleted by remember { mutableStateOf(false) }
    var launchBulkAssign by remember { mutableStateOf(false) }
    var isBulk by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    if (attendanceStep == ButtonStep.SAVING) {
        AttendanceSummaryDialog(
            title = stringResource(R.string.attendance_summary),
            data = viewModel.getSummary(),
            themeColor = colorPrimary,
            disableActions = isAttendanceCompleted,
            onCancel = { viewModel.setAttendanceStep(ButtonStep.HOLD_SAVING) },
        ) {
            isAttendanceCompleted = true
            if (isBulk) {
                viewModel.bulkSave {
                    isAttendanceCompleted = false
                    isBulk = false
                }
            } else {
                viewModel.clearCache()
                viewModel.refreshOnSave()
            }
        }
    }

    if (isAttendanceCompleted) {
        LaunchedEffect(key1 = snackbarHostState) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.attendance_saved),
                duration = SnackbarDuration.Short,
            )
        }
    }

    if (launchBulkAssign) {
        BulkAssignComponent(
            onDismissRequest = { launchBulkAssign = false },
            attendanceOptions = attendanceOptions,
            onAttendanceStatus = { status ->
                isBulk = true
                viewModel.bulkAttendance(
                    index = status.first,
                    value = status.second,
                    color = status.third,
                )
                launchBulkAssign = false
            },
            onClear = viewModel::clearCache,
            onCancel = { launchBulkAssign = false },
        )
    }

    Scaffold(
        topBar = {
            Toolbar(
                headers = toolbarHeaders,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorPrimary,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                navigationAction = onBack::invoke,
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = true,
                    filterVisibility = false,
                    showCalendar = true,
                ),
                calendarAction = viewModel::setDate,
                dateValidator = {
                    val date = DateHelper.stringToLocalDate(DateHelper.formatDate(it)!!)
                    val today = System.currentTimeMillis()

                    if (schoolCalendar != null) {
                        (
                            !DateHelper.isWeekend(date) && schoolCalendar?.weekDays?.saturday == false &&
                                schoolCalendar?.weekDays?.sunday == false
                            ) &&
                            schoolCalendar?.holidays?.let { holiday ->
                                DateHelper.isHoliday(holiday, it)
                            } == true && it <= today
                    } else {
                        it <= today
                    }
                },
                syncAction = sync,
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
                        color = colorPrimary,
                        style = LocalTextStyle.current.copy(
                            fontFamily = FontFamily(Font(R.font.rubik_medium)),
                        ),
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
                        tint = colorPrimary,
                    )
                },
                onClick = {
                    if (attendanceStep == ButtonStep.HOLD_SAVING) {
                        isAttendanceCompleted = false
                        viewModel.setAttendanceStep(ButtonStep.SAVING)
                    } else {
                        viewModel.setAttendanceStep(ButtonStep.HOLD_SAVING)
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    containerColor = light_success,
                    contentColor = Color.White,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.success_icon),
                            contentDescription = it.visuals.message,
                        )

                        Text(
                            text = it.visuals.message,
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.rubik_regular)),
                            ),
                        )
                    }
                }
            }
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ShowCard(
                    infoCard,
                    false,
                    enabledIconButton = attendanceStep == ButtonStep.HOLD_SAVING,
                    onIconClick = { launchBulkAssign = true },
                )

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = colorPrimary,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 108.dp),
                    ) {
                        itemsIndexed(students) { _, student ->
                            val card = student.map(teiCardMapper = teiCardMapper, showSync = false)
                            val isInactive = student.enrollments.getOrNull(0)
                                ?.status() == EnrollmentStatus.CANCELLED

                            AttendanceOptionContainer(
                                attendanceStatus = attendanceStatus,
                                attendanceBtnState = attendanceBtnState,
                                attendanceOptions = attendanceOptions,
                                formFields = formFields,
                                fieldsState = fieldState,
                                formData = formData,
                                attendanceStep = attendanceStep,
                                isEnabled = !isInactive,
                                student = student,
                                card = card,
                                setAttendance = { index, ou, tei, value, reasonOfAbsence, color, hasPersisted ->
                                    viewModel.setAttendance(
                                        index,
                                        ou,
                                        tei,
                                        student.selectedEnrollment.uid().orEmpty(),
                                        value,
                                        reasonOfAbsence,
                                        color,
                                        hasPersisted,
                                    )
                                },
                                setTEIAbsence = { index, tei, value, color ->
                                    viewModel.setAbsence(
                                        index,
                                        student.tei.organisationUnit().orEmpty(),
                                        tei,
                                        student.selectedEnrollment.uid().orEmpty(),
                                        value,
                                        color,
                                        null,
                                    )
                                },
                                setAbsenceState = viewModel::fieldState,
                                onNext = { tei, ou, fieldData ->
                                    viewModel.setAbsence(reasonOfAbsence = fieldData.second)
                                    viewModel.save()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
