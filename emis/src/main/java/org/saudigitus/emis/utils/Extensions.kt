package org.saudigitus.emis.utils

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event

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
) = optionModule()
    .options()
    .byOptionSetUid().eq(optionSet)
    .blockingGet()

