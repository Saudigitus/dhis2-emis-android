package org.saudigitus.emis.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextChipWithIconVisibility(
    isSelected: MutableState<Boolean>,
    displayName: String,
    code: String,

    onChecked: (checked: Boolean, code: String, displayName: String) -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor = if (isSelected.value) {
        Color(0xFF03599E) // Change to the desired selected background color
    } else {
        Color(0xFCD0E6FB) // Change to the desired unselected background color
    }

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
                color = backgroundColor,
                shape = shape
            )
            .clip(shape = shape)
            .clickable {
                isSelected.value = !isSelected.value // Toggle the value using MutableState
                onChecked(isSelected.value, code, displayName) // Notify the listener
            }
            .padding(horizontal =  12.dp, vertical = 16.dp)

    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            text = displayName,
            fontSize = 14.sp,
            color = Color(0xFF2C98F0)
        )
    }
}
