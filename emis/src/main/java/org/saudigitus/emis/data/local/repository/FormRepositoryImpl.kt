package org.saudigitus.emis.data.local.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.dataElement
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.date.DateUtils
import org.dhis2.form.ui.provider.HintProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.saudigitus.emis.data.local.FormRepository
import org.saudigitus.emis.data.model.EventTuple
import org.saudigitus.emis.data.model.Option
import org.saudigitus.emis.ui.form.FormData
import org.saudigitus.emis.ui.form.FormField
import org.saudigitus.emis.utils.Constants
import org.saudigitus.emis.utils.DateHelper
import org.saudigitus.emis.utils.optionByOptionSet
import timber.log.Timber
import javax.inject.Inject

class FormRepositoryImpl
@Inject constructor(
    private val d2: D2,
    private val hintProvider: HintProvider
): FormRepository {

    private fun getAttributeOptionCombo() =
        d2.categoryModule().categoryOptionCombos()
            .byDisplayName().eq(Constants.DEFAULT).one().blockingGet().uid()

    private fun createEventProjection(
        tei: String,
        ou: String,
        program: String,
        programStage: String
    ): String {
        val enrollment = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(tei)
            .one().blockingGet()

        return d2.eventModule().events()
            .blockingAdd(
                EventCreateProjection.builder()
                    .organisationUnit(ou)
                    .program(program).programStage(programStage)
                    .attributeOptionCombo(getAttributeOptionCombo())
                    .enrollment(enrollment.uid()).build()
            )
    }

    private fun eventUid(
        tei: String,
        ou: String,
        program: String,
        programStage: String
    ): String? {
        return d2.eventModule().events()
            .byTrackedEntityInstanceUids(listOf(tei))
            .byProgramUid().eq(program)
            .byOrganisationUnitUid().eq(ou)
            .byProgramStageUid().eq(programStage)
            .one().blockingGet()?.uid()
    }

    override suspend fun save(
        eventTuple: EventTuple
    ): Unit = withContext(Dispatchers.IO) {
        try {
            val uid = eventUid(
                eventTuple.tei,
                eventTuple.ou,
                eventTuple.program,
                eventTuple.programStage
            ) ?: createEventProjection(
                eventTuple.tei,
                eventTuple.ou,
                eventTuple.program,
                eventTuple.programStage,
            )

            d2.trackedEntityModule().trackedEntityDataValues()
                .value(uid, eventTuple.rowAction.id)
                .blockingSet(eventTuple.rowAction.value)

            val repository = d2.eventModule().events().uid(uid)

            repository.blockingGet()
        } catch (e: Exception) {
            Timber.tag("SAVE_EVENT").e(e)
        }
    }

    override suspend fun keyboardInputTypeByStage(
        stage: String,
        dl: String
    ) = withContext(Dispatchers.IO) {
        d2.programModule().programStageDataElements()
            .byProgramStage().eq(stage)
            .byDataElement().eq(dl)
            .blockingGet()
            .map { stageDls ->
                FormField(
                    uid = stageDls.dataElement()?.uid() ?: "",
                    label = stageDls.dataElement()?.displayFormName() ?: "",
                    type = stageDls.dataElement()?.valueType(),
                    placeholder = hintProvider
                        .provideDateHint(stageDls.dataElement()?.valueType() ?: ValueType.TEXT),
                    options = getOptions(stageDls.dataElement()?.uid() ?: "").map { option ->
                        Option(
                            uid = option.uid(),
                            code = option.code(),
                            displayName = option.displayName()
                        )
                    }
                )
            }
    }

    override suspend fun getOptions(
        dataElement: String
    ): List<org.hisp.dhis.android.core.option.Option> = withContext(Dispatchers.IO) {
        val optionSet = d2.dataElement(dataElement).optionSetUid() ?: return@withContext emptyList()

        return@withContext d2.optionByOptionSet(optionSet)
    }

    @Throws(IllegalArgumentException::class)
    override suspend fun getEvents(
        ou: String,
        program: String,
        programStage: String,
        dataElement: String,
        teis: List<String>
    ): List<FormData> = withContext(Dispatchers.IO) {
        return@withContext d2.eventModule().events()
            .byTrackedEntityInstanceUids(teis)
            .byProgramUid().eq(program)
            .byProgramStageUid().eq(programStage)
            .withTrackedEntityDataValues()
            .blockingGet()
            .mapNotNull {
                eventsTransform(it, dataElement)
            }
    }

    private suspend fun eventsTransform(
        event: Event,
        dataElement: String
    ): FormData? {
        val dataValue = event.trackedEntityDataValues()?.find { it.dataElement() == dataElement }
        val tei = d2.enrollment(event.enrollment().toString()).trackedEntityInstance() ?: ""

        val dl = d2.dataElement(dataElement)
        val options = this.getOptions(dataElement)

        return if (dataValue != null) {
            FormData(
                tei = tei,
                dataElement = dl.uid(),
                value = dataValue.value(),
                date = DateHelper.formatDate(
                    event.eventDate()?.time ?: DateUtils.getInstance().today.time
                ).toString(),
                valueType = dl.valueType(),
                hasOptions = options.isNotEmpty(),
                itemOptions = if (options.isNotEmpty()) {
                    val opt = options.find { option -> option.code() == dataValue.value() }

                    Option(
                        uid = opt?.uid() ?: "",
                        code = opt?.code(),
                        displayName = opt?.displayName()
                    )
                } else null
            )
        } else null
    }
}