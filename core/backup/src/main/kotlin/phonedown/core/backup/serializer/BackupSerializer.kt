package phonedown.core.backup.serializer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import phonedown.core.backup.dto.BackupData

object BackupSerializer {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    const val CURRENT_SCHEMA_VERSION = 1

    fun serialize(data: BackupData): String = json.encodeToString(data)

    fun deserialize(jsonString: String): BackupData = json.decodeFromString(jsonString)

    fun validateSchemaVersion(data: BackupData): Boolean = data.schemaVersion == CURRENT_SCHEMA_VERSION
}
