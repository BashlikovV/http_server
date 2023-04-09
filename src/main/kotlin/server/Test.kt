package server

import Repository
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.ByteString
import okio.ByteString.Companion.decodeHex

fun main() {
    WebSocketEcho().run()
}

val contentType = "application/json; charset=utf-8".toMediaType()

class WebSocketEcho : WebSocketListener() {
    fun run() {
        val client = OkHttpClient.Builder().build()

        val request = Request.Builder()
            .url("http://${Repository.IP_ADDRESS}:${Repository.PORT}/get-image?/home/bashlykovvv/IntelliJIDEAProjects/http_server/src/main/resources/images/adminPhoto.jpg")
            .build()
        client.newWebSocket(request, this)

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher.executorService.shutdown()
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        webSocket.send("Hello...")
        webSocket.send("...World!")
        webSocket.send("deadbeef".decodeHex())
        webSocket.close(1000, "Goodbye, World!")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        println("MESSAGE: $text");
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        println("MESSAGE: " + bytes.hex());
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        webSocket.close(1000, null);
        println("CLOSE: $code $reason");
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        t.printStackTrace()
    }
}