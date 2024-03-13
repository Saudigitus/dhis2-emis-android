package org.saudigitus.emis.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.ActionButtons
import org.saudigitus.emis.ui.components.Item
import org.saudigitus.emis.ui.theme.light_error
import org.saudigitus.emis.ui.theme.light_success
import org.saudigitus.emis.ui.theme.light_warning

@Composable
fun ReasonForAbsenceDialog(
    reasons: List<Item>,
    title: String,
    themeColor: Color,
    selectedItemCode: String? = null,
    onItemClick: (Item) -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    var selectedIndex by remember {
        mutableStateOf(reasons.indexOfFirst { it.code == selectedItemCode })
    }

    AlertDialogTemplate {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.Black.copy(.75f),
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular))
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(.75f),
            thickness = .5.dp
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .fillMaxHeight(.5f)
        ) {
            itemsIndexed(reasons) { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable {
                            selectedIndex = index
                            onItemClick(option)
                        },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            onItemClick(option)
                        }
                    )
                    Text(text = option.itemName)
                }
            }
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(.75f),
            thickness = .5.dp
        )
        ActionButtons(
            modifier = Modifier.align(Alignment.End),
            contentColor = themeColor,
            onCancel = {
                selectedIndex = -1
                onCancel.invoke()
            },
            onDone = onDone
        )
    }
}

@Composable
fun AttendanceSummaryDialog(
    title: String,
    data: Triple<String, String, String>,
    themeColor: Color,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    AlertDialogTemplate {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.Start),
            color = Color.Black.copy(.75f),
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            textAlign = TextAlign.Start,
            style = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular))
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(.75f),
            thickness = .5.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryComponent(
                summary = data.first,
                containerColor = light_success,
                icon = Icons.Outlined.Check
            )

            SummaryComponent(
                summary = data.second,
                containerColor = light_warning,
                icon = Icons.Outlined.Schedule
            )

            SummaryComponent(
                summary = data.third,
                containerColor = light_error,
                icon = Icons.Outlined.Close
            )
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Color.LightGray.copy(.75f),
            thickness = .5.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertDialogTemplate(
    content: @Composable() (ColumnScope.() -> Unit)
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = Color.White,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
    }
}