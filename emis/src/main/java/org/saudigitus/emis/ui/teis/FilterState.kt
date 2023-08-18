package org.saudigitus.emis.ui.teis

import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.ui.components.Item

data class FilterState(
    val academicYear: Item? = null,
    val school: OU? = null,
    val grade: Item? = null,
    val section: Item? = null
) {
    fun isNull() = academicYear == null &&
    school == null && grade == null &&
    section == null

    fun isNotNull() = academicYear != null &&
        school != null && grade != null &&
        section != null
}
