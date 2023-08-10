package org.saudigitus.emis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextAttribute(
    attribute: String,
    attributeValue: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start)
    ) {
        Text(
            modifier = Modifier
                .wrapContentWidth(Alignment.Start, false)
                .fillMaxWidth(.2f),
            text = attribute,
            softWrap = true,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            //fontFamily = Rubik,
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            //color = colorResource(R.color.textPrimary)
        )
        Text(
            text = attributeValue,
            softWrap = true,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            //fontFamily = Rubik,
            fontSize = 12.sp,
            fontStyle = FontStyle.Normal,
            //color = colorResource(R.color.textSecondary)
        )
    }
}

data class TitleSubtitleDefaults(
    val titleFontSize: TextUnit = 17.sp,
    val subtitleFontSize: TextUnit = 12.sp,
    val titleFontWeight: FontWeight = FontWeight.Normal,
    val subtitleFontWeight: FontWeight = FontWeight.Normal,
    val titleColor: Color = Color.Black,
    val subtitleColor: Color = Color.Black.copy(.65f)
)

/**
 * Groups the title and subtitle in a column, with the title being the most prominent text
 */
@Composable
fun TitleSubtitleComponent(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    fontDefaults: TitleSubtitleDefaults = TitleSubtitleDefaults()
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            maxLines = 1,
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            fontSize =  fontDefaults.titleFontSize,
            fontWeight = fontDefaults.titleFontWeight,
            color = fontDefaults.titleColor
        )
        subtitle?.let {
            Text(
                text = it,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                fontSize =  fontDefaults.subtitleFontSize,
                fontWeight = fontDefaults.subtitleFontWeight,
                color = fontDefaults.subtitleColor
            )
        }
    }
}