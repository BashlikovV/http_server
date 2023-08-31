package server

fun interface WebSocketHandler {

    fun handle(request: HttpRequest, response: HttpResponse): ByteArray?
}