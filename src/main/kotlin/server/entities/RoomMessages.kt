package server.entities

import database.entities.Message

data class RoomMessagesRequestBody(
    val room: String,
    val pagination: IntRange
)

data class RoomMessagesResponseBody(
    val messages: List<Message>,
    val unreadMessagesCount: Int
)