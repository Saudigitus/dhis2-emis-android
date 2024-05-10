package org.saudigitus.emis.ui.form

import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.emis.data.model.Option

data class FormData(
    val tei: String,
    val dataElement: String,
    val value: String?,
    val date: String?,
    val valueType: ValueType?,
    val hasOptions: Boolean,
    val itemOptions: Option? = null
)