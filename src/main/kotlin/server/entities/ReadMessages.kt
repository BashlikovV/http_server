package server.entities

data class ReadMessagesRequestBody(
    val room: String
)

data class ReadMessagesResponseBody(
    val result: String
)