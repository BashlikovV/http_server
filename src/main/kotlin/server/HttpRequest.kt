package server

import java.util.Collections

class HttpRequest(
    message: String
) {

    private val method: HttpMethod
    val url: String
    val headers: Map<String, String>
    private val body: String

    companion object {
        private const val DELIMITER = "\r\n\r\n"
        private const val NEW_LINE = "\r\n"
        private const val HEADER_DELIMITER = ":"
    }

    init {
        val parts = message.split(DELIMITER)
        val head = parts.first()
        val headers = head.split(NEW_LINE)
        val firstLine = headers.first().split(" ")
        method = HttpMethod.valueOf(firstLine[0])
        url = firstLine[1]

        val map = mutableMapOf<String, String>()
        for (i in 1 until headers.size) {
            val headerPart = headers[i].split(delimiters = arrayOf(HEADER_DELIMITER), ignoreCase = true, limit = 2)
            map[headerPart.first().trim()] = headerPart[1].trim()
        }
        this.headers = Collections.unmodifiableMap(map)

        val bodyLength = this.headers["Content-Length"]
        val length = bodyLength?.toInt() ?: 0
        this.body = if (parts.size > 1) {
            parts[1].trim().substring(0, length)
        } else {
            ""
        }
    }
}