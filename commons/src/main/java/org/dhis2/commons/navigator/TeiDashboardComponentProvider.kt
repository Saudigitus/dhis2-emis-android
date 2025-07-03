package org.dhis2.commons.navigator

import android.content.Context
import android.content.Intent

interface TeiDashboardComponentProvider {
    fun launch(
        context: Context,
        teiUid: String?,
        programUid: String?,
        enrollmentUid: String?,
        academicYear: String
    ): Intent
}
