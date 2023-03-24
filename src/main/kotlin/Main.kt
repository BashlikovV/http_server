import database.SQLiteMessengerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import server.HttpRequest
import server.HttpResponse
import server.Server

fun main() {
//    Server { _: HttpRequest?, _: HttpResponse? ->
//        "<html><body><h1>Hello, http_server!</h1></body></html>"
//    }.bootstrap()

    val messengerRepository = SQLiteMessengerRepository()
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

//    val user1 = messengerRepository.getUserByToken("8rX3Wt3FYRbcnlGM1aZy2qbCrWY=")
//    val user2 = messengerRepository.getUserByToken("l3l0P+IRei8FNuTuP56F5Gnl0T0=")
//    messengerRepository.addRoomByTwoUsers(
//        user1 = user1,
//        user2 = user2
//    )

//    val result = messengerRepository.getRoomByTwoUsers(
//        user1 = messengerRepository.getUserByToken("8rX3Wt3FYRbcnlGM1aZy2qbCrWY="),
//        user2 = messengerRepository.getUserByToken("l3l0P+IRei8FNuTuP56F5Gnl0T0=")
//    )
//    println(result.toString())

//    messengerRepository.deleteRoomByTwoUsers(
//        user1 = messengerRepository.getUserByToken("8rX3Wt3FYRbcnlGM1aZy2qbCrWY="),
//        user2 = messengerRepository.getUserByToken("l3l0P+IRei8FNuTuP56F5Gnl0T0=")
//    )
}