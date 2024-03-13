package org.saudigitus.emis.ui.form

import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.data.model.Option

data class FormField(
    val uid: String,
    val label: String,
    val type: ValueType?,
    val value: String? = null,
    val placeholder: String = "",
    val options: List<Option>? = null
) {
    fun hasOptions() = options?.isNotEmpty() == true
}

data class Field(
    val key: String,
    val dataElement: String,
    val value: String,
    val valueType: ValueType?
)