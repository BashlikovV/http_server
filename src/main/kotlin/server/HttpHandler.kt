package server

fun interface HttpHandler {

    fun handle(request: HttpRequest, response: HttpResponse): String?
}