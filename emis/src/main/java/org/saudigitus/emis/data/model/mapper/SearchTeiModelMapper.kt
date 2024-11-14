package org.saudigitus.emis.data.model.mapper

import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.ui.teis.mapper.TEICardMapper

fun SearchTeiModel.map(
    teiCardMapper: TEICardMapper,
    onSyncIconClick: ((uid: String) -> Unit)? = null,
    showSync: Boolean = true,
    onCardClick: (tei: String, enrollment: String) -> Unit = { _, _ -> },
) = teiCardMapper.map(
    searchTEIModel = this,
    onSyncIconClick = {
        if (onSyncIconClick != null) {
            onSyncIconClick(this.uid())
        }
    },
    onCardClick = {
        onCardClick(this.uid(), this.enrollments.getOrNull(0)?.uid() ?: "")
    },
    onImageClick = {},
    showSync = showSync,
)
