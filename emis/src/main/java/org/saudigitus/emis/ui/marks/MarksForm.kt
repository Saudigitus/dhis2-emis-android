package org.saudigitus.emis.ui.marks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.dhis2.composetable.ui.Keyboard
import org.dhis2.composetable.ui.keyboardAsState
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.ui.form.DropdownField
import org.saudigitus.emis.ui.form.Field
import org.saudigitus.emis.ui.form.FormData
import org.saudigitus.emis.ui.form.FormField
import org.saudigitus.emis.ui.form.InputField

@Composable
fun MarksForm(
    modifier: Modifier = Modifier,
    state: List<Field>,
    key: String,
    fields: List<FormField>,
    formData: List<FormData>? = emptyList(),
    onNext: (Triple<String, String?, ValueType?>) -> Unit,
    setFormState: (
        key: String,
        dataElement: String,
        value: String,
        valueType: ValueType?
    ) -> Unit
) {
    val formState = remember { mutableStateMapOf<String, String>() }

    val focusManager = LocalFocusManager.current
    val keyboardState by keyboardAsState()

    if (keyboardState == Keyboard.Closed) {
        focusManager.clearFocus(true)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        fields.forEach { formField ->
            val data = formData?.find { it.tei == key && it.dataElement == formField.uid }

            if (formField.hasOptions() || data?.hasOptions == true) {
                DropdownField(
                    label = formField.label,
                    placeholder = formField.placeholder,
                    data = formField.options ?: emptyList(),
                    selectedItem = data?.itemOptions
                ) { item ->
                    onNext(Triple(formField.uid, item.code, null))
                }
            } else {
                InputField(
                    value = state.find { it.key == key && it.dataElement == formField.uid }?.value
                        ?: data?.value ?: "",
                    onValueChange = {
                        setFormState.invoke(
                            key,
                            formField.uid,
                            it,
                            formField.type
                        )
                    },
                    placeholder = formField.placeholder,
                    label = formField.label,
                    inputType = formField.type,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (!it.isFocused && (formState.isNotEmpty() || state.isNotEmpty())) {
                                val fieldValue = state.find { field ->
                                    field.key == key && field.dataElement == formField.uid
                                }
                                onNext(Triple(formField.uid, fieldValue?.value, formField.type))
                            }
                        }
                )
            }
        }
    }
}