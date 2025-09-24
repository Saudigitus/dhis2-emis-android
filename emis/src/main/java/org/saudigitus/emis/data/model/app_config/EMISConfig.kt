package org.saudigitus.emis.data.model.app_config

import com.fasterxml.jackson.databind.ObjectMapper
import org.saudigitus.emis.utils.Mapper

class EMISConfig {
    private fun toJson(): String = Mapper.translateJsonToObject().writeValueAsString(this)

    companion object {
        fun fromJson(json: String?): List<EMISConfigItem>? = if (json != null) {
            val mapper = ObjectMapper()

            Mapper.translateJsonToObject()
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
                Mapper.translateJsonToObject()
                    .readValue(json, T::class.java)
            } else {
                null
            }
    }
}