package org.saudigitus.emis.ui.form

import android.content.Intent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.dhis2.composetable.model.extensions.keyboardCapitalization
import org.dhis2.composetable.model.extensions.toKeyboardType
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.ui.form.fields.IntentAction
import org.saudigitus.emis.utils.toKeyBoardInputType

@Composable
fun InputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String,
    inputType: ValueType?,
    enabled: Boolean = true,
    colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
    ),
) {
    var action by remember { mutableStateOf("") }

    if (action.isNotEmpty()) {
        IntentAction(action = action, value = value)
    }

    TextField(
        modifier = modifier,
        enabled = enabled,
        value = value,
        onValueChange = onValueChange,
        label = if (label != null) { { Text(text = label) } } else null,
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            when (inputType?.toKeyBoardInputType()?.toKeyboardType()) {
                KeyboardType.Email -> {
                    IconButton(onClick = { action = Intent.ACTION_SENDTO }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = label,
                        )
                    }
                }

                KeyboardType.Phone -> {
                    IconButton(onClick = { action = Intent.ACTION_DIAL }) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = label,
                        )
                    }
                }
            }
        },
        singleLine = inputType?.toKeyBoardInputType()?.multiline == true,
        maxLines = if (inputType?.toKeyBoardInputType()?.multiline == false) 1 else Int.MAX_VALUE,
        keyboardOptions = KeyboardOptions(
            keyboardType = inputType?.toKeyBoardInputType()?.toKeyboardType() ?: KeyboardType.Number,
            capitalization = inputType?.toKeyBoardInputType()?.keyboardCapitalization() ?: KeyboardCapitalization.None,
            imeAction = ImeAction.Done,
        ),
        colors = colors,
        visualTransformation = if (inputType?.toKeyBoardInputType()?.toKeyboardType() == KeyboardType.Password) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        textStyle = TextStyle(fontSize = 22.sp, textAlign = TextAlign.Center),
    )
}
