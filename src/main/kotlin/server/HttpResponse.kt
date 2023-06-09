package server

import com.google.gson.GsonBuilder
import database.SQLiteContract
import model.accounts.AccountsRepository
import model.accounts.SQLiteAccountsRepository
import model.messages.MessagesRepository
import model.messages.SQLiteMessagesRepository
import model.rooms.RoomsRepository
import model.rooms.SQLiteRoomsRepository
import model.users.SQLiteUsersRepository
import model.users.UsersRepository
import server.entities.*

class HttpResponse(
    databaseUrl: String = SQLiteContract.MESSENGER_SQLITE_DATABASE_URL
) {

    private val accountsRepository: AccountsRepository = SQLiteAccountsRepository(databaseUrl)

    private val messagesRepository: MessagesRepository = SQLiteMessagesRepository(databaseUrl)

    private val roomsRepository: RoomsRepository = SQLiteRoomsRepository(databaseUrl)

    private val usersRepository: UsersRepository = SQLiteUsersRepository(databaseUrl)

    private val gson = GsonBuilder().setLenient().create()

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
     *      "password":"<password>",
     *      "image":"<image_uri>"
     * }
     * */
    fun handleSignUpRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            SignUpRequestBody::class.java
        )
        return gson.toJson(accountsRepository.signUp(body))
    }

    /**
     * POST /sign-in
     * {
     *      "email":"<email>",
     *      "password":"<password>"
     * }
     * */
    fun handleSignInRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            SignInRequestBody::class.java
        )
        return gson.toJson(accountsRepository.signIn(body))
    }

    /**
     * POST /room-messages
     * {
     *      "room":"<room_token>"
     * }
     * */
    fun handleGetRoomMessagesRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            RoomMessagesRequestBody::class.java
        )
        return gson.toJson(messagesRepository.getRoomMessages(body))
    }

    /**
     * POST /get-rooms
     * {
     *      "user":"<user_token>"
     * }
     * */
    fun handleGetRoomsRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            GetRoomsRequestBody::class.java
        )
        return gson.toJson(roomsRepository.getRooms(body))
    }

    /**
     * POST /add-room
     * {
     *      "user1":"<user_token>",
     *      "user2":"<user_token>"
     * }
     * */
    fun handleAddRoomRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            AddRoomRequestBody::class.java
        )
        return gson.toJson(roomsRepository.addRoom(body))
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
        val body = gson.fromJson(
            request.body,
            AddMessageRequestBody::class.java
        )
        return gson.toJson(messagesRepository.addMessage(body))
    }

    /**
     * POST /get-users
     * */
    fun handleGetAllUsersRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            GetUsersRequestBody::class.java
        )
        return gson.toJson(usersRepository.getAllUsers(body.token))
    }

    /**
     * POST /delete-room
     * {
     *      "user1":"<user_token>",
     *      "user2":"<user_token>"
     * }
     * */
    fun handleDeleteRoomRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            DeleteRoomRequestBody::class.java
        )
        return gson.toJson(roomsRepository.deleteRoom(body))
    }

    /**
     * POST /get-username
     * {
     *      "token":"<user_token>"
     * }
     * */
    fun handleGetUsernameRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            GetUsernameRequestBody::class.java
        )
        return gson.toJson(usersRepository.getUsername(body))
    }

    /**
     * POST /get-room
     * {
     *      "user1":"<user_token>",
     *      "user2":"<user_token>"
     * }
     * */
    fun handleGetRoomRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            GetRoomRequestBody::class.java
        )
        return gson.toJson(roomsRepository.getRoom(body))
    }

    /**
     * POST /get-user
     * {
     *      "token":"<user_token>"
     * }
     * */
    fun handleGetUserRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            GetUserRequestBody::class.java
        )
        return gson.toJson(usersRepository.getUser(body))
    }

    /**
     * POST /delete-message
     * {
     *      "message":"[Message]"
     * }
     * */
    fun handleDeleteMessageRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            DeleteMessageRequestBody::class.java
        )
        return gson.toJson(messagesRepository.deleteMessage(body))
    }

    /**
     * GET /get-image?<uri_of_image>\r\nContent-Type:image/jpg \r\n\r\n
     * */
    fun handleGetImageRequest(request: HttpRequest): ByteArray {
        val imgUri = request.url.substringAfter("?")
        return messagesRepository.getImage(imageUri = imgUri)
    }

    /**
     * POST /add-image
     * * multipart body
     * {
     *      "image":"<ByteArray that represent image>",
     *      "room":"<ByteArray that represent token of room>",
     *      "owner":"<ByteArray that represent owner token>"
     * }
     * */
    fun handleAddImageRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            AddImageRequestBody::class.java
        )
        return gson.toJson(messagesRepository.addImage(body))
    }

    /**
     * POST /update-username
     * {
     *      "token":"<user_token>",
     *      "newName":"<String>"
     * }
     * */
    fun handleUpdateUsernameRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            UpdateUsernameRequestBody::class.java
        )
        return gson.toJson(usersRepository.updateUsername(body))
    }

    fun handleReadMessagesRequest(request: HttpRequest): String {
        val body = gson.fromJson(
            request.body,
            ReadMessagesRequestBody::class.java
        )
        return gson.toJson(messagesRepository.readMessages(body))
    }

    fun checkToken(token: String?) = if (token != null) {
        usersRepository.checkUserToken(token)
    } else {
        false
    }
}