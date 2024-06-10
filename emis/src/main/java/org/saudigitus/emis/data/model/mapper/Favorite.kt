package org.saudigitus.emis.data.model.mapper

import org.saudigitus.emis.data.model.Favorite
import org.saudigitus.emis.data.model.Section
import org.saudigitus.emis.data.model.Stream

fun Favorite.mapStream(
    streams: List<Stream>,
) = Favorite(
    uid = this.uid,
    school = this.school,
    stream = streams,
)

fun Stream.mapSections(
    sections: List<Section>,
) = Stream(
    grade = this.grade,
    sections = sections,
)
