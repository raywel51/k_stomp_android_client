package digital.raywel.japchai.kstomp.model

data class ChatMessage(
    val sender: String,
    val content: String,
    val ts: Long = System.currentTimeMillis()
)

