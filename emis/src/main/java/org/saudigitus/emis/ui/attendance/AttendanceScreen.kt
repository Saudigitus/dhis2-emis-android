package org.saudigitus.emis.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.dhis2.commons.data.SearchTeiModel
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.mapper.map
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ShowCard
import org.saudigitus.emis.ui.components.Toolbar
import org.saudigitus.emis.ui.components.ToolbarActionState
import org.saudigitus.emis.ui.form.FormBuilder
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
    teiPositions: Set<Int>,
    onBack: () -> Unit,
    setDate: (String) -> Unit,
    setAbsence: (reasonOfAbsence: String?) -> Unit,
    setTEIPos: (pos: Int) -> Unit,
    summary: () -> List<Triple<Int, ImageVector?, Color?>>,
    setAttendanceStep: (buttonStep: ButtonStep) -> Unit,
    setAttendance: (
        teiPos: Int?,
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
    setAbsenceState: (
        key: String,
        dataElement: String,
        value: String,
        valueType: ValueType?,
    ) -> Unit,
    onNext: (
        tei: String,
        ou: String,
        fieldData: Triple<String, String?, ValueType?>,
    ) -> Unit,
    onSave: () -> Unit,
    bulkSave: () -> Unit,
    clearCache: () -> Unit,
    refreshOnSave: () -> Unit,
) {
    var isAbsent by remember { mutableStateOf(false) }
    var isAttendanceCompleted by remember { mutableStateOf(false) }
    var launchBulkAssign by remember { mutableStateOf(false) }
    var isBulk by remember { mutableStateOf(false) }

    var cachedTEIId by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
                    true,
                    enabledIconButton = uiState.attendanceStep == ButtonStep.HOLD_SAVING,
                    onIconClick = { launchBulkAssign = true },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(uiState.students) { i, student ->
                        val card = student.map(teiCardMapper, showSync = false)

                        Card(
                            modifier = Modifier
                                .background(
                                    color = TextColor.OnPrimary, shape = RoundedCornerShape(
                                        Radius.L
                                    )
                                )
                                .shadow(
                                    elevation = 10.dp,
                                    spotColor = SurfaceColor.Container,
                                )
                                .clip(shape = RoundedCornerShape(Radius.L))
                                .padding(bottom = 5.dp),
                            shape = RoundedCornerShape(Radius.L),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 3.dp,
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(bottom = 5.dp),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
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
                                            if (attendance.lowercase() != ABSENT) {
                                                setAttendance(
                                                    i,
                                                    index,
                                                    student.tei.organisationUnit() ?: "",
                                                    tei ?: student.tei.uid(),
                                                    attendance,
                                                    null,
                                                    color,
                                                    true,
                                                )
                                            } else {
                                                cachedTEIId = tei ?: student.tei.uid()
                                                isAbsent = true
                                                setTEIPos(i)
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
                                AbsenceForm(
                                    visibility = teiPositions.find { it == i } != null,
                                    student = student,
                                    uiState = uiState,
                                    onNext = onNext,
                                    setAbsenceState = setAbsenceState,
                                )
                                Spacer(modifier = Modifier.size(5.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AbsenceForm(
    visibility: Boolean = false,
    student: SearchTeiModel,
    uiState: AttendanceUiState,
    onNext: (
        tei: String,
        ou: String,
        fieldData: Triple<String, String?, ValueType?>,
    ) -> Unit,
    setAbsenceState: (
        key: String,
        dataElement: String,
        value: String,
        valueType: ValueType?,
    ) -> Unit,
) {
    AnimatedVisibility(visible = visibility) {
        FormBuilder(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            label = stringResource(R.string.reason_absence),
            state = uiState.fieldsState,
            key = student.uid(),
            fields = uiState.formFields,
            onNext = {
                onNext.invoke(
                    student.uid(),
                    student.tei.organisationUnit() ?: "",
                    it,
                )
            },
            setFormState = setAbsenceState
        )
    }
}
