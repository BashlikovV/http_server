package server

class WebSocketHandlerImpl : WebSocketHandler {
    override fun handle(request: HttpRequest, response: HttpResponse): ByteArray {
        return when(request.url.substringBefore("?").substringAfter("/")) {
            HttpContract.UrlMethods.GET_IMAGE -> {
                response.handleGetImageRequest(request = request)
            }
            else -> {
                byteArrayOf()
            }
        }
    }
}