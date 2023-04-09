package server

import Repository
import okio.ByteString.Companion.toByteString
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

class Server(
    private val handler: HttpHandler?
) {

    private lateinit var server : ServerSocket

    companion object {
        private const val BUFFER_SIZE = 256
    }

    fun bootstrap() {
        try {
            server = ServerSocket()
            server.bind(InetSocketAddress(Repository.IP_ADDRESS, Repository.PORT))
            while (true) {
                val socket = server.accept()
                ClientHandler(socket)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            e.printStackTrace()
        }
    }

    inner class ClientHandler(
        private val socket: Socket
    ) : Thread() {

        init {
            start()
        }

        override fun run() {
            super.run()
            println("New client connection")

            val stream = socket.getOutputStream()

            while (!socket.isClosed) {
                val buffer = ByteArray(BUFFER_SIZE)
                val builder = StringBuilder()
                var keepReading = true

                while (keepReading) {
                    val readResult = socket.getInputStream().read(buffer)

                    keepReading = readResult == BUFFER_SIZE
                    val charBuffer = StandardCharsets.UTF_8.decode(buffer.toByteString().asByteBuffer())
                    builder.append(charBuffer)

                    buffer.fill(0)
                }

                val request = HttpRequest(builder.toString())
                val response = HttpResponse()

                if (handler != null) {
                    try {
                        if (request.method == HttpMethod.GET) {
                            val outputStream = socket.getOutputStream()
                            WebSocketHandlerImpl().handle(request, response)?.let {
                                outputStream.write(it)
                            }
                            outputStream.flush()
                            outputStream.close()
                            stream.flush()
                            stream.close()
                            this.interrupt()
                        } else {
                            val body = handler.handle(request, response)

                            if (!body.isNullOrBlank()) {
                                if (response.headers["Content-Type"] == null) {
                                    response.addHeader("Content-Type", "application/json; charset=utf-8")
                                }
                                response.setBody(body)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                        response.statusCode = 500
                        response.status = "Internal server error"
                        response.addHeader("Content-Type", "application/json; charset=utf-8")
                    }
                } else {
                    response.statusCode = 404
                    response.status = "Not found"
                    response.addHeader("Content-Type", "application/json; charset=utf-8")
                }

                stream.write(response.getBytes())
                stream.flush()
                stream.close()
                socket.close()
            }
            this.interrupt()
        }
    }
}