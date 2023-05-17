package model.messages

import server.entities.*

interface MessagesRepository {

    fun getRoomMessages(body: RoomMessagesRequestBody): RoomMessagesResponseBody

    fun addMessage(body: AddMessageRequestBody): AddMessageResponseBody

    fun deleteMessage(body: DeleteMessageRequestBody): DeleteMessageResponseBody

    fun getImage(imageUri: String): ByteArray

    fun addImage(body: AddImageRequestBody): AddImageResponseBody

    fun readMessages(body: ReadMessagesRequestBody): ReadMessagesResponseBody
}