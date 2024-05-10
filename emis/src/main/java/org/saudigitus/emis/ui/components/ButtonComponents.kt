package org.saudigitus.emis.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.saudigitus.emis.R

@Composable
fun TextButton(
    title: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = { onClick.invoke() },
        border = BorderStroke(width = 0.dp, color = Color.White),
        shape = ShapeDefaults.Small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = contentColor
        )
    ) {
        Text(text = title)
    }
}

@Composable
fun ActionButtons(
    modifier: Modifier = Modifier,
    contentColor: Color,
    onCancel: () -> Unit,
    onDone: () -> Unit,
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            title = stringResource(R.string.cancel),
            containerColor = Color.White,
            contentColor = contentColor
        ) { onCancel.invoke() }

        TextButton(
            title = stringResource(R.string.done),
            containerColor = Color.White,
            contentColor = contentColor
        ) { onDone.invoke() }
    }
}