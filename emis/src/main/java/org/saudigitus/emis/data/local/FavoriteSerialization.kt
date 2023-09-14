package org.saudigitus.emis.data.local

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.saudigitus.emis.data.model.FavoriteConfig
import timber.log.Timber

object FavoriteSerialization : Serializer<FavoriteConfig> {
    override val defaultValue: FavoriteConfig
        get() = FavoriteConfig()

    override suspend fun readFrom(input: InputStream): FavoriteConfig {
        return try {
            Json.decodeFromString(
                deserializer = FavoriteConfig.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: java.lang.Exception) {
            Timber.e(e)

            defaultValue
        }
    }

    override suspend fun writeTo(t: FavoriteConfig, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = FavoriteConfig.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}