package org.saudigitus.emis.utils

import android.annotation.SuppressLint
import android.content.Context
import org.saudigitus.emis.R

object Utils {
    @SuppressLint("DiscouragedApi")
    fun getDrawableIdByName(
        context: Context,
        name: String
    )  = context.resources
        .getIdentifier(
            name,
            "drawable",
            context.packageName
        )

    /**
     * This functions return specific icons by its name...
     */
    fun getDrawableIdByName(name: String) = when (name) {
        "correct_blue_fill" -> R.drawable.correct_blue_fill
        "wrong_red_fill" -> R.drawable.wrong_red_fill
        "clock_orange_fill" -> R.drawable.clock_orange_fill
        else -> R.drawable.ic_empty
    }

    fun getIconByName(name: String) = when (name) {
        "correct_blue_fill" -> R.drawable.present
        "wrong_red_fill" -> R.drawable.absent
        "clock_orange_fill" -> R.drawable.late
        else -> R.drawable.ic_empty
    }

    const val GREEN = 0xFF81C784
    const val RED = 0xFFE57373
    const val ORANGE = 0xFFFFB74D
    const val WHITE = 0xFFFFFFFF

    fun getColorByIconName(name: String) = when (name) {
        "correct_blue_fill" -> GREEN
        "wrong_red_fill" -> RED
        "clock_orange_fill" -> ORANGE
        else -> WHITE
    }
}