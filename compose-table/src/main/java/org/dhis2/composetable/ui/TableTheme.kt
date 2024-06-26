package org.dhis2.composetable.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.dhis2.composetable.actions.DefaultValidator
import org.dhis2.composetable.actions.LocalValidator
import org.dhis2.composetable.actions.Validator

@Composable
fun TableTheme(
    tableColors: TableColors?,
    tableDimensions: TableDimensions? = LocalTableDimensions.current,
    tableConfiguration: TableConfiguration? = LocalTableConfiguration.current,
    tableValidator: Validator? = null,
    content: @Composable
    () -> Unit
) {
    CompositionLocalProvider(
        LocalTableColors provides (tableColors ?: TableColors()),
        LocalTableDimensions provides (tableDimensions ?: TableDimensions()),
        LocalTableConfiguration provides (tableConfiguration ?: TableConfiguration()),
        LocalValidator provides (tableValidator ?: DefaultValidator())
    ) {
        MaterialTheme(
            content = content
        )
    }
}

object TableTheme {
    val colors: TableColors
        @Composable
        get() = LocalTableColors.current
    val dimensions: TableDimensions
        @Composable
        get() = LocalTableDimensions.current
    val configuration: TableConfiguration
        @Composable
        get() = LocalTableConfiguration.current
    val tableSelection
        @Composable
        get() = LocalTableSelection.current
    val validator
        @Composable
        get() = LocalValidator.current
}
