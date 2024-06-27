package org.saudigitus.emis.data.model.mapper

import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.ui.teis.mapper.TEICardMapper

fun SearchTeiModel.map(
    teiCardMapper: TEICardMapper,
    onSyncIconClick: ((uid: String) -> Unit)? = null,
    showSync: Boolean = true
) = teiCardMapper.map(
    searchTEIModel = this,
    onSyncIconClick = {
        if (onSyncIconClick != null) {
            onSyncIconClick(this.uid())
        }
    },
    onCardClick = {},
    onImageClick = {},
    showSync = showSync
)
