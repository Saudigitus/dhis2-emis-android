package org.dhis2.form.ui

import android.text.TextWatcher
import android.view.ViewTreeObserver
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormSection
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.FieldProvider
import org.hisp.dhis.mobile.ui.designsystem.component.Section

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Form(
    sections: List<FormSection> = emptyList(),
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
    needToForceUpdate: Boolean,
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val callback = remember {
        object : FieldUiModel.Callback {
            override fun intent(intent: FormIntent) {
                if (intent is FormIntent.OnNext) {
                    scope.launch {
                        intent.position?.let { scrollState.animateScrollToItem(it + 1) }
                    }
                }
                intentHandler(intent)
            }

            override fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents) {
                uiEventHandler(uiEvent)
            }
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        state = scrollState,
        verticalArrangement = spacedBy(24.dp),
    ) {
        if (sections.isNotEmpty()) {
            this.itemsIndexed(
                items = sections,
                key = { _, fieldUiModel -> fieldUiModel.uid },
            ) { _, section ->
                Section(
                    title = section.title,
                    isLastSection = getNextSection(section, sections) == null,
                    description = section.description,
                    completedFields = section.completedFields(),
                    totalFields = section.fields.size,
                    state = section.state,
                    errorCount = section.errorCount(),
                    warningCount = section.warningCount(),
                    onNextSection = {
                        getNextSection(section, sections)?.let {
                            intentHandler.invoke(FormIntent.OnSection(it.uid))
                        }
                    },
                    onSectionClick = {
                        intentHandler.invoke(FormIntent.OnSection(section.uid))
                    },
                    content = {
                        section.fields.forEach { fieldUiModel ->
                            fieldUiModel.setCallback(callback)
                            FieldProvider(
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = tween(
                                        durationMillis = 500,
                                        easing = LinearOutSlowInEasing,
                                    ),
                                ),
                                context = context,
                                fieldUiModel = fieldUiModel,
                                needToForceUpdate = needToForceUpdate,
                                textWatcher = textWatcher,
                                coordinateTextWatcher = coordinateTextWatcher,
                                uiEventHandler = uiEventHandler,
                                intentHandler = intentHandler,
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    },
                )
            }
            item(sections.size - 1) {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
    ScrollOnKeyboardVisibility(scrollState, scope)
}

/**
 * This method listen the keyboard interactions and scroll it if the keyboard is open, to see all the items
 */
@Composable
private fun ScrollOnKeyboardVisibility(
    lazyListState: LazyListState,
    coroutineScope: CoroutineScope,
) {
    val currentView = LocalView.current
    var keyboardHeight by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            currentView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = currentView.height
            val keypadHeight = screenHeight - rect.bottom

            // Calculate the keyboard height when it's first shown
            if (keyboardHeight == 0 && keypadHeight > screenHeight * 0.15) {
                keyboardHeight = keypadHeight
            }

            // If the keyboard is still visible, scroll the list
            if (keypadHeight > screenHeight * 0.15) {
                val scrollOffset =
                    lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.offset ?: 0
                val itemHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
                if (scrollOffset + itemHeight > 0) {
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(
                            lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0,
                        )
                    }
                }
            }
        }

        currentView.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            currentView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
}

private fun getNextSection(section: FormSection, sections: List<FormSection>): FormSection? {
    val currentIndex = sections.indexOf(section)
    if (currentIndex != -1 && currentIndex < sections.size - 1) {
        return sections[currentIndex + 1]
    }
    return null
}
