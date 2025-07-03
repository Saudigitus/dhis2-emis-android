package org.saudigitus.emis.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.SearchTeiModel
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.ui.form.FormBuilder
import org.saudigitus.emis.ui.form.FormField
import org.saudigitus.emis.utils.Constants.ABSENT

@Stable
@Suppress("DEPRECATION")
@Composable
fun AttendanceOptionContainer(
    attendanceStatus: List<AttendanceEntity> = emptyList(),
    attendanceBtnState: List<AttendanceActionButtonState> = emptyList(),
    attendanceOptions: List<AttendanceOption> = emptyList(),
    formFields: List<FormField> = emptyList(),
    fieldsState: List<Field> = emptyList(),
    attendanceStep: ButtonStep,
    card: ListCardUiModel,
    student: SearchTeiModel,
    isEnabled: Boolean = true,
    selectedReason: String? = null,
    setAttendance: (
        index: Int,
        ou: String,
        tei: String,
        value: String,
        reasonOfAbsence: String?,
        color: Color?,
        hasPersisted: Boolean,
    ) -> Unit,
    setTEIAbsence: (index: Int, tei: String, value: String, color: Color?) -> Unit,
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
    var isAbsent by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(bottom = 5.dp)
            .background(color = Color.White),
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

            if (attendanceStep == ButtonStep.EDITING) {
                AttendanceItemState(
                    tei = student.tei.uid(),
                    attendanceState = attendanceStatus,
                )
            } else {
                AttendanceButtons(
                    tei = student.tei.uid(),
                    btnState = attendanceBtnState,
                    actions = attendanceOptions,
                    isEnabled = isEnabled,
                ) { index, key, tei, attendance, color ->
                    setAttendance(
                        index,
                        student.tei.organisationUnit().orEmpty(),
                        tei ?: student.tei.uid(),
                        attendance,
                        null,
                        color,
                        true,
                    )
                    if (key.lowercase() == ABSENT) {
                        isAbsent = true
                        setTEIAbsence(index, tei ?: student.tei.uid(), attendance, color)
                    }
                }
            }
        }
        AbsenceForm(
            visibility = isAbsent || selectedReason != null,
            enabled = attendanceStep == ButtonStep.HOLD_SAVING,
            student = student,
            formFields = formFields,
            fieldsState = fieldsState,
            selectedReason = selectedReason,
            onNext = onNext,
            setAbsenceState = setAbsenceState,
        )
        Spacer(modifier = Modifier.size(10.dp))
    }
}

@Stable
@Composable
private fun AbsenceForm(
    visibility: Boolean = false,
    enabled: Boolean = true,
    student: SearchTeiModel,
    selectedReason: String? = null,
    formFields: List<FormField> = emptyList(),
    fieldsState: List<Field> = emptyList(),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xFFF6F6F6),
                    shape = RoundedCornerShape(16.dp)
                ),
            verticalArrangement = Arrangement.spacedBy(
                8.dp,
                Alignment.Top
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FormBuilder(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                enabled = enabled,
                label = stringResource(R.string.reason_absence),
                state = fieldsState,
                key = student.uid(),
                fields = formFields,
                selectedItemCode = selectedReason,
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
}
