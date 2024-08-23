package org.saudigitus.emis.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.utils.DateHelper

@OptIn(ExperimentalMaterial3Api::class)
class SchoolDates(
    private val schoolCalendar: CalendarConfig?,
) : SelectableDates {

    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val date = DateHelper.stringToLocalDate(DateHelper.formatDate(utcTimeMillis)!!)

        return if (schoolCalendar != null) {
            (
                !DateHelper.isWeekend(date) && schoolCalendar.weekDays.saturday.not() &&
                    schoolCalendar.weekDays.sunday.not()
                ) && DateHelper.isHoliday(schoolCalendar.holidays, utcTimeMillis)
        } else {
            true
        }
    }
}
