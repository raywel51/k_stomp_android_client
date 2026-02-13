package digital.raywel.japchai.kstomp.ui.viewmodel

import androidx.lifecycle.ViewModel
import digital.raywel.japchai.kstomp.model.ChatMessage
import digital.raywel.japchai.kstomp.model.ChatRequest
import digital.raywel.japchai.kstomp.model.toJsonString
import digital.raywel.japchai.stomp_client.KStompClient
import digital.raywel.japchai.stomp_client.StompConfig
import digital.raywel.japchai.stomp_client.StompEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class ChatViewModel : ViewModel() {

    private val stomp = KStompClient(
        url = "ws://192.168.88.202:8080/ws",
        config = StompConfig(
            heartbeatSendIntervalMs = 10_000,
            serverTimeoutMs = 30_000
        )
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    private val _heartbeatTs = MutableStateFlow(0L)
    val heartbeatTs = _heartbeatTs.asStateFlow()

    var deviceName: String = ""

    init {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL

        deviceName = "$manufacturer $model"

        stomp.setListener { event ->
            when (event) {
                is StompEvent.Connected -> {
                    _connected.value = true

                    stomp.unsubscribe()
                    stomp.subscribe("/topic/rooms/room1")
                    stomp.subscribe("/topic/heartbeat")

                    Timber.i("Connected to STOMP server")
                }

                is StompEvent.Disconnected -> {
                    _connected.value = false

                    Timber.w("Disconnected from STOMP server: ${event.reason ?: "unknown reason"}")
                }

                is StompEvent.Error -> {
                    _connected.value = false

                    Timber.e(event.throwable, "Error occurred in STOMP client")
                }

                is StompEvent.Message -> {
                    if (event.destination == "/topic/rooms/room1") {
                        try {
                            val json = org.json.JSONObject(event.body)
                            val sender = json.getString("sender")
                            val content = json.getString("content")

                            _messages.value += ChatMessage(sender, content)
                        } catch (_: Exception) {}
                    }

                    if (event.destination == "/topic/heartbeat") {
                        _heartbeatTs.value = System.currentTimeMillis()
                    }

                    Timber.d("Received message from ${event.destination}: ${event.body}")
                }

                else -> {}
            }
        }

        stomp.connect()
    }

    fun sendMessage(content: String) {
        val request = ChatRequest(deviceName, content).toJsonString()

        stomp.send("/app/chat.send/room1", request)
    }

    override fun onCleared() {
        super.onCleared()
        stomp.disconnect()
    }
}