package org.saudigitus.emis.ui.save_filter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.saudigitus.emis.ui.components.DropDownItem

/*
@Preview
@Composable
fun ChipWithIconVisibility(){
    TextChipWithIconVisibility(
        false,
        "Grade 1",
        "ssssss",
    ) { checked, code -> }
}*/

@Composable
fun TextChipWithIconVisibility(
    isSelected: MutableState<Boolean>,
    text: String,
    code: String,
    onChecked: (checked: Boolean, code: String) -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                vertical = 16.dp,
                horizontal = 16.dp
            )
            .shadow(3.dp, RoundedCornerShape(16.dp), true)
            .background(
                color = Color(0xFCD0E6FB),
                shape = shape
            )
            .clip(shape = shape)
            .clickable {
                isSelected.value = !isSelected.value // Toggle the value using MutableState
                onChecked(isSelected.value, code) // Notify the listener
            }
            .padding(horizontal =  12.dp, vertical = 16.dp)

    ) {
        if (isSelected.value) {
            Icon(
                Icons.Rounded.Check,
                tint = Color(0xFF2C98F0),
                contentDescription = "Icon"
            )
        }
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF2C98F0)
        )
    }
}
