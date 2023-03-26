package server

import com.google.gson.Gson
import database.SQLiteMessengerRepository
import database.entities.Message
import server.entities.*
import utils.SecurityUtilsImpl

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

    fun addHeaders(headers: Map<String, String>) {
        this.headers.putAll(headers)
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

            val user1 = messengerRepository.getUserByToken(
                token = body.user1
            )
            val user2 = messengerRepository.getUserByToken(
                token = body.user2
            )
            val room = messengerRepository.getRoomByTwoUsers(
                user1 = user1,
                user2 = user2
            )
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
        var result = ""

        try {
            val body = Gson().fromJson(
                request.body,
                AddRoomRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.user1)
            val user2 = messengerRepository.getUserByToken(body.user2)

            val token = messengerRepository.addRoomByTwoUsers(user1, user2)

            result = Gson().toJson(AddRoomResponseBody(token = SecurityUtilsImpl().bytesToString(token)))
        } catch (e: Exception) {
            result = Gson().toJson(AddRoomResponseBody("ERROR"))
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
        var result = ""

        try {
            val body = Gson().fromJson(
                request.body,
                AddMessageRequestBody::class.java
            )

            val user1 = messengerRepository.getUserByToken(body.owner)
            val user2 = messengerRepository.getUserByToken(body.receiver)

            messengerRepository.addMessage(Message(
                room = messengerRepository.getRoomByTwoUsers(user1, user2),
                image = body.image,
                value = body.value,
                file = body.file.toByteArray(),
                time = body.time
            ))
            result = Gson().toJson(AddMessageResponseBody("200 OK"))
        } catch (e: Exception) {
            result = Gson().toJson(AddMessageResponseBody("ERROR"))
        }

        return  result
    }

    fun handleGetAllUsersRequest(request: HttpRequest): String {
        var result = ""

        try {
            result = Gson().toJson(GetUsersResponseBody(messengerRepository.getAllUsers()))
        } catch (e: Exception) {
            result = Gson().toJson(AddMessageResponseBody("ERROR"))
        }

        return  result
    }
}