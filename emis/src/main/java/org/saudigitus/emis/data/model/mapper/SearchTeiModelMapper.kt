package org.saudigitus.emis.data.model.mapper

import org.dhis2.commons.data.SearchTeiModel
import org.saudigitus.emis.ui.teis.mapper.TEICardMapper

fun SearchTeiModel.map(
    teiCardMapper: TEICardMapper
) = teiCardMapper.map(
    searchTEIModel = this,
    onSyncIconClick = { },
    onCardClick = {},
    onImageClick = {}
)
