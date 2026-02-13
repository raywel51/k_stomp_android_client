package digital.raywel.japchai.stomp_client

data class StompSubscription(
    val id: String,
    val destination: String,
    val headers: Map<String, String>
)