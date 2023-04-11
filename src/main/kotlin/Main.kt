import server.*

fun main() {
    Server { request: HttpRequest?, response: HttpResponse? ->
        HttpHandlerImpl().handle(request!!, response!!)
    }.bootstrap()
}