package digital.raywel.japchai.stomp_client

sealed class StompEvent {
    object Connected : StompEvent()
    data class Disconnected(val reason: String? = null) : StompEvent()
    data class Error(val throwable: Throwable) : StompEvent()

    data class Message(
        val destination: String?,
        val body: String,
        val headers: Map<String, String>
    ) : StompEvent()

    data class Receipt(val receiptId: String) : StompEvent()
}