package org.saudigitus.emis.ui.teis

import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.ui.components.DropdownItem

data class FilterState(
    val academicYear: DropdownItem? = null,
    val school: OU? = null,
    val grade: DropdownItem? = null,
    val section: DropdownItem? = null
) {
    fun isNull() = academicYear == null &&
    school == null && grade == null &&
    section == null

    fun isNotNull() = academicYear != null &&
        school != null && grade != null &&
        section != null
}
