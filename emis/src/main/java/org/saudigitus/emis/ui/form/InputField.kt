package org.saudigitus.emis.ui.form

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.dhis2.composetable.model.extensions.keyboardCapitalization
import org.dhis2.composetable.model.extensions.toKeyboardType
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.R
import org.saudigitus.emis.utils.toKeyBoardInputType

@Composable
fun InputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    inputType: ValueType?
) {
    TextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        singleLine = inputType?.toKeyBoardInputType()?.multiline == true,
        maxLines = if (inputType?.toKeyBoardInputType()?.multiline == false) 1 else Int.MAX_VALUE,
        keyboardOptions = KeyboardOptions(
            keyboardType = inputType?.toKeyBoardInputType()?.toKeyboardType() ?: KeyboardType.Text,
            capitalization = inputType?.toKeyBoardInputType()?.keyboardCapitalization() ?: KeyboardCapitalization.None,
            imeAction = ImeAction.Done
        ),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent
        ),
        visualTransformation =  if (inputType?.toKeyBoardInputType()?.toKeyboardType() == KeyboardType.Password) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        }
    )
}
