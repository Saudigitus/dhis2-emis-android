package org.dhis2.uicomponents.map.geometry

import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.maps.geometry.bound.BoundsGeometry
import org.dhis2.maps.geometry.bound.GetBoundingBox
import org.dhis2.maps.geometry.mapper.featurecollection.MapRelationshipsToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.ENROLLMENT
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.ENROLLMENT_UID
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI
import org.dhis2.maps.geometry.mapper.featurecollection.MapTeisToFeatureCollection.Companion.TEI_IMAGE
import org.dhis2.maps.geometry.point.MapPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonPointToFeature
import org.dhis2.maps.geometry.polygon.MapPolygonToFeature
import org.dhis2.maps.mapper.MapRelationshipToRelationshipMapModel
import org.dhis2.uicomponents.map.mocks.RelationshipUiCompomentDummy
import org.dhis2.uicomponents.map.mocks.RelationshipViewModelDummy
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Before
import org.junit.Test

class MapTeisToFeatureCollectionTest {

    private lateinit var mapTeisToFeatureCollection: MapTeisToFeatureCollection
    private val bounds: BoundsGeometry = BoundsGeometry()
    private val mapPointToFeature: MapPointToFeature = mock()
    private val mapPolygonToFeature: MapPolygonToFeature = mock()
    private val mapPolygonPointToFeature: MapPolygonPointToFeature = mock()
    private val mapRelationshipToRelationshipMapModel: MapRelationshipToRelationshipMapModel =
        mock()
    private val mapRelationshipsToFeatureCollection: MapRelationshipsToFeatureCollection = mock()
    private val boundingBox: GetBoundingBox = GetBoundingBox()

    @Before
    fun setup() {
        mapTeisToFeatureCollection =
            MapTeisToFeatureCollection(
                bounds,
                mapPointToFeature,
                mapPolygonToFeature,
                mapPolygonPointToFeature,
                mapRelationshipToRelationshipMapModel,
                mapRelationshipsToFeatureCollection
            )
    }

    @Test
    fun `Should map tei models to point feature collection`() {
        val teiList = createSearchTeiModelList()
        val featurePoint = Feature.fromGeometry(Point.fromLngLat(POINT_LONGITUDE, POINT_LATITUDE))

        whenever(mapPointToFeature.map(teiList[0].tei.geometry()!!, bounds)) doReturn Pair(
            featurePoint,
            bounds
        )

        val result = mapTeisToFeatureCollection.map(teiList, true)
        val featureCollectionResults = result.first[TEI]

        val pointFeatureResult = featureCollectionResults?.features()?.get(0)?.geometry() as Point
        val uid = featureCollectionResults.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        val image = featureCollectionResults.features()?.get(0)?.getStringProperty(TEI_IMAGE)
        assertThat(pointFeatureResult.longitude(), `is`(POINT_LONGITUDE))
        assertThat(pointFeatureResult.latitude(), `is`(POINT_LATITUDE))
        assertThat(uid, `is`(TEI_UID))
        assertThat(image, `is`(TEI_UID_IMAGE))
    }

    @Test
    fun `Should map tei models to relationship feature collection`() {
        val teiList = createSearchTeiModelWithRelationships()
        val feature = Feature.fromGeometry(
            LineString.fromLngLats(
                listOf(
                    Point.fromLngLat(POINT_LONGITUDE, POINT_LATITUDE),
                    Point.fromLngLat(POINT_LONGITUDE_ENROLLMENT, POINT_LATITUDE_ENROLLMENT)
                )
            )
        )
        val relationshipModels = listOf(RelationshipUiCompomentDummy.relationshipUiComponentModel())
        whenever(
            mapRelationshipToRelationshipMapModel.mapList(teiList[0].relationships)
        ) doReturn relationshipModels
        whenever(mapRelationshipsToFeatureCollection.map(relationshipModels)) doReturn Pair(
            mapOf(relationshipModels[0].displayName!! to FeatureCollection.fromFeature(feature)),
            boundingBox.getEnclosingBoundingBox(listOf())
        )

        val result = mapTeisToFeatureCollection.map(teiList, true)
        val featureCollectionResults =
            result.first[RelationshipUiCompomentDummy.DISPLAY_NAME_FIRST]

        val relationshipFeatureCollection =
            featureCollectionResults?.features()?.get(0)?.geometry() as LineString
        assertThat(
            relationshipFeatureCollection.coordinates()[0].longitude(),
            `is`(POINT_LONGITUDE)
        )
        assertThat(relationshipFeatureCollection.coordinates()[0].latitude(), `is`(POINT_LATITUDE))
        assertThat(
            relationshipFeatureCollection.coordinates()[1].longitude(),
            `is`(POINT_LONGITUDE_ENROLLMENT)
        )
        assertThat(
            relationshipFeatureCollection.coordinates()[1].latitude(),
            `is`(POINT_LATITUDE_ENROLLMENT)
        )
    }

    @Test
    fun `Should map tei models to point enrollment feature collection`() {
        val teiList = createSearchTeiModelWithEnrollment()
        val enrollmentFeaturePoint = Feature.fromGeometry(
            Point.fromLngLat(
                POINT_LONGITUDE_ENROLLMENT,
                POINT_LATITUDE_ENROLLMENT
            )
        )
        val teiFeaturePoint =
            Feature.fromGeometry(Point.fromLngLat(POINT_LONGITUDE, POINT_LATITUDE))

        whenever(mapPointToFeature.map(teiList[0].tei.geometry()!!, bounds)) doReturn Pair(
            teiFeaturePoint,
            bounds
        )
        whenever(
            mapPointToFeature.map(
                teiList[0].selectedEnrollment.geometry()!!,
                bounds
            )
        ) doReturn Pair(enrollmentFeaturePoint, bounds)

        val result = mapTeisToFeatureCollection.map(teiList, true)
        val featureTeiCollectionResults = result.first[TEI]
        val featureEnrollmentCollectionResults = result.first[ENROLLMENT]

        val teiPointFeatureResult =
            featureTeiCollectionResults?.features()?.get(0)?.geometry() as Point
        val uid = featureTeiCollectionResults.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(teiPointFeatureResult.longitude(), `is`(POINT_LONGITUDE))
        assertThat(teiPointFeatureResult.latitude(), `is`(POINT_LATITUDE))
        assertThat(uid, `is`(TEI_UID))

        val enrollmentUid = featureEnrollmentCollectionResults?.features()?.get(0)
            ?.getStringProperty(ENROLLMENT_UID)
        val enrollmentPointFeatureResult =
            featureEnrollmentCollectionResults?.features()?.get(0)?.geometry() as Point
        assertThat(enrollmentPointFeatureResult.longitude(), `is`(POINT_LONGITUDE_ENROLLMENT))
        assertThat(enrollmentPointFeatureResult.latitude(), `is`(POINT_LATITUDE_ENROLLMENT))
        assertThat(enrollmentUid, `is`("enrollment_uid"))
    }

    @Test
    fun `Should map tei models to polygon enrollment feature collection`() {
        val teiList = createSearchTeiModelListWithEnrollmentPolygon()
        val polygonTei = listOf(listOf(Point.fromLngLat(POINT_LONGITUDE, POINT_LATITUDE)))
        val polygonEnrollment =
            listOf(listOf(Point.fromLngLat(POINT_LONGITUDE_ENROLLMENT, POINT_LATITUDE_ENROLLMENT)))
        val teiFeaturePoint = Feature.fromGeometry(Polygon.fromLngLats(polygonTei))
        val enrollmentFeaturePoint = Feature.fromGeometry(Polygon.fromLngLats(polygonEnrollment))

        whenever(mapPolygonToFeature.map(any(), any())) doReturn Pair(
            teiFeaturePoint,
            bounds
        ) doReturn Pair(enrollmentFeaturePoint, bounds)
        whenever(mapPolygonPointToFeature.map(any())) doReturn enrollmentFeaturePoint

        val result = mapTeisToFeatureCollection.map(teiList, true)
        val featureTeiCollectionResults = result.first[TEI]
        val featureEnrollmentCollectionResults = result.first[ENROLLMENT]

        val featureTeiResult =
            featureTeiCollectionResults?.features()?.get(0)?.geometry() as Polygon
        val uidTeiFeature = featureTeiCollectionResults.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(featureTeiResult.coordinates()[0][0].longitude(), `is`(POINT_LONGITUDE))
        assertThat(featureTeiResult.coordinates()[0][0].latitude(), `is`(POINT_LATITUDE))
        assertThat(uidTeiFeature, `is`(TEI_UID))

        val featureTeiEnrollmentResult =
            featureTeiCollectionResults.features()?.get(1)?.geometry() as Polygon
        val uidTeiEnrollmentFeature = featureTeiCollectionResults.features()?.get(1)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(
            featureTeiEnrollmentResult.coordinates()[0][0].longitude(),
            `is`(POINT_LONGITUDE_ENROLLMENT)
        )
        assertThat(
            featureTeiEnrollmentResult.coordinates()[0][0].latitude(),
            `is`(POINT_LATITUDE_ENROLLMENT)
        )
        assertThat(uidTeiEnrollmentFeature, `is`(TEI_UID))

        val featureEnrollmentFirstResult =
            featureEnrollmentCollectionResults?.features()?.get(0)?.geometry() as Polygon
        val featureEnrollmentFirstUid = featureEnrollmentCollectionResults.features()?.get(0)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(
            featureEnrollmentFirstResult.coordinates()[0][0].longitude(),
            `is`(POINT_LONGITUDE_ENROLLMENT)
        )
        assertThat(
            featureEnrollmentFirstResult.coordinates()[0][0].latitude(),
            `is`(POINT_LATITUDE_ENROLLMENT)
        )
        assertThat(featureEnrollmentFirstUid, `is`(TEI_UID))

        val featureEnrollmentSecondResult =
            featureEnrollmentCollectionResults.features()?.get(1)?.geometry() as Polygon
        val featureEnrollmentSecondUid = featureEnrollmentCollectionResults.features()?.get(1)
            ?.getStringProperty(MapTeisToFeatureCollection.TEI_UID)
        assertThat(
            featureEnrollmentSecondResult.coordinates()[0][0].longitude(),
            `is`(POINT_LONGITUDE_ENROLLMENT)
        )
        assertThat(
            featureEnrollmentSecondResult.coordinates()[0][0].latitude(),
            `is`(POINT_LATITUDE_ENROLLMENT)
        )
        assertThat(featureEnrollmentSecondUid, `is`(TEI_UID))
    }

    private fun createSearchTeiModelListWithEnrollmentPolygon(): List<SearchTeiModel> {
        val searchTeiModel = SearchTeiModel().apply {
            tei = createTeiModel(FeatureType.POLYGON)
            setCurrentEnrollment(createTeiEnrollment(FeatureType.POLYGON))
            setProfilePicture(TEI_UID_IMAGE)
        }
        return listOf(searchTeiModel)
    }

    private fun createSearchTeiModelList(): List<SearchTeiModel> {
        val searchTeiModel = SearchTeiModel().apply {
            tei = createTeiModel(FeatureType.POINT)
            setProfilePicture(TEI_UID_IMAGE)
        }
        return listOf(searchTeiModel)
    }

    private fun createSearchTeiModelWithEnrollment(): List<SearchTeiModel> {
        val searchTeiModel = SearchTeiModel().apply {
            tei = createTeiModel(FeatureType.POINT)
            setCurrentEnrollment(createTeiEnrollment(FeatureType.POINT))
        }
        return listOf(searchTeiModel)
    }

    private fun createSearchTeiModelWithRelationships(): List<SearchTeiModel> {
        val searchTeiModel = SearchTeiModel().apply {
            tei = createTeiModel(FeatureType.POINT)
            relationships = listOf(RelationshipViewModelDummy.getViewModelTypeFull())
        }
        return listOf(searchTeiModel)
    }

    private fun createTeiEnrollment(type: FeatureType): Enrollment {
        return Enrollment.builder()
            .id(1L)
            .uid("enrollment_uid")
            .trackedEntityInstance("tracked_entity_instance")
            .geometry(
                Geometry.builder()
                    .type(type)
                    .coordinates("[$POINT_LONGITUDE_ENROLLMENT, $POINT_LATITUDE_ENROLLMENT]")
                    .build()
            )
            .build()
    }

    private fun createTeiModel(type: FeatureType): TrackedEntityInstance {
        return TrackedEntityInstance.builder().uid(TEI_UID)
            .geometry(
                Geometry.builder().coordinates("[$POINT_LONGITUDE,$POINT_LATITUDE]")
                    .type(type).build()
            ).build()
    }

    companion object {
        const val POINT_LONGITUDE = 43.34532
        const val POINT_LATITUDE = -23.98234
        const val POINT_LONGITUDE_ENROLLMENT = 47.34532
        const val POINT_LATITUDE_ENROLLMENT = -27.98234
        const val TEI_UID = "123"
        const val TEI_UID_IMAGE = "/random/path"
    }
}
