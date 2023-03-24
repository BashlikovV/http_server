import database.SQLiteMessengerRepository
import database.entities.Message
import server.HttpContract
import server.HttpRequest
import server.HttpResponse
import server.Server
import java.util.*

fun main() {
    Server { request: HttpRequest?, response: HttpResponse? ->
        when(request?.url?.substringBefore("?")?.substringAfter("/")) {
            HttpContract.UrlMethods.SIGN_UP -> {
                response?.handleSignUpRequest(request = request)
            }
            HttpContract.UrlMethods.SIGN_IN -> {
                response?.handleSignInRequest(request = request)
            }
            HttpContract.UrlMethods.ROOM_MESSAGES -> {
                response?.handleGetRoomMessagesRequest(request = request)
            }
            else -> { "Error" }
        }
    }.bootstrap()

//    val messengerRepository = SQLiteMessengerRepository()
//    val result = messengerRepository.signIn(
//        email = "newuser@gmail.com",
//        password = "newuser"
//    )

//    messengerRepository.signUp(
//        email = "newuser@gmail.com",
//        password = "newuser",
//        username = "new user"
//    )

//    val result = messengerRepository.getUserByToken(
//        token = "fJIRGyx9VFSvENoOEHgUbwaYIAk="
//    )

//    messengerRepository.updateUsernameByToken(
//        token = "iAzjuieASfRy/kyLOSTGS8glP6k=",
//        username = "new user"
//    )

//    val result = messengerRepository.getAllUsers()

//    val user1 = messengerRepository.getUserByToken("MTm4NuLnbvRXmATDfFuvN9Qhc9M=")
//    val user2 = messengerRepository.getUserByToken("XWYIOnpXkx6m41n10l52lo4ztAQ=")
//    messengerRepository.addRoomByTwoUsers(
//        user1 = user1,
//        user2 = user2
//    )

//    val room = messengerRepository.getRoomByTwoUsers(
//        user1 = user1,
//        user2 = user2
//    )
//    println(result.toString())

//    messengerRepository.deleteRoomByTwoUsers(
//        user1 = messengerRepository.getUserByToken("8rX3Wt3FYRbcnlGM1aZy2qbCrWY="),
//        user2 = messengerRepository.getUserByToken("l3l0P+IRei8FNuTuP56F5Gnl0T0=")
//    )

//    messengerRepository.addMessage(
//        message = Message(
//            room = room,
//            image = "image_uri_3",
//            value = "test_message_3",
//            file = byteArrayOf(0, 1, 2, 3, 4, 5),
//            owner = user1,
//            time = Calendar.getInstance().time.toString()
//        )
//    )

//    messengerRepository.deleteMessage(
//        message = Message(
//            room = room,
//            image = "image_uri_2",
//            value = "test_message_1",
//            file = byteArrayOf(0, 1, 2, 3, 4, 5),
//            owner = user1,
//            time = "Fri Mar 24 13:53:36 MSK 2023"
//        )
//    )

//    val messages = messengerRepository.getMessagesByRoom(
//        room = room
//    )
//    messages.forEach {
//        println(it.value)
//    }
}