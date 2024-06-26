package org.saudigitus.emis.ui.teis.mapper

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyle
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.saudigitus.emis.R
import java.io.File


class TEICardMapper(
    val context: Context,
    val resourceManager: ResourceManager,
) {

    fun map(
        searchTEIModel: SearchTeiModel,
        onSyncIconClick: () -> Unit,
        onCardClick: () -> Unit,
        onImageClick: (String) -> Unit,
    ): ListCardUiModel {
        return ListCardUiModel(
            avatar = { ProvideAvatar(searchTEIModel, onImageClick) },
            title = getTitle(searchTEIModel),
            lastUpdated = searchTEIModel.tei.lastUpdated().toDateSpan(context),
            additionalInfo = getAdditionalInfoList(searchTEIModel),
            actionButton = { ProvideSyncButton(searchTEIModel, onSyncIconClick) },
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            onCardCLick = onCardClick,
        )
    }

    @Composable
    private fun ProvideAvatar(item: SearchTeiModel, onImageClick: ((String) -> Unit)) {
        if (item.profilePicturePath.isNotEmpty()) {
            val file = File(item.profilePicturePath)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath).asImageBitmap()
            val painter = BitmapPainter(bitmap)

            Avatar(
                imagePainter = painter,
                style = AvatarStyle.IMAGE,
                onImageClick = { onImageClick(item.profilePicturePath) },
            )
        } else {
            Avatar(
                textAvatar = getTitleFirstLetter(item),
                style = AvatarStyle.TEXT,
            )
        }
    }

    private fun getTitleFirstLetter(item: SearchTeiModel): String {
        val firstLetter = item.attributeValues?.values
            ?.toList()?.getOrNull(1)
            ?.value()
            ?.getOrNull(0) ?: '?'

        return "${firstLetter.uppercaseChar()}"
    }

    private fun getTitle(item: SearchTeiModel): String {
        return "${item.attributeValues?.values?.toList()?.getOrNull(1)?.value()} " +
            "${item.attributeValues?.values?.toList()?.getOrNull(2)?.value()}"
    }

    private fun getAdditionalInfoList(item: SearchTeiModel): List<AdditionalInfoItem> {
        val attributeList = listOf(
            AdditionalInfoItem(
                value = item.attributeValues?.values?.toList()?.getOrNull(0)?.value() ?: ""
            )
        )

        return attributeList

    }

    @Composable
    private fun ProvideSyncButton(searchTEIModel: SearchTeiModel, onSyncIconClick: () -> Unit) {
        val buttonText = when (searchTEIModel.tei.aggregatedSyncState()) {
            State.TO_POST,
            State.TO_UPDATE,
            -> {
                resourceManager.getString(R.string.sync)
            }

            State.ERROR,
            State.WARNING,
            -> {
                resourceManager.getString(R.string.sync_retry)
            }

            else -> null
        }
        buttonText?.let {
            Button(
                style = ButtonStyle.TONAL,
                text = it,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = it,
                        tint = TextColor.OnPrimaryContainer,
                    )
                },
                onClick = { onSyncIconClick() },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
