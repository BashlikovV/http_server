package model.messages

import database.SQLiteContract
import database.SQLiteMessengerRepository
import database.entities.Message
import server.entities.*
import utils.SecurityUtilsImpl
import java.io.*
import java.nio.ByteBuffer
import java.util.*

class SQLiteMessagesRepository(
    databaseUrl: String = SQLiteContract.MESSENGER_SQLITE_DATABASE_URL
) : MessagesRepository {

    private val messengerRepository = SQLiteMessengerRepository(databaseUrl)

    override fun getRoomMessages(body: RoomMessagesRequestBody): RoomMessagesResponseBody {
        return try {
            val room = messengerRepository.getRoomByToken(body.room)
            val messages = messengerRepository.getMessagesByRoom(room, body.pagination)

            RoomMessagesResponseBody(messages, messages.count { !it.isRead })
        } catch (e: Exception) {
            e.printStackTrace()
            RoomMessagesResponseBody(listOf(), 0)
        }
    }

    override fun addMessage(body: AddMessageRequestBody): AddMessageResponseBody {
        return try {
            val user1 = messengerRepository.getUserByToken(body.owner)
            val user2 = messengerRepository.getUserByToken(body.receiver)
            val room = messengerRepository.getRoomByTwoUsers(user1, user2)

            messengerRepository.addMessage(
                Message(
                    room = room,
                    image = body.image,
                    value = body.value.encodeToByteArray(),
                    file = body.file,
                    time = Calendar.getInstance().time.toString(),
                    owner = messengerRepository.getUserByToken(body.owner),
                    from = body.from
                )
            )

            AddMessageResponseBody("200 OK")
        } catch (e: Exception) {
            e.printStackTrace()
            AddMessageResponseBody("500 ERROR")
        }
    }

    override fun deleteMessage(body: DeleteMessageRequestBody): DeleteMessageResponseBody {
        return try {
            deleteImage(body.message.image)
            messengerRepository.deleteMessage(body.message)

            DeleteMessageResponseBody("200 OK")
        } catch (e: Exception) {
            e.printStackTrace()
            DeleteMessageResponseBody("500 ERROR")
        }
    }

    override fun getImage(imageUri: String): ByteArray {
        return getImageByUri(uri = imageUri)
    }

    override fun addImage(body: AddImageRequestBody): AddImageResponseBody {
        val fileName = if (body.owner.decodeToString().contains("@")) {
            "${body.owner.decodeToString()}.jpg"
        } else {
            "image${messengerRepository.getMaxId() + 1}.jpg"
        }

        return try {
            messengerRepository.addImage(fileName)

            val file = File(
                "${Repository.IMAGES_DIRECTORY}$fileName"
            )
            file.createNewFile()
            FileOutputStream(file).write(ByteArrayInputStream(body.image).readBytes())
            if (!body.owner.decodeToString().contains("@")) {
                messengerRepository.addMessage(
                    Message(
                        room = messengerRepository.getRoomByToken(SecurityUtilsImpl().bytesToString(body.room)),
                        image = fileName,
                        value = fileName
                            .encodeToByteArray(),
                        owner = messengerRepository.getUserByToken(SecurityUtilsImpl().bytesToString(body.owner)),
                        from = SecurityUtilsImpl().bytesToString(body.owner),
                        time = Calendar.getInstance().time.toString(),
                        file = "no file".encodeToByteArray()
                    )
                )
            }

            AddImageResponseBody(fileName.encodeToByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            AddImageResponseBody(fileName.encodeToByteArray())
        }
    }

    override fun readMessages(body: ReadMessagesRequestBody): ReadMessagesResponseBody {
        return try {
            val room = messengerRepository.getRoomByToken(body.room)
            messengerRepository.readRoomMessages(room)

            ReadMessagesResponseBody("200 OK")
        } catch (e: Exception) {
            e.printStackTrace()
            ReadMessagesResponseBody("500 ERROR")
        }
    }

    private fun deleteImage(image: String) {
        if (image != "no image" && image.isNotEmpty()) {
            try {
                File(Repository.IMAGES_DIRECTORY + image).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getImageByUri(uri: String): ByteArray {
        val image = File(Repository.IMAGES_DIRECTORY + uri)
        if (image.exists()) {
            val imageIo = FileInputStream(image)
            val byteArrayOutputStream = ByteArrayOutputStream()
            byteArrayOutputStream.write(imageIo.readBytes())
            byteArrayOutputStream.flush()
            val size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array()
            return size + byteArrayOutputStream.toByteArray()
        } else {
            throw FileNotFoundException()
        }
    }
}