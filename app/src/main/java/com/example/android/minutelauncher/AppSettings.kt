package com.example.android.minutelauncher

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class AppSettings(
  @Polymorphic
  @Serializable(PersistentListSerializer::class)
  val favoriteApps: PersistentList<UserApp> = persistentListOf()
)

@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class PersistentListSerializer(private val dataSerializer: KSerializer<UserApp>) :
  KSerializer<PersistentList<UserApp>> {
  private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<UserApp>>() {
    @ExperimentalSerializationApi
    override val serialName: String = "kotlinx.serialization.immutable.persistentList"
  }

  override val descriptor: SerialDescriptor = PersistentListDescriptor()
  override fun serialize(encoder: Encoder, value: PersistentList<UserApp>) {
    return ListSerializer(dataSerializer).serialize(encoder, value.toList())
  }

  override fun deserialize(decoder: Decoder): PersistentList<UserApp> {
    return ListSerializer(dataSerializer).deserialize(decoder).toPersistentList()
  }
}

object AppSettingsSerializer : Serializer<AppSettings> {

  override val defaultValue: AppSettings
    get() = AppSettings()

  override suspend fun readFrom(input: InputStream): AppSettings {
    try {
      return Json.decodeFromString(
        deserializer = AppSettings.serializer(),
        string = input.readBytes().decodeToString()
      ).also { Log.d("APP_SETTINGS", it.favoriteApps.toString()) }
    } catch (e: SerializationException) {
      throw CorruptionException("Unable to read AppSettings", e)
    }
  }

  override suspend fun writeTo(t: AppSettings, output: OutputStream) {
    withContext(Dispatchers.IO) {
      output.write(
        Json.encodeToString(
          serializer = AppSettings.serializer(),
          value = t
        ).encodeToByteArray().also { Log.d("APP_SETTINGS", t.favoriteApps.toString()) }
      )
    }
  }
}
