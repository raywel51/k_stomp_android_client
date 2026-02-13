package digital.raywel.japchai.stomp_client

data class StompFrame(
    val command: String,
    val headers: Map<String, String>,
    val body: String?
)