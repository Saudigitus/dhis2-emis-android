package org.saudigitus.emis.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dhis2.form.bindings.toRuleDataValue
import org.dhis2.form.bindings.toRuleEngineObject
import org.dhis2.form.bindings.toRuleVariable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleEffects
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleVariable
import org.hisp.dhis.rules.models.TriggerEnvironment
import javax.inject.Inject

open class RuleEngineRepository @Inject constructor(
    private val d2: D2
) {
    suspend fun supplementaryData(ou: String) = withContext(Dispatchers.IO)  {
        val suppData = HashMap<String, List<String>>()

        d2.organisationUnitModule().organisationUnits()
            .withOrganisationUnitGroups()
            .uid(ou).blockingGet()
            .let { orgUnit ->
                orgUnit.organisationUnitGroups()?.map {
                    if (it.code() != null) {
                        suppData[it.code()!!] = listOf(orgUnit.uid())
                    }
                    suppData[it.uid()] = listOf(orgUnit.uid())
                }
            }

        return@withContext suppData
    }

    suspend fun ruleVariables(program: String) = withContext(Dispatchers.IO) {
        return@withContext d2.programModule().programRuleVariables()
            .byProgramUid().eq(program)
            .blockingGet()
            .map {
                it.toRuleVariable(
                    d2.trackedEntityModule().trackedEntityAttributes(),
                    d2.dataElementModule().dataElements(),
                    d2.optionModule().options()
                )
            }
    }

    suspend fun rules(program: String) = withContext(Dispatchers.IO) {
        return@withContext d2.programModule().programRules()
            .byProgramUid().eq(program)
            .withProgramRuleActions()
            .blockingGet()
            .map {
                it.toRuleEngineObject()
            }
    }

    suspend fun constants() = withContext(Dispatchers.IO) {
        return@withContext d2.constantModule()
            .constants().blockingGet()
            .associate { constant ->
                Pair(constant.uid(), "${constant.value()}")
            }
    }

    @Suppress("DEPRECATION")
    private suspend fun ruleEvents(
        ou: String,
        program: String
    ) = withContext(Dispatchers.IO) {
        return@withContext d2.eventModule().events()
            .byOrganisationUnitUid().eq(ou)
            .byProgramUid().eq(program)
            .withTrackedEntityDataValues()
            .blockingGet()
            .map { event ->
                RuleEvent.builder()
                    .event(event.uid())
                    .programStage(event.programStage())
                    .programStageName(
                        d2.programModule().programStages().uid(event.programStage())
                            .blockingGet()!!.name()
                    )
                    .status(
                        if (event.status() == EventStatus.VISITED) {
                            RuleEvent.Status.ACTIVE
                        } else {
                            RuleEvent.Status.valueOf(event.status()!!.name)
                        }
                    )
                    .eventDate(event.eventDate())
                    .dueDate(
                        if (event.dueDate() != null) {
                            event.dueDate()
                        } else {
                            event.eventDate()
                        }
                    )
                    .organisationUnit(event.organisationUnit())
                    .organisationUnitCode(
                        d2.organisationUnitModule().organisationUnits().uid(
                            event.organisationUnit()
                        ).blockingGet()!!.code()
                    )
                    .dataValues(
                        event.trackedEntityDataValues()?.toRuleDataValue(
                            event,
                            d2.dataElementModule().dataElements(),
                            d2.programModule().programRuleVariables(),
                            d2.optionModule().options()
                        )
                    )
                    .build()
            }
    }

    suspend fun ruleContext(
        ruleVariables: List<RuleVariable>,
        rules: List<Rule>,
        supplementaryData: Map<String, List<String>>,
        constants: Map<String, String>,
        ruleEvents: List<RuleEvent>
    ) = withContext(Dispatchers.IO) {
        return@withContext RuleEngineContext.builder()
            .ruleVariables(ruleVariables)
            .rules(rules)
            .supplementaryData(supplementaryData)
            .constantsValue(constants)
            .build()
            .toEngineBuilder()
            .triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
            .events(ruleEvents)
            .build()
    }

    /**
     *
     */
    suspend fun evaluate(
        ou: String,
        program: String
    ): List<RuleEffects> = withContext(Dispatchers.IO) {
        val rules = rules(program)
        val ruleVariables = ruleVariables(program)
        val constants = constants()
        val supplementaryData = supplementaryData(ou)
        val ruleEvents = ruleEvents(ou, program)

        return@withContext ruleContext(
            ruleVariables,
            rules,
            supplementaryData,
            constants,
            ruleEvents
        )
        .evaluate()
        .call()
    }
}