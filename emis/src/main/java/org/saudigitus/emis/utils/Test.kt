package org.saudigitus.emis.utils

import org.saudigitus.emis.R
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.data.model.ClassPeriod
import org.saudigitus.emis.data.model.Holiday
import org.saudigitus.emis.data.model.WeekDays
import org.saudigitus.emis.utils.Utils.GREEN
import org.saudigitus.emis.utils.Utils.ORANGE
import org.saudigitus.emis.utils.Utils.RED

object Test {


    val attendanceStatus = listOf(
        Triple(R.drawable.present, "Present", GREEN),
        Triple(R.drawable.late, "Late", ORANGE),
        Triple(R.drawable.absent, "Absent", RED),
    )
}
