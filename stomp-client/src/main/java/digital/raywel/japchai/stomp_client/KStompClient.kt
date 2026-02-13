package digital.raywel.japchai.stomp_client

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class KStompClient(
    private val url: String,
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build(),
    private val config: StompConfig = StompConfig()
) {

    private val scheduler: ScheduledExecutorService =
        Executors.newSingleThreadScheduledExecutor { r ->
            Thread(r, "stomp-client").apply { isDaemon = true }
        }

    private var ws: WebSocket? = null

    private val connected = AtomicBoolean(false)
    private val manualDisconnect = AtomicBoolean(false)

    private val sendQueue: BlockingQueue<String> = LinkedBlockingQueue()
    private val subscriptions: ConcurrentHashMap<String, StompSubscription> = ConcurrentHashMap()

    private var reconnectAttempt = 0
    private val lastServerActivity = AtomicLong(System.currentTimeMillis())

    private var heartbeatTask: ScheduledFuture<*>? = null
    private var timeoutTask: ScheduledFuture<*>? = null

    private var listener: ((StompEvent) -> Unit)? = null

    fun setListener(listener: (StompEvent) -> Unit) {
        this.listener = listener
    }

    fun connect(headers: Map<String, String> = emptyMap()) {
        manualDisconnect.set(false)
        openSocket(headers)
    }

    fun disconnect() {
        manualDisconnect.set(true)
        connected.set(false)

        heartbeatTask?.cancel(true)
        timeoutTask?.cancel(true)

        try {
            ws?.send(StompParser.build("DISCONNECT", emptyMap(), null))
        } catch (_: Exception) {}

        ws?.close(1000, "manual disconnect")
        ws = null

        listener?.invoke(StompEvent.Disconnected("manual disconnect"))
    }

    fun subscribe(destination: String, id: String = "sub-${System.nanoTime()}"): String {
        val headers = mapOf(
            "id" to id,
            "destination" to destination,
            "ack" to "auto"
        )

        subscriptions[id] = StompSubscription(id, destination, headers)

        if (connected.get()) {
            sendRaw(StompParser.build("SUBSCRIBE", headers, null))
        }

        return id
    }

    fun unsubscribe(id: String) {
        subscriptions.remove(id)
        if (connected.get()) {
            sendRaw(StompParser.build("UNSUBSCRIBE", mapOf("id" to id), null))
        }
    }

    fun unsubscribe() {
        val subsSnapshot = subscriptions.values.toList()
        subscriptions.clear()

        if (connected.get()) {
            subsSnapshot.forEach { sub ->
                sendRaw(StompParser.build("UNSUBSCRIBE", mapOf("id" to sub.id), null))
            }
        }
    }

    fun send(destination: String, body: String) {
        val headers = mapOf(
            "destination" to destination,
            "content-type" to "application/json",
            "content-length" to body.toByteArray(Charsets.UTF_8).size.toString()
        )

        sendRaw(StompParser.build("SEND", headers, body))
    }

    fun send(
        destination: String,
        body: String,
        receiptId: String
    ) {
        val headers = mapOf(
            "destination" to destination,
            "receipt" to receiptId,
            "content-type" to "application/json",
            "content-length" to body.toByteArray(Charsets.UTF_8).size.toString()
        )

        sendRaw(StompParser.build("SEND", headers, body))
    }

    private fun openSocket(connectHeaders: Map<String, String>) {
        val request = Request.Builder().url(url).build()

        ws = okHttpClient.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                lastServerActivity.set(System.currentTimeMillis())

                val headers = connectHeaders.toMutableMap().apply {
                    putIfAbsent("accept-version", "1.2")
                    putIfAbsent("host", "localhost")
                    putIfAbsent(
                        "heart-beat",
                        "${config.heartbeatSendIntervalMs},${config.serverTimeoutMs}"
                    )
                }

                webSocket.send(StompParser.build("CONNECT", headers, null))

                startHeartbeatLoop()
                startServerTimeoutMonitor()
                flushQueue()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("STOMP_RAW", text.replace("\u0000", "\\u0000"))
                lastServerActivity.set(System.currentTimeMillis())
                handleIncoming(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                lastServerActivity.set(System.currentTimeMillis())
                handleIncoming(bytes.utf8())
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                connected.set(false)
                listener?.invoke(StompEvent.Disconnected("closed: $code $reason"))
                scheduleReconnect(connectHeaders)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connected.set(false)
                listener?.invoke(StompEvent.Error(t))
                scheduleReconnect(connectHeaders)
            }
        })
    }

    private fun handleIncoming(raw: String) {
        val frames = StompParser.parse(raw)

        for (frame in frames) {
            when (frame.command) {
                "CONNECTED" -> {
                    reconnectAttempt = 0
                    connected.set(true)
                    listener?.invoke(StompEvent.Connected)

                    // resubscribe
                    subscriptions.values.forEach { sub ->
                        sendRaw(StompParser.build("SUBSCRIBE", sub.headers, null))
                    }

                    flushQueue()
                }

                "MESSAGE" -> {
                    val destination = frame.headers["destination"]
                    val body = frame.body ?: ""
                    listener?.invoke(StompEvent.Message(destination, body, frame.headers))
                }

                "RECEIPT" -> {
                    val id = frame.headers["receipt-id"] ?: "unknown"
                    listener?.invoke(StompEvent.Receipt(id))
                }

                "ERROR" -> {
                    listener?.invoke(StompEvent.Error(RuntimeException(frame.body ?: "STOMP ERROR")))
                }
            }
        }
    }

    private fun startHeartbeatLoop() {
        val interval = config.heartbeatSendIntervalMs
        if (interval <= 0) return

        heartbeatTask?.cancel(true)
        heartbeatTask = scheduler.scheduleWithFixedDelay({
            if (!connected.get()) return@scheduleWithFixedDelay
            ws?.send("\n")
        }, interval, interval, TimeUnit.MILLISECONDS)
    }

    private fun startServerTimeoutMonitor() {
        val timeout = config.serverTimeoutMs
        if (timeout <= 0) return

        timeoutTask?.cancel(true)
        timeoutTask = scheduler.scheduleWithFixedDelay({
            if (!connected.get()) return@scheduleWithFixedDelay

            val diff = System.currentTimeMillis() - lastServerActivity.get()
            if (diff > timeout) {
                connected.set(false)
                ws?.close(1001, "server timeout")
            }
        }, 5000, 5000, TimeUnit.MILLISECONDS)
    }

    private fun scheduleReconnect(connectHeaders: Map<String, String>) {
        if (manualDisconnect.get()) return
        if (!config.reconnectEnabled) return

        reconnectAttempt++
        val delay = computeBackoffDelay()

        scheduler.schedule({
            if (!manualDisconnect.get()) {
                openSocket(connectHeaders)
            }
        }, delay, TimeUnit.MILLISECONDS)
    }

    private fun computeBackoffDelay(): Long {
        val base = config.reconnectInitialDelayMs
        val delay = (base * Math.pow(config.reconnectMultiplier, (reconnectAttempt - 1).toDouble())).toLong()
        return delay.coerceAtMost(config.reconnectMaxDelayMs)
    }

    private fun sendRaw(frame: String) {
        if (connected.get()) {
            ws?.send(frame)
        } else {
            sendQueue.offer(frame)
        }
    }

    private fun flushQueue() {
        scheduler.execute {
            while (connected.get()) {
                val msg = sendQueue.poll() ?: break
                ws?.send(msg)
            }
        }
    }
}