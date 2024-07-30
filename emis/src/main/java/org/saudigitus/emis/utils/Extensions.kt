package org.saudigitus.emis.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
import org.saudigitus.emis.R
import org.saudigitus.emis.ui.components.DropdownState
import org.saudigitus.emis.ui.teis.FilterType

fun D2.eventsWithTrackedDataValues(
    ou: String,
    program: String,
    stage: String,
): List<Event> = eventModule().events()
    .byOrganisationUnitUid().eq(ou)
    .byProgramUid().eq(program)
    .byProgramStageUid().eq(stage)
    .withTrackedEntityDataValues()
    .blockingGet()

fun D2.optionByOptionSet(
    optionSet: String?,
): List<Option> = optionModule()
    .options()
    .byOptionSetUid().eq(optionSet)
    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()

fun D2.optionsNotInOptionGroup(
    optionGroups: List<String>,
    optionSet: String?,
): List<Option> = optionModule()
    .optionGroups()
    .byUid().notIn(optionGroups)
    .byOptionSetUid().eq(optionSet)
    .withOptions()
    .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()
    .flatMap {
        it.options() ?: emptyList()
    }.flatMap {
        optionModule()
            .options()
            .byUid().eq(it.uid())
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()
    }

fun D2.optionsByOptionSetAndCode(
    optionSet: String?,
    codes: List<String>,
): List<Option> = optionModule()
    .options()
    .byCode().`in`(codes)
    .byOptionSetUid().eq(optionSet)
    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()

fun List<DropdownState>.getByType(type: FilterType): DropdownState? =
    find { it.filterType == type }


fun DropdownState.icon() = when (this.filterType) {
    FilterType.ACADEMIC_YEAR -> R.drawable.ic_book
    FilterType.GRADE -> R.drawable.ic_school
    FilterType.SECTION -> R.drawable.ic_category
    FilterType.SCHOOL -> R.drawable.ic_location_on
}

fun DropdownState.placeholder() = when(this.filterType) {
    FilterType.ACADEMIC_YEAR -> R.string.academic_year
    FilterType.GRADE -> R.string.grade
    FilterType.SECTION -> R.string.cls
    FilterType.SCHOOL -> R.string.school
}