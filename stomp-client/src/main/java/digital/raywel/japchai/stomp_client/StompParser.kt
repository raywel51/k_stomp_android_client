package digital.raywel.japchai.stomp_client

internal object StompParser {

    fun parse(raw: String): List<StompFrame> {
        if (raw.trim().isEmpty()) return emptyList()

        val frames = raw.split("\u0000").filter { it.isNotBlank() }
        return frames.map { parseSingle(it) }
    }

    private fun parseSingle(frame: String): StompFrame {
        val lines = frame.split("\n")

        val command = lines.firstOrNull()?.trim().orEmpty()
        val headers = mutableMapOf<String, String>()

        var i = 1
        while (i < lines.size) {
            val line = lines[i]
            i++

            if (line.isBlank()) break

            val idx = line.indexOf(":")
            if (idx > 0) {
                val key = line.substring(0, idx).trim()
                val value = line.substring(idx + 1).trim()
                headers[key] = value
            }
        }

        val body = if (i < lines.size) {
            lines.subList(i, lines.size).joinToString("\n")
        } else null

        return StompFrame(command, headers, body)
    }

    fun build(command: String, headers: Map<String, String>, body: String?): String {
        val sb = StringBuilder()
        sb.append(command).append("\n")

        headers.forEach { (k, v) ->
            sb.append(k).append(":").append(v).append("\n")
        }

        sb.append("\n")

        if (body != null) sb.append(body)

        sb.append("\u0000")
        return sb.toString()
    }
}