package org.saudigitus.emis.data.model

import com.fasterxml.jackson.databind.ObjectMapper
import org.saudigitus.emis.utils.Mapper.translateJsonToObject

class EMISConfig {
    private fun toJson(): String = translateJsonToObject().writeValueAsString(this)

    companion object {
        fun fromJson(json: String?): List<EMISConfigItem>? = if (json != null) {
            val mapper = ObjectMapper()

            translateJsonToObject()
                .readValue(
                    json,
                    mapper.typeFactory.constructCollectionType(
                        List::class.java,
                        EMISConfigItem::class.java,
                    ),
                )
        } else {
            null
        }

        inline fun <reified T> translateFromJson(json: String?): T? =
            if (json != null) {
                translateJsonToObject()
                    .readValue(json, T::class.java)
            } else {
                null
            }
    }
}
