package org.saudigitus.emis.ui.performance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.Subject
import org.saudigitus.emis.ui.components.DropDown
import org.saudigitus.emis.ui.components.InfoCard
import org.saudigitus.emis.ui.components.ShowCard

@Composable
fun PerformanceDetails(
    modifier: Modifier = Modifier,
    infoCard: InfoCard,
    subjects: List<Subject>,
    defaultSelection: String = "",
    onItemClick: (Subject) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(width = 0.85.dp, color = Color.LightGray.copy(.85f)),
        shape = RoundedCornerShape(16.dp),
    ) {
        ShowCard(infoCard = infoCard)
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            color = Color.LightGray.copy(.85f),
            thickness = .9.dp
        )
        DropDown(
            placeholder = stringResource(R.string.subject),
            leadingIcon = ImageVector.vectorResource(R.drawable.ic_category),
            trailingIcon = Icons.TwoTone.Edit,
            data = subjects,
            elevation = 1.dp,
            selectedItemName = defaultSelection,
            onItemClick = onItemClick
        )
        Spacer(modifier = Modifier.size(5.dp))
    }
}