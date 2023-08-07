package org.saudigitus.emis.ui.teis

import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.ui.components.DropDownItem

data class FilterState(
    val academicYear: DropDownItem? = null,
    val school: OU? = null,
    val grade: DropDownItem? = null,
    val section: DropDownItem? = null
) {
    fun isNull() = academicYear == null &&
    school == null && grade == null &&
    section == null

    fun isNotNull() = academicYear != null &&
        school != null && grade != null &&
        section != null
}
