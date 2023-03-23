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
//        username = "newuser"
//    )
}