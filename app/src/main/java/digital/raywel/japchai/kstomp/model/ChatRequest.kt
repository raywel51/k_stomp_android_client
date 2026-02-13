package digital.raywel.japchai.kstomp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChatRequest(
    val sender: String,
    val content: String
)

fun ChatRequest.toJsonString(): String {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
    return json.encodeToString(this)
}