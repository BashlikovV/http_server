package server

import java.util.Collections

class HttpRequest(
    message: String,
    isMultipartBody: Boolean
) {

    val method: HttpMethod
    val url: String
    val headers: Map<String, String>
    val body: String
    var length: Int = 0

    companion object {
        private const val DELIMITER = "\r\n\r\n"
        private const val NEW_LINE = "\r\n"
        private const val HEADER_DELIMITER = ":"
    }

    init {
        val parts = message.split(DELIMITER)
        length = parts.sumOf { it.length }
        val head = if (isMultipartBody) {
            parts.first() + parts[1]
        } else {
            parts.first()
        }
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

        this.body = if (parts.size > 1) {
            try {
                String(
                    parts.last().toByteArray().filter { it != 0.toByte() }.toByteArray()
                )
            } catch (_: Exception) {
                ""
            }
        } else {
            ""
        }
    }
}