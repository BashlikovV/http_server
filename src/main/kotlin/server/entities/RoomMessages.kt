package server.entities

import database.entities.Message

data class RoomMessagesRequestBody(
    val room: String
)

data class RoomMessagesResponseBody(
    val messages: List<Message>
)