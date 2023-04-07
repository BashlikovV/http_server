package server

import com.google.gson.Gson
import database.SQLiteMessengerRepository
import database.entities.Message
import database.entities.Room
import database.entities.User
import server.entities.*
import utils.SecurityUtilsImpl
import java.util.*

class HttpResponse {

    private val messengerRepository = SQLiteMessengerRepository()

    private val gson = Gson()

    var headers: MutableMap<String, String> = mutableMapOf(
        "Server" to "http_server",
        "Connection" to "Close"
    )
        private set
    private var body: String = ""
    var statusCode: Int = 200
    var status : String = "Ok"

    fun setBody(value: String) {
        this.headers["Content-Length"] = value.length.toString()
        this.body = value
    }

    companion object {
        private const val NEW_LINE = "\r\n"
    }

    fun addHeader(key: String, value: String) {
        this.headers[key] = value
    }

    private fun message(): String {
        val stringBuilder = StringBuilder()
            .append("HTTP/1.1 ")
            .append("$statusCode ")
            .append("$status ")
            .append(NEW_LINE)

        headers.entries.forEach { entry ->
            stringBuilder.append(entry.key)
                .append(": ")
                .append(entry.value)
                .append(NEW_LINE)
        }

        return stringBuilder.append(NEW_LINE).append(body).toString()
    }

    fun getBytes(): ByteArray {
        return message().encodeToByteArray()
    }

    /**
     * POST /sign-up
     * {
     *      "username":"<username>"
     *      "email":"<email>",
     *      "password":"<password>"
     * }
     * */
    fun handleSignUpRequest(request: HttpRequest): String {
        return try {
            val body = gson.fromJson(
                request.body,
                SignUpRequestBody::class.java
            )

            messengerRepository.signUp(
                email = body.email,
                username = body.username,
                password = body.password
            )
            gson.toJson(SignInResponseBody("200 OK"))
        } catch (e: Exception) {
            gson.toJson(SignInResponseBody("500 ERROR"))
        }
    }

    /**
     * POST /sign-in
     * {
     *      "email":"<email>",
     *      "password":"<password>"
     * }
     * */
    fun handleSignInRequest(request: HttpRequest): String {
        return try {
            val body = gson.fromJson(
                request.body,
                SignInRequestBody::class.java
            )

            val user = messengerRepository.signIn(
                email = body.email,
                password = body.password
            )
            val token = messengerRepository.securityUtils.bytesToString(user.token)
            gson.toJson(SignInResponseBody(token))
        } catch (e: Exception) {
            gson.toJson(SignInResponseBody("500 ERROR"))
        }
    }

    /**
     * POST /room-messages
     * {
     *      "user1":"<user_token>"
     *      "user2":"<user_token>"
     * }
     * */
    fun handleGetRoomMessagesRequest(request: HttpRequest): String {
        var result = ""

        try {
            val body = gson.fromJson(
                request.body,
                RoomMessagesRequestBody::class.java
            )

            val room = messengerRepository.getRoomByToken(body.room)
            val messages = messengerRepository.getMessagesByRoom(room, body.pagination)

            result = gson.toJson(RoomMessagesResponseBody(messages))
        } catch (e: Exception) {
            gson.toJson(RoomMessagesResponseBody(listOf()))
        }
        println("ress: ${result.length}")
        return result
    }

    /**
     * POST /get-rooms
     * {
     *      "user":"<user_token>"
     * }
     * */
    fun handleGetRoomsRequest(request: HttpRequest): String {
        var result = ""

        try {
            val body = gson.fromJson(
                request.body,
                GetRoomsRequestBody::class.java
            )

            val user = messengerRepository.getUserByToken(body.user)

            val rooms = messengerRepository.getRoomsByUser(user)
            result = gson.toJson(GetRoomsResponseBody(rooms))
        } catch (e: Exception) {
            gson.toJson(GetRoomsResponseBody(listOf()))
        }

        return result
    }

    /**
     * POST /add-room
     * {
     *      "user1":"<user_token>",
     *      "user2":"<user_token>"
     * }
     * */
    fun handleAddRoomRequest(request: HttpRequest): String {

        val result: String = try {
            val body = gson.fromJson(
                request.body,
                AddRoomRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)

            val token = messengerRepository.addRoomByTwoUsers(user1, user2)

            gson.toJson(AddRoomResponseBody(token = SecurityUtilsImpl().bytesToString(token)))
        } catch (e: Exception) {
            gson.toJson(AddRoomResponseBody("ERROR"))
        }

        return result
    }

    /**
     * POST /add-message
     * {
     *      "image":"<image>",
     *      "file":"<file>",
     *      "value": "<value>",
     *      "time":"<time>",
     *      "owner":"<user_token>",
     *      "receiver":"<user_token>"
     * }
     * */
    fun handleAddMessageRequest(request: HttpRequest): String {
        var result: String

        try {
            val body = gson.fromJson(
                request.body,
                AddMessageRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.owner)
            val user2 = messengerRepository.getUserByToken(body.receiver)
            val room = messengerRepository.getRoomByTwoUsers(user1, user2)

            messengerRepository.addMessage(Message(
                room = room,
                image = body.image,
                value = body.value.encodeToByteArray(),
                file = body.file,
                time = Calendar.getInstance().time.toString(),
                owner = messengerRepository.getUserByToken(body.owner),
                from = body.from
            ))
            result = gson.toJson(AddMessageResponseBody("200 OK"))
        } catch (e: Exception) {
            result = gson.toJson(AddMessageResponseBody("500 ERROR"))
        }

        return  result
    }

    /**
     * POST /get-users
     * */
    fun handleGetAllUsersRequest(): String {
        val result: String = try {
            gson.toJson(GetUsersResponseBody(messengerRepository.getAllUsers()))
        } catch (e: Exception) {
            gson.toJson(AddMessageResponseBody("ERROR"))
        }

        return  result
    }

    /**
     * POST /delete-room
     * {
     *      "user1":"<user_token>",
     *      "user2":"<user_token>"
     * }
     * */
    fun handleDeleteRoomRequest(request: HttpRequest): String {
        var result: String

        try {
            val body = gson.fromJson(
                request.body,
                DeleteRoomRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)

            messengerRepository.deleteRoomByTwoUsers(
                user1 = user1,
                user2 = user2
            )
            result = gson.toJson(DeleteRoomResponseBody("200 OK"))
        } catch (e: Exception) {
            result = gson.toJson(DeleteRoomResponseBody("ERROR"))
        }

        return  result
    }

    /**
     * POST /get-username
     * {
     *      "token":"<user_token>"
     * }
     * */
    fun handleGetUsernameRequest(request: HttpRequest): String {
        val result: String = try {
            val body = gson.fromJson(
                request.body,
                GetUsernameRequestBody::class.java
            )
            val user = messengerRepository.getUserByToken(body.token)
            gson.toJson(GetUsernameResponseBody(user.username))
        } catch (e: Exception) {
            gson.toJson(GetUsernameResponseBody("500 ERROR"))
        }

        return result
    }

    /**
     * POST /get-room
     * {
     *      "user1":"<user_token>",
     *      "user2":"<user_token>"
     * }
     * */
    fun handleGetRoomRequest(request: HttpRequest): String {
        val result: String = try {
            val body = gson.fromJson(
                request.body,
                GetRoomRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)
            val room = messengerRepository.getRoomByTwoUsers(user1, user2)
            gson.toJson(GetRoomResponseBody(room))
        } catch (e: Exception) {
            gson.toJson(GetRoomResponseBody(Room()))
        }

        return result
    }

    /**
     * POST /get-user
     * {
     *      "token":"<user_token>"
     * }
     * */
    fun handleGetUserRequest(request: HttpRequest): String {
        val result: String = try {
            val body = gson.fromJson(
                request.body,
                GetUserRequestBody::class.java
            )
            gson.toJson(GetUserResponseBody(
                user = messengerRepository.getUserByToken(body.token)
            ))
        } catch (e: Exception) {
            gson.toJson(GetUserResponseBody(User()))
        }

        return result
    }

    /**
     * POST /delete-message
     * {
     *      "message":"[Message]"
     * }
     * */
    fun handleDeleteMessageRequest(request: HttpRequest): String {
        val result: String = try {
            val body = gson.fromJson(
                request.body,
                DeleteMessageRequestBody::class.java
            )
            messengerRepository.deleteMessage(body.message)
            gson.toJson(DeleteMessageResponseBody("200 OK"))
        } catch (e: Exception) {
            gson.toJson(DeleteMessageResponseBody("500 ERROR"))
        }

        return result
    }
}