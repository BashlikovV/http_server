package server.entities

import database.entities.Message

data class RoomMessagesRequestBody(
    val user1: String,
    val user2: String
)

data class RoomMessagesResponseBody(
    val messages: List<Message>
)