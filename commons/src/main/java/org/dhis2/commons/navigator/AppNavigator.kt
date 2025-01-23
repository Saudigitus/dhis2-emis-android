package org.dhis2.commons.navigator

import androidx.fragment.app.FragmentActivity

class AppNavigator(
    val activity: FragmentActivity,
    val tei: String,
    val program: String,
    val enrollment: String,
) {
    fun navigateToDashboard() {
        (activity.applicationContext as? NavigatorComponentProvider)
            ?.dashboard
            ?.launch(
                activity,
                tei,
                program,
                enrollment,
            )
            ?.let {
                activity.startActivity(it)
            }
    }
}
