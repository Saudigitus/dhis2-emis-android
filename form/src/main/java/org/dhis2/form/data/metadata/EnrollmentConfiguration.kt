package org.dhis2.form.data.metadata

import org.dhis2.bindings.userFriendlyValue
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.enrollmentImportConflicts
import org.dhis2.commons.bindings.program
import org.dhis2.commons.bindings.tei
import org.dhis2.commons.bindings.teiAttribute
import org.dhis2.commons.bindings.trackedEntityType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class EnrollmentConfiguration(
    private val d2: D2,
    private val enrollmentUid: String,
) :
    FormBaseConfiguration(d2) {
    private val _enrollment: Enrollment? by lazy {
        d2.enrollment(enrollmentUid)
    }

    fun enrollment() = _enrollment

    fun program() = enrollment()?.program()?.let {
        d2.program(it)
    }

    fun tei() = enrollment()?.trackedEntityInstance()?.let { d2.tei(it) }
    fun trackedEntityType() = d2.trackedEntityType(program()?.trackedEntityType()?.uid()!!)
    fun sections() = d2.programModule().programSections()
        .withAttributes()
        .byProgramUid().eq(enrollment()?.program())
        .blockingGet()

    fun orgUnit(orgUnitUid: String) =
        d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).blockingGet()

    fun programAttributes() =
        d2.programModule().programTrackedEntityAttributes()
            .withRenderType()
            .byProgram().eq(enrollment()?.program())
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()

    fun programAttribute(attributeUid: String) =
        d2.programModule().programTrackedEntityAttributes().withRenderType()
            .byProgram().eq(enrollment()?.program())
            .byTrackedEntityAttribute().eq(attributeUid)
            .one().blockingGet()

    fun trackedEntityAttribute(trackedEntityAttributeUid: String) =
        d2.teiAttribute(trackedEntityAttributeUid)

    fun attributeValue(trackedEntityAttributeUid: String) =
        d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(
                trackedEntityAttributeUid,
                enrollment()?.trackedEntityInstance()!!,
            ).blockingGet()?.userFriendlyValue(d2, addPercentageSymbol = false)

    fun conflicts() = d2.enrollmentImportConflicts(enrollmentUid)

    fun fetchAutogeneratedValue(trackedEntityAttributeUid: String, orgUnitUid: String) =
        d2.trackedEntityModule().reservedValueManager()
            .blockingGetValue(trackedEntityAttributeUid, orgUnitUid)

    fun captureOrgUnitsCount() = d2.organisationUnitModule().organisationUnits()
        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
        .byProgramUids(enrollment()?.program()?.let { listOf(it) } ?: emptyList())
        .blockingCount()

    fun hasEventsGeneratedByEnrollmentDate(): Boolean {
        val stagesWithReportDateToUse = d2.programModule().programStages()
            .byProgramUid().eq(enrollment()?.program())
            .byOpenAfterEnrollment().isTrue
            .byReportDateToUse().eq("enrollmentDate")
            .blockingGetUids()
        val stagesWithGeneratedBy = d2.programModule().programStages()
            .byProgramUid().eq(enrollment()?.program())
            .byAutoGenerateEvent().isTrue
            .byGeneratedByEnrollmentDate().isTrue
            .blockingGetUids()
        return !d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().`in`(stagesWithReportDateToUse.union(stagesWithGeneratedBy))
            .blockingIsEmpty()
    }

    fun hasEventsGeneratedByIncidentDate(): Boolean {
        val stagesWithReportDateToUse = d2.programModule().programStages()
            .byProgramUid().eq(enrollment()?.program())
            .byOpenAfterEnrollment().isTrue
            .byReportDateToUse().eq("incidentDate")
            .blockingGetUids()
        val stagesWithGeneratedBy = d2.programModule().programStages()
            .byProgramUid().eq(enrollment()?.program())
            .byAutoGenerateEvent().isTrue
            .byGeneratedByEnrollmentDate().isFalse
            .blockingGetUids()
        return !d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().`in`(stagesWithReportDateToUse.union(stagesWithGeneratedBy))
            .blockingIsEmpty()
    }

    fun setValue(attributeUid: String, value: String) {
        d2.trackedEntityModule().trackedEntityAttributeValues()
            .value(attributeUid, tei()?.uid()!!)
            .blockingSet(value)
    }

    fun getValue(attributeUid: String) = d2.trackedEntityModule().trackedEntityAttributeValues()
        .value(attributeUid, tei()?.uid()!!)
        .blockingGet()
}
