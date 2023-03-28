package server

import com.google.gson.Gson
import database.SQLiteMessengerRepository
import database.entities.Message
import database.entities.Room
import database.entities.User
import server.entities.*
import utils.SecurityUtilsImpl
import java.util.Calendar

class HttpResponse {

    private val messengerRepository = SQLiteMessengerRepository()

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
            val body = Gson().fromJson(
                request.body,
                SignUpRequestBody::class.java
            )

            messengerRepository.signUp(
                email = body.email,
                username = body.username,
                password = body.password
            )
            Gson().toJson(SignInResponseBody("200 OK"))
        } catch (e: Exception) {
            Gson().toJson(SignInResponseBody("500 ERROR"))
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
            val body = Gson().fromJson(
                request.body,
                SignInRequestBody::class.java
            )

            val user = messengerRepository.signIn(
                email = body.email,
                password = body.password
            )
            val token = messengerRepository.securityUtils.bytesToString(user.token)
            Gson().toJson(SignInResponseBody(token))
        } catch (e: Exception) {
            Gson().toJson(SignInResponseBody("500 ERROR"))
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
            val body = Gson().fromJson(
                request.body,
                RoomMessagesRequestBody::class.java
            )

            val room = messengerRepository.getRoomByToken(body.room)
            val messages = messengerRepository.getMessagesByRoom(room)

            result = Gson().toJson(RoomMessagesResponseBody(messages))
        } catch (e: Exception) {
            Gson().toJson(RoomMessagesResponseBody(listOf()))
        }

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
            val body = Gson().fromJson(
                request.body,
                GetRoomsRequestBody::class.java
            )

            val user = messengerRepository.getUserByToken(body.user)

            val rooms = messengerRepository.getRoomsByUser(user)
            result = Gson().toJson(GetRoomsResponseBody(rooms))
        } catch (e: Exception) {
            Gson().toJson(GetRoomsResponseBody(listOf()))
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
            val body = Gson().fromJson(
                request.body,
                AddRoomRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)

            val token = messengerRepository.addRoomByTwoUsers(user1, user2)

            Gson().toJson(AddRoomResponseBody(token = SecurityUtilsImpl().bytesToString(token)))
        } catch (e: Exception) {
            Gson().toJson(AddRoomResponseBody("ERROR"))
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
            val body = Gson().fromJson(
                request.body,
                AddMessageRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.owner)
            val user2 = messengerRepository.getUserByToken(body.receiver)
            val room = messengerRepository.getRoomByTwoUsers(user1, user2)

            val img = body.image.ifEmpty {
                "no image"
            }

            messengerRepository.addMessage(Message(
                room = room,
                image = img,
                value = body.value,
                file = body.file.toByteArray(),
                time = Calendar.getInstance().time.toString(),
                owner = messengerRepository.getUserByToken(body.owner),
                from = body.from
            ))
            result = Gson().toJson(AddMessageResponseBody("200 OK"))
        } catch (e: Exception) {
            result = Gson().toJson(AddMessageResponseBody("500 ERROR"))
        }

        return  result
    }

    fun handleGetAllUsersRequest(): String {

        val result: String = try {
            Gson().toJson(GetUsersResponseBody(messengerRepository.getAllUsers()))
        } catch (e: Exception) {
            Gson().toJson(AddMessageResponseBody("ERROR"))
        }

        return  result
    }

    fun handleDeleteRoomRequest(request: HttpRequest): String {
        var result: String

        try {
            val body = Gson().fromJson(
                request.body,
                DeleteRoomRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)

            messengerRepository.deleteRoomByTwoUsers(
                user1 = user1,
                user2 = user2
            )
            result = Gson().toJson(DeleteRoomResponseBody("200 OK"))
        } catch (e: Exception) {
            result = Gson().toJson(DeleteRoomResponseBody("ERROR"))
        }

        return  result
    }

    fun handleGetUsernameRequest(request: HttpRequest): String {

        val result: String = try {
            val body = Gson().fromJson(
                request.body,
                GetUsernameRequestBody::class.java
            )
            val user = messengerRepository.getUserByToken(body.token)
            Gson().toJson(GetUsernameResponseBody(user.username))
        } catch (e: Exception) {
            Gson().toJson(GetUsernameResponseBody("500 ERROR"))
        }

        return result
    }

    fun handleGetRoomRequest(request: HttpRequest): String {

        val result: String = try {
            val body = Gson().fromJson(
                request.body,
                GetRoomRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)
            val room = messengerRepository.getRoomByTwoUsers(user1, user2)
            Gson().toJson(GetRoomResponseBody(room))
        } catch (e: Exception) {
            Gson().toJson(GetRoomResponseBody(Room()))
        }

        return result
    }

    fun handleGetUserRequest(request: HttpRequest): String {
        val result: String = try {
            val body = Gson().fromJson(
                request.body,
                GetUserRequestBody::class.java
            )
            Gson().toJson(GetUserResponseBody(
                user = messengerRepository.getUserByToken(body.token)
            ))
        } catch (e: Exception) {
            Gson().toJson(GetUserResponseBody(User()))
        }

        return result
    }
}