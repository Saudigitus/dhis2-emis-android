package org.saudigitus.emis.ui.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.saudigitus.emis.data.model.dto.AttendanceEntity
import org.saudigitus.emis.utils.Utils.WHITE

@Composable
fun AttendanceItemState(
    tei: String,
    attendanceState: List<AttendanceEntity>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (attendanceState.isEmpty()) {
            Icon(
                imageVector = Icons.Filled.Help,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color.LightGray
            )
        } else {
            val attendance = attendanceState.find { it.tei == tei }

            if (attendance != null) {

                Icon(
                    imageVector = ImageVector.vectorResource(attendance.setting?.icon!!),
                    contentDescription = attendance.value,
                    modifier = Modifier.size(32.dp),
                    tint = Color(attendance.setting.iconColor)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Help,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun AttendanceButtons(
    tei: String,
    btnState: List<AttendanceActionButtonState>,
    actions: List<AttendanceOption>,
    onClick: (
        index: Int,
        tei: String?,
        attendanceState: String
    ) -> Unit
) {
    var btnCode by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(-1) }

    Row(
        modifier = Modifier.layoutId(layoutId = tei),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEachIndexed { index, action ->
            IconButton(
                onClick = {
                    btnCode = action.code ?: ""
                    selectedIndex = index

                    onClick.invoke(
                        index,
                        tei,
                        action.code ?: ""
                    )
                },
                modifier = Modifier
                    .border(
                        border = BorderStroke((0.5).dp, Color.LightGray),
                        shape = MaterialTheme.shapes.small.copy(CornerSize(32.dp))
                    )
                    .size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(
                        getContainerColor(btnState, tei, action.code.toString(), selectedIndex, index)
                    ),
                    contentColor = Color(
                        if (selectedIndex == index || btnState.isNotEmpty()) {
                            getContentColor(btnState, tei, action.code.toString(), selectedIndex, index)
                                ?: action.hexColor ?: WHITE
                        } else {
                            action.hexColor ?: WHITE
                        }
                    )
                )
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(action.icon),
                    contentDescription = action.name
                )
            }
        }
    }
}

private fun getContainerColor(
    btnState: List<AttendanceActionButtonState>,
    tei: String,
    code: String,
    selectedIndex: Int,
    itemIndex: Int
): Long {
    val attendance = btnState.find { it.btnId == tei }

    return if (attendance != null &&
        attendance.buttonState?.buttonType == code &&
        (attendance.btnIndex == selectedIndex || attendance.btnIndex == itemIndex)
    ) {
        attendance.buttonState.containerColor ?: 0L
    } else {
        WHITE
    }
}

private fun getContentColor(
    btnState: List<AttendanceActionButtonState>,
    tei: String,
    code: String,
    selectedIndex: Int,
    itemIndex: Int
): Long? {
    val attendance = btnState.find { it.btnId == tei }

    return if (attendance != null &&
        attendance.buttonState?.buttonType == code &&
        (attendance.btnIndex == selectedIndex || attendance.btnIndex == itemIndex)
    ) {
        attendance.buttonState.contentColor ?: WHITE
    } else {
        null
    }
}