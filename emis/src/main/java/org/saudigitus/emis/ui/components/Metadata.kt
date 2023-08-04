package org.saudigitus.emis.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.saudigitus.emis.R

@Composable
fun MetadataIcon(
    modifier: Modifier = Modifier,
    cornerShape: Dp = 4.dp,
    backgroundColor: Color,
    size: Dp = 40.dp,
    paddingAll: Dp = 0.dp,
    painter: Painter? = null,
    contentDescription: String? = null,
    colorFilter: ColorFilter? = null
) {
    if (painter != null) {
        Image(
            modifier = modifier
                .clip(RoundedCornerShape(cornerShape))
                .background(color = backgroundColor)
                .size(size)
                .padding(paddingAll),
            painter = painter,
            contentDescription = contentDescription,
            colorFilter = colorFilter
        )
    }
}

@Composable
fun MetadataItem(
    displayName: String,
    attrValue: String? = null,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .clickable { onClick.invoke() },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundedIcon(label = "${displayName[0]}")
            Spacer(modifier = Modifier.size(15.dp))
            TitleSubtitleComponent(
                modifier = Modifier.weight(1f, true),
                title = displayName,
                subtitle = "$attrValue",
                fontDefaults = TitleSubtitleDefaults(
                    titleColor = Color.Black.copy(.75f)
                )
            )
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth(.83f)
                .align(Alignment.End)
                .wrapContentWidth(Alignment.End, false)
                .padding(end = 5.dp)
        )
    }
}

@Composable
fun RoundedIcon(
    painter: Painter? = null,
    label: String? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color = colorResource(R.color.colorPrimary))
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        MetadataIcon(
            cornerShape = 100.dp,
            backgroundColor = colorResource(R.color.colorPrimary),
            size = 48.dp,
            paddingAll = 5.dp,
            painter = painter,
            colorFilter = ColorFilter.tint(Color.White)
        )
        if (painter == null) {
            Text(text = "$label", color = Color.White)
        }
    }
}