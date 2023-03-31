package server

import Repository
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeoutException

class Server(
    private val handler: HttpHandler?
) {

    private lateinit var server : AsynchronousServerSocketChannel

    companion object {
        private const val BUFFER_SIZE = 256
    }

    fun bootstrap() {
        try {
            server = AsynchronousServerSocketChannel.open()
            server.bind(InetSocketAddress(Repository.IP_ADDRESS, Repository.PORT))
            while (true) {
                val future = server.accept()
                handleClient(future)
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

    private fun handleClient(future: Future<AsynchronousSocketChannel>){
        println("New client connection")

        val clientChannel = future.get()

        while (clientChannel != null && clientChannel.isOpen) {
            val buffer = ByteBuffer.allocate(BUFFER_SIZE)
            val builder = StringBuilder()
            var keepReading = true

            while (keepReading) {
                val readResult = clientChannel.read(buffer).get()

                keepReading = readResult == BUFFER_SIZE
                buffer.flip()
                val charBuffer = StandardCharsets.UTF_8.decode(buffer)
                builder.append(charBuffer)

                buffer.clear()
            }

            val request = HttpRequest(builder.toString())
            val response = HttpResponse()

            if (handler != null) {
                try {
                    val body = this.handler.handle(request, response)

                    if (!body.isNullOrBlank()) {
                        if (response.headers["Content-Type"] == null) {
                            response.addHeader("Content-Type", "application/json; charset=utf-8")
                        }
                        response.setBody(body)
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

            val resp = ByteBuffer.wrap(response.getBytes())
            clientChannel.write(resp)

            clientChannel.close()
        }
    }
}