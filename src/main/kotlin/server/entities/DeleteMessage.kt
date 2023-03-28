package server.entities

import database.entities.Message

data class DeleteMessageRequestBody(
    val message: Message
)

data class DeleteMessageResponseBody(
    val result: String
)