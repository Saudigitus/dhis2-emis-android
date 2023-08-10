package org.saudigitus.emis.utils

import org.dhis2.commons.date.DateUtils
import timber.log.Timber
import java.text.SimpleDateFormat
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
}