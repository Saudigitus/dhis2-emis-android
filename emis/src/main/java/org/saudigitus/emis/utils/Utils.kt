package org.saudigitus.emis.utils

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import org.saudigitus.emis.R
import org.saudigitus.emis.utils.Constants.ABSENT
import org.saudigitus.emis.utils.Constants.LATE
import org.saudigitus.emis.utils.Constants.PRESENT

object Utils {
    const val GREEN = 0xFF81C784
    const val RED = 0xFFE57373
    const val ORANGE = 0xFFFFB74D
    const val WHITE = 0xFFFFFFFF

    fun getIconByName(name: String) = when (name) {
        "correct_blue_fill" -> R.drawable.present
        "wrong_red_fill" -> R.drawable.absent
        "clock_orange_fill" -> R.drawable.late
        else -> R.drawable.ic_empty
    }

    fun dynamicIcons(name: String) = try {
        val cl = Class.forName("androidx.compose.material.icons.filled.${name}Kt")
        val method = cl.declaredMethods.first()
        method.invoke(null, Icons.Filled) as ImageVector
    } catch (_: Throwable) {
        null
    }

    fun getColorByAttendanceType(type: String) = when (type.lowercase()) {
        PRESENT -> { GREEN }
        LATE -> { ORANGE }
        ABSENT -> { RED }
        else -> WHITE
    }
}