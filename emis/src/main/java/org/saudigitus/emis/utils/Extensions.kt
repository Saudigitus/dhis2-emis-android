package org.saudigitus.emis.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.option.Option
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
    optionSet: String
): List<Option> = optionModule()
    .options()
    .byOptionSetUid().eq(optionSet)
    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
    .blockingGet()


fun List<DropdownState>.getByType(type: FilterType): DropdownState? =
    find { it.filterType == type}