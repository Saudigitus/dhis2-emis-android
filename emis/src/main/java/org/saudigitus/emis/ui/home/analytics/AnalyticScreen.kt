package org.saudigitus.emis.ui.home.analytics

import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment

@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    activity: FragmentActivity,
    program: String
) {
    var frameId = 0
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context).apply {
                id = ViewCompat.generateViewId()
                frameId = id
            }
        },
        update = {
            activity.supportFragmentManager.beginTransaction()
                .add(frameId, GroupAnalyticsFragment.forProgram(program))
                .commit()
        },
    )
}