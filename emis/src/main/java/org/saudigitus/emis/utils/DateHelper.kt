package org.saudigitus.emis.utils

import org.dhis2.commons.date.DateUtils
import org.saudigitus.emis.data.model.schoolcalendar_config.Holiday
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

object DateHelper {
    fun formatDate(date: Long): String? {
        return try {
            val formatter = SimpleDateFormat(DateUtils.DATE_FORMAT_EXPRESSION, Locale.US)
            formatter.format(Date(date))
        } catch (e: Exception) {
            Timber.tag("DATE_FORMAT").e(e)
            null
        }
    }

    fun stringToLocalDate(date: String): LocalDate {
        return LocalDate.parse(date)
    }

    fun isWeekend(date: LocalDate): Boolean {
        val dayOfWeek = date.dayOfWeek
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
    }

    fun isHoliday(holidays: List<Holiday>, date: Long): Boolean {
        return holidays.none { holiday ->
            holiday.date == formatDate(date)
        }
    }

    fun formatDateWithWeekDay(date: String): String? {
        return try {
            val inputFormat = SimpleDateFormat(DateUtils.DATE_FORMAT_EXPRESSION, Locale.US)
            val outputFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.US)

            val inputDate: Date = inputFormat.parse(date)!!
            outputFormat.format(inputDate)
        } catch (e: Exception) {
            Timber.tag("DATE_FORMAT").e(e)
            null
        }
    }

    fun dateStringToSeconds(dateString: String): Long {
        val localDate = LocalDate.parse(dateString)
        return localDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() / 1000
    }
}
