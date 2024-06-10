package org.saudigitus.emis.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.MetadataIcon
import org.saudigitus.emis.ui.components.TitleSubtitleComponent

@Composable
fun SubjectItem(
    displayName: String,
    attrValue: String? = null,
    color: Color?,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .clickable { onClick.invoke() },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MetadataIcon(
                backgroundColor = color ?: MaterialTheme.colorScheme.primary,
                painter = painterResource(R.drawable.subject_icon),
                colorFilter = ColorFilter.tint(Color.White),
                paddingAll = 3.dp,
            )
            Spacer(modifier = Modifier.size(15.dp))
            TitleSubtitleComponent(
                modifier = Modifier.weight(1f, true),
                title = displayName,
                subtitle = attrValue,
            )
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth(.83f)
                .align(Alignment.End)
                .wrapContentWidth(Alignment.End, false)
                .padding(end = 5.dp),
            thickness = .75.dp,
        )
    }
}
