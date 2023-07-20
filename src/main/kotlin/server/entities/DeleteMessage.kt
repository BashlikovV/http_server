package server.entities

import database.entities.Message

data class DeleteMessageRequestBody(
    val messages: List<Message>
)

data class DeleteMessageResponseBody(
    val result: String
)