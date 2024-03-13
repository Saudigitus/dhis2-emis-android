package org.saudigitus.emis.utils

import org.saudigitus.emis.data.model.CalendarConfig
import org.saudigitus.emis.data.model.ClassPeriod
import org.saudigitus.emis.data.model.Holiday
import org.saudigitus.emis.data.model.WeekDays

object Test {

    val calendarConfig = CalendarConfig(
        listOf(
            ClassPeriod("Term1", "2024-06-01", "2024-06-25"),
            ClassPeriod("Term2", "2024-06-01", "2024-06-25"),
            ClassPeriod("Term3", "2024-06-01", "2024-06-25")
        ),
        listOf(
            Holiday("2024-03-15", "Heroes day"),
            Holiday("2024-03-26", "Heroes day"),
            Holiday("2024-03-06", "Heroes day"),
            Holiday("2024-03-12", "Heroes day"),
            Holiday("2024-03-23", "Heroes day")
        ),
        WeekDays(
            monday = true,
            tuesday = true,
            wednesday = true,
            thursday = true,
            friday = true,
            saturday = false,
            sunday = false
        )
    )
}