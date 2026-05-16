package phonedown.core.backup

import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val APPLICATION_JSON = "application/json; charset=UTF-8"
private const val DRIVE_BACKUP_FILE_NAME = "phone-down-backup-v1.json"

data class DriveBackupFile(
    val id: String,
    val name: String,
)

class DriveAppDataClient {
        fun listBackupFiles(accessToken: String): List<DriveBackupFile> {
            val query = "'appDataFolder' in parents and name = '$DRIVE_BACKUP_FILE_NAME' and trashed = false"
            val url =
                "$DRIVE_FILES_ENDPOINT?" +
                    "spaces=${urlEncode("appDataFolder")}&" +
                    "q=${urlEncode(query)}&" +
                    "fields=${urlEncode("files(id,name)")}&" +
                    "orderBy=${urlEncode("modifiedTime desc")}"
            val body = executeRequest(accessToken, url).body
            val filesArray =
                Json
                    .parseToJsonElement(body)
                    .jsonObject["files"]
                    ?.jsonArray
                    .orEmpty()
            return filesArray.mapNotNull { element ->
                val obj = element.jsonObject
                val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val name = obj["name"]?.jsonPrimitive?.content ?: DRIVE_BACKUP_FILE_NAME
                DriveBackupFile(id = id, name = name)
            }
        }

        fun downloadBackup(
            accessToken: String,
            fileId: String,
        ): String {
            val response = executeRequest(accessToken, "$DRIVE_FILES_ENDPOINT/$fileId?alt=media")
            return response.body
        }

        fun createBackup(
            accessToken: String,
            jsonPayload: String,
        ): DriveBackupFile {
            val boundary = "phone-down-backup-${System.currentTimeMillis()}"
            val metadata =
                """
                {
                  "name": "$DRIVE_BACKUP_FILE_NAME",
                  "parents": ["appDataFolder"],
                  "mimeType": "application/json"
                }
                """.trimIndent()
            val body =
                buildString {
                    append("--").append(boundary).append(CRLF)
                    append("Content-Type: application/json; charset=UTF-8").append(CRLF).append(CRLF)
                    append(metadata).append(CRLF)
                    append("--").append(boundary).append(CRLF)
                    append("Content-Type: application/json; charset=UTF-8").append(CRLF).append(CRLF)
                    append(jsonPayload).append(CRLF)
                    append("--").append(boundary).append("--").append(CRLF)
                }.toByteArray(StandardCharsets.UTF_8)
            val response =
                executeRequest(
                    accessToken = accessToken,
                    url = "$DRIVE_UPLOAD_ENDPOINT?uploadType=multipart&fields=${urlEncode("id,name")}",
                    method = METHOD_POST,
                    contentType = "multipart/related; boundary=$boundary",
                    body = body,
                )
            val objectJson = Json.parseToJsonElement(response.body).jsonObject
            return DriveBackupFile(
                id = objectJson["id"]?.jsonPrimitive?.content ?: throw DriveApiException("Drive did not return a backup file id."),
                name = objectJson["name"]?.jsonPrimitive?.content ?: DRIVE_BACKUP_FILE_NAME,
            )
        }

        fun deleteFile(
            accessToken: String,
            fileId: String,
        ) {
            executeRequest(
                accessToken = accessToken,
                url = "$DRIVE_FILES_ENDPOINT/$fileId",
                method = METHOD_DELETE,
            )
        }

        private fun executeRequest(
            accessToken: String,
            url: String,
            method: String = METHOD_GET,
            contentType: String = APPLICATION_JSON,
            body: ByteArray? = null,
        ): DriveResponse {
            val connection =
                (URI(url).toURL().openConnection() as HttpsURLConnection).apply {
                    requestMethod = method
                    connectTimeout = NETWORK_TIMEOUT_MILLIS
                    readTimeout = NETWORK_TIMEOUT_MILLIS
                    setRequestProperty("Authorization", "Bearer $accessToken")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", contentType)
                    doInput = true
                    if (body != null) {
                        doOutput = true
                    }
                }

            try {
                if (body != null) {
                    connection.outputStream.use { output ->
                        output.write(body)
                    }
                }

                val statusCode = connection.responseCode
                val responseBody =
                    readBody(
                        connection =
                            if (statusCode in HTTP_SUCCESS_RANGE) {
                                connection
                            } else {
                                connection
                            },
                        success = statusCode in HTTP_SUCCESS_RANGE,
                    )

                if (statusCode !in HTTP_SUCCESS_RANGE) {
                    Log.w(
                        TAG,
                        "Drive request failed: method=$method url=$url status=$statusCode body=${responseBody.take(512)}",
                    )
                    throw when (statusCode) {
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        HttpURLConnection.HTTP_FORBIDDEN,
                        -> DriveUnauthorizedException(responseBody.ifBlank { "Drive authorization failed." })

                        else -> DriveApiException(responseBody.ifBlank { "Drive request failed with HTTP $statusCode." })
                    }
                }

                return DriveResponse(statusCode = statusCode, body = responseBody)
            } catch (exception: SocketTimeoutException) {
                Log.e(TAG, "Drive request timed out: method=$method url=$url", exception)
                throw DriveNetworkException("Google Drive request timed out.", exception)
            } catch (exception: IOException) {
                Log.e(TAG, "Drive request hit IOException: method=$method url=$url", exception)
                throw DriveNetworkException("Google Drive request failed due to a network problem.", exception)
            } finally {
                connection.disconnect()
            }
        }

        private fun readBody(
            connection: HttpsURLConnection,
            success: Boolean,
        ): String {
            val stream =
                if (success) {
                    connection.inputStream
                } else {
                    connection.errorStream ?: return ""
                }
            return stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        }

        private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

        private companion object {
            const val CRLF = "\r\n"
            const val METHOD_DELETE = "DELETE"
            const val METHOD_GET = "GET"
            const val METHOD_POST = "POST"
            const val NETWORK_TIMEOUT_MILLIS = 15_000
            const val DRIVE_FILES_ENDPOINT = "https://www.googleapis.com/drive/v3/files"
            const val DRIVE_UPLOAD_ENDPOINT = "https://www.googleapis.com/upload/drive/v3/files"
            val HTTP_SUCCESS_RANGE = 200..299
            const val TAG = "DriveAppDataClient"
        }
    }

data class DriveResponse(
    val statusCode: Int,
    val body: String,
)

open class DriveApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class DriveUnauthorizedException(
    message: String,
    cause: Throwable? = null,
) : DriveApiException(message, cause)

class DriveNetworkException(
    message: String,
    cause: Throwable? = null,
) : DriveApiException(message, cause)
