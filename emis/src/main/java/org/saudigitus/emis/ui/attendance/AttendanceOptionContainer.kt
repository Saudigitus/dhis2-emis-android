package org.saudigitus.emis.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.form.FormBuilder
import org.saudigitus.emis.utils.Constants.ABSENT

@Composable
fun AttendanceOptionContainer(
    uiState: AttendanceUiState,
    card: ListCardUiModel,
    student: SearchTeiModel,
    selectedTei: Set<Pair<String, String>>,
    setAttendance: (
        index: Int,
        ou: String,
        tei: String,
        value: String,
        reasonOfAbsence: String?,
        color: Color?,
        hasPersisted: Boolean,
    ) -> Unit,
    setTEIAbsence: (tei: String, value: String) -> Unit,
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
) {
    Card(
        modifier = Modifier
            .background(
                color = TextColor.OnPrimary,
                shape = RoundedCornerShape(
                    Radius.L,
                ),
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
        ),
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 5.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
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
                            setTEIAbsence(tei ?: student.tei.uid(), attendance)
                        }
                    }
                }
            }
            AbsenceForm(
                visibility = selectedTei.find { it.first == student.tei.uid() } != null,
                enabled = uiState.attendanceStep == ButtonStep.HOLD_SAVING,
                student = student,
                uiState = uiState,
                onNext = onNext,
                setAbsenceState = setAbsenceState,
            )
            Spacer(modifier = Modifier.size(5.dp))
        }
    }
}

@Composable
private fun AbsenceForm(
    visibility: Boolean = false,
    enabled: Boolean = true,
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
            enabled = enabled,
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
            setFormState = setAbsenceState,
        )
    }
}
