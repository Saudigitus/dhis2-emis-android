package org.saudigitus.emis.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.ActionButtons
import org.saudigitus.emis.ui.components.DropDown
import org.saudigitus.emis.ui.components.DropDownItem
import org.saudigitus.emis.ui.theme.light_error
import org.saudigitus.emis.ui.theme.light_success
import org.saudigitus.emis.ui.theme.light_warning

@Composable
fun ReasonForAbsenceDialog(
    reasons: List<DropDownItem>,
    title: String,
    themeColor: Color,
    onItemClick: (DropDownItem) -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    DialogTemplate(
        title = title,
        themeColor = themeColor
    ) {
        DropDown(
            placeholder = stringResource(R.string.reason_absence),
            leadingIcon = Icons.Default.Quiz,
            data = reasons,
            onItemClick = { onItemClick.invoke(it) }
        )
        ActionButtons(
            modifier = Modifier.align(Alignment.End),
            contentColor = themeColor,
            onCancel = onCancel,
            onDone = onDone
        )
    }
}

@Composable
fun AttendanceSummaryDialog(
    title: String,
    presentValue: String,
    lateValue: String,
    absentValue: String,
    themeColor: Color,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    DialogTemplate(
        title = title,
        themeColor = themeColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryComponent(
                summary = presentValue,
                containerColor = light_success,
                icon = Icons.Outlined.Check
            )

            SummaryComponent(
                summary = lateValue,
                containerColor = light_warning,
                icon = Icons.Outlined.Schedule
            )

            SummaryComponent(
                summary = absentValue,
                containerColor = light_error,
                icon = Icons.Outlined.Close
            )
        }

        ActionButtons(
            modifier = Modifier.align(Alignment.End),
            contentColor = themeColor,
            onCancel = onCancel,
            onDone = onDone
        )
    }
}


@Composable
private fun SummaryComponent(
    title: String? = null,
    summary: String,
    containerColor: Color,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .width(95.dp)
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White
            )
            Text(text = summary)
        }
    }
}

@Composable
private fun DialogTemplate(
    title: String,
    themeColor: Color,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(color = themeColor),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 16.dp),
                    color = Color.White
                )
            }
            content()
        }
    }
}