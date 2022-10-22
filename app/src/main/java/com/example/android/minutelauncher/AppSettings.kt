package com.example.android.minutelauncher

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.collections.immutable.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
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
  val favoriteApps: PersistentList<UserApp> = persistentListOf(),
  @Polymorphic
  @Serializable(PersistentListSerializer::class)
  val gestureActions: PersistentList<GestureAction> = persistentListOf(),
  @Polymorphic
  @Serializable(PersistentMapSerializer::class)
  val shortcutApps: PersistentMap<GestureAction, UserApp> = persistentMapOf()
)

@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentList::class)
class PersistentListSerializer(
  private val dataSerializer: KSerializer<UserApp>
) : KSerializer<PersistentList<UserApp>> {

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

@OptIn(ExperimentalSerializationApi::class)
@kotlinx.serialization.Serializer(forClass = PersistentMap::class)
class PersistentMapSerializer(
  private val keySerializer: KSerializer<GestureAction>,
  private val valueSerializer: KSerializer<UserApp>
) : KSerializer<PersistentMap<GestureAction, UserApp>> {

  private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<UserApp>>() {
    @ExperimentalSerializationApi
    override val serialName: String = "kotlinx.serialization.immutable.persistentList"
  }

  override val descriptor: SerialDescriptor = PersistentListDescriptor()
  override fun serialize(encoder: Encoder, value: PersistentMap<GestureAction, UserApp>) {
    return MapSerializer(keySerializer, valueSerializer).serialize(encoder, value.toMap())
  }

  override fun deserialize(decoder: Decoder): PersistentMap<GestureAction, UserApp> {
    return MapSerializer(keySerializer, valueSerializer).deserialize(decoder).toPersistentMap()
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
