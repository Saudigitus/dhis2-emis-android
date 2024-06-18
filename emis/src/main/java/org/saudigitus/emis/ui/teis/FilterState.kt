package org.saudigitus.emis.ui.teis

import org.saudigitus.emis.data.model.OU
import org.saudigitus.emis.ui.components.DropdownItem
import org.saudigitus.emis.utils.Constants

data class FilterState(
    val academicYear: DropdownItem? = null,
    val school: OU? = null,
    val grade: DropdownItem? = null,
    val section: DropdownItem? = null,
    val key: String? = null,
) {
    fun isNull() = academicYear == null &&
        school == null && grade == null &&
        section == null

    fun isStaff() = key == Constants.STAFF

    fun options(): List<String> {
        return if (key == Constants.STAFF && academicYear != null && school != null) {
            listOf(
                academicYear.code,
                grade?.code,
                section?.code,
            ).mapNotNull { it }
        } else if (!isNull()) {
            listOf(
                "${academicYear?.code}",
                "${grade?.code}",
                "${section?.code}",
            )
        } else {
            emptyList()
        }
    }
}
