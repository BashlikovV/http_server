package server

import com.google.gson.Gson
import database.SQLiteMessengerRepository

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
     * -H "email: <email>"
     * -H "username: <username>"
     * -H "password: <password>"
     * */
    fun handleSignUpRequest(request: HttpRequest): String {
        return try {
            messengerRepository.signUp(
                email = request.headers["email"] ?: "",
                username = request.headers["username"] ?: "",
                password = request.headers["password"] ?: ""
            )
            setBody("result: OK")
            message()
        } catch (e: Exception) {
            statusCode = 500
            status = "Error, can not create user"
            message()
        }
    }

    /**
     * POST /sign-in
     * -H "email: <email>"
     * -H "password: <password>"
     * */
    fun handleSignInRequest(request: HttpRequest): String {
        return try {
            val user = messengerRepository.signIn(
                email = request.headers["email"] ?: "",
                password = request.headers["password"] ?: ""
            )
            val token = messengerRepository.securityUtils.bytesToString(user.token)
            setBody("token: $token")
            message()
        } catch (e: Exception) {
            statusCode = 500
            status = "Error, can not create user"
            message()
        }
    }

    /**
     * POST /room-messages
     * -H "user1: <user_token>"
     * -H "user2: <user_token>"
     * */
    fun handleGetRoomMessagesRequest(request: HttpRequest): String {
        var result = ""

        try {
            val user1 = messengerRepository.getUserByToken(
                token = request.headers["user1"] ?: ""
            )
            val user2 = messengerRepository.getUserByToken(
                token = request.headers["user2"] ?: ""
            )
            val room = messengerRepository.getRoomByTwoUsers(
                user1 = user1,
                user2 = user2
            )
            val messages = messengerRepository.getMessagesByRoom(room)

            result = Gson().toJson(messages)
        } catch (e: Exception) {
            statusCode = 500
            status = "Error, can not get messages"
            message()
        }

        return result
    }
}