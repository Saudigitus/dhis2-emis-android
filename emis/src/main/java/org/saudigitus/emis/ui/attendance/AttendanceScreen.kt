package org.saudigitus.emis.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.mapper.map
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.teis.mapper.TEICardMapper
import org.saudigitus.emis.ui.theme.light_success
import org.saudigitus.emis.utils.Constants.ABSENT
import org.saudigitus.emis.utils.DateHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    uiState: AttendanceUiState,
    infoCard: InfoCard,
    teiCardMapper: TEICardMapper,
    onBack: () -> Unit,
    setDate: (String) -> Unit,
    summary: () -> List<Triple<Int, ImageVector?, Color?>>,
    setAttendanceStep: (buttonStep: ButtonStep) -> Unit,
    setAttendance: (
        index: Int,
        ou: String,
        tei: String,
        value: String,
        reasonOfAbsence: String?,
        color: Color?,
        hasPersisted: Boolean,
    ) -> Unit,
    onSetAbsence: (
        index: Int,
        ou: String,
        tei: String,
        value: String,
        reasonOfAbsence: String?,
    ) -> Unit,
    bulkAttendance: (
        index: Int,
        value: String,
        reasonOfAbsence: String?,
    ) -> Unit,
    setAbsenceReason: (reasonOfAbsence: String?) -> Unit,
    bulkSave: () -> Unit,
    clearCache: () -> Unit,
    onSave: () -> Unit,
    refreshOnSave: () -> Unit,
) {
    var isAttendanceCompleted by remember { mutableStateOf(false) }
    var launchBulkAssign by remember { mutableStateOf(false) }
    var isBulk by remember { mutableStateOf(false) }
    var isAbsent by remember { mutableStateOf(false) }

    var cachedTEIId by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    if (isAbsent) {
        ReasonForAbsenceDialog(
            reasons = uiState.reasonOfAbsence,
            title = stringResource(R.string.reason_absence),
            themeColor = Color(0xFF2C98F0),
            selectedItemCode = if (uiState.absence.find { it.tei == cachedTEIId } != null) {
                uiState.absence.find { it.tei == cachedTEIId }!!.reasonOfAbsence
            } else {
                null
            },
            onItemClick = {
                setAbsenceReason(it.code)
            },
            onCancel = {
                isAbsent = false
            },
            onDone = {
                isAbsent = false
                onSave()
            },
        )
    }

    if (uiState.attendanceStep == ButtonStep.SAVING) {
        AttendanceSummaryDialog(
            title = stringResource(R.string.attendance_summary),
            data = summary(),
            themeColor = Color(0xFF2C98F0),
            onCancel = { setAttendanceStep(ButtonStep.HOLD_SAVING) },
        ) {
            if (isBulk) {
                bulkSave()
            } else {
                clearCache()
                refreshOnSave()
            }
            isAttendanceCompleted = true
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
            attendanceOptions = uiState.attendanceOptions,
            onAttendanceStatus = { status ->
                isBulk = true
                bulkAttendance(
                    status.first,
                    status.second.lowercase(),
                    null,
                )
            },
            onClear = clearCache::invoke,
            onCancel = { launchBulkAssign = false },
        )
    }

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
                navigationAction = onBack::invoke,
                disableNavigation = false,
                actionState = ToolbarActionState(
                    syncVisibility = false,
                    filterVisibility = false,
                    showCalendar = true,
                ),
                calendarAction = setDate::invoke,
                dateValidator = {
                    val date = DateHelper.stringToLocalDate(DateHelper.formatDate(it)!!)

                    if (uiState.schoolCalendar != null) {
                        (
                            !DateHelper.isWeekend(date) && !uiState.schoolCalendar.weekDays.saturday &&
                                !uiState.schoolCalendar.weekDays.sunday
                            ) &&
                            DateHelper.isHoliday(uiState.schoolCalendar.holidays, it)
                    } else {
                        true
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = if (uiState.attendanceStep == ButtonStep.EDITING) {
                            stringResource(R.string.update)
                        } else {
                            stringResource(R.string.save)
                        },
                        color = Color(0xFF2C98F0),
                        style = LocalTextStyle.current.copy(
                            fontFamily = FontFamily(Font(R.font.rubik_medium)),
                        ),
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (uiState.attendanceStep == ButtonStep.EDITING) {
                            Icons.Default.Edit
                        } else {
                            Icons.Default.Save
                        },
                        contentDescription = null,
                        tint = Color(0xFF2C98F0),
                    )
                },
                onClick = {
                    if (uiState.attendanceStep == ButtonStep.HOLD_SAVING) {
                        setAttendanceStep(ButtonStep.SAVING)
                    } else {
                        setAttendanceStep(ButtonStep.HOLD_SAVING)
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
                horizontalAlignment = Alignment.Start,
            ) {
                ShowCard(
                    infoCard,
                    false,
                    enabledIconButton = uiState.attendanceStep == ButtonStep.HOLD_SAVING,
                    onIconClick = { launchBulkAssign = true },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(uiState.students) { _, student ->
                        val card = student.map(teiCardMapper, showSync = false)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Color.White),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            ListCard(
                                modifier = Modifier.testTag("TEI_ITEM"),
                                listAvatar = card.avatar,
                                title = ListCardTitleModel(text = card.title),
                                additionalInfoList = card.additionalInfo,
                                actionButton = card.actionButton,
                                expandLabelText = card.expandLabelText,
                                shrinkLabelText = card.shrinkLabelText,
                                onCardClick = card.onCardCLick,
                                shadow = false,
                            )

                            if (uiState.attendanceStep == ButtonStep.EDITING) {
                                AttendanceItemState(
                                    tei = student.tei.uid(),
                                    attendanceState = uiState.attendanceStatus,
                                )
                            } else {
                                AttendanceButtons(
                                    tei = student.tei.uid(),
                                    btnState = uiState.attendanceBtnState,
                                    actions = uiState.attendanceOptions,
                                ) { index, tei, attendance, color ->
                                    setAttendance(
                                        index,
                                        student.tei.organisationUnit() ?: "",
                                        tei ?: student.tei.uid(),
                                        attendance,
                                        null,
                                        color,
                                        true,
                                    )
                                    if (attendance.lowercase() == ABSENT) {
                                        cachedTEIId = tei ?: student.tei.uid()
                                        isAbsent = true
                                        onSetAbsence(
                                            index,
                                            student.tei.organisationUnit() ?: "",
                                            tei ?: student.tei.uid(),
                                            attendance,
                                            null,
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
