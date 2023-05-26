package server

import Repository
import database.SQLiteContract
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

class Server(
    private val ip: String = Repository.IP_ADDRESS,
    private val port: Int = Repository.PORT,
    private val databaseUrl: String = SQLiteContract.MESSENGER_SQLITE_DATABASE_URL,
    private val handler: HttpHandler?
) {

    private lateinit var server: ServerSocket

    companion object {
        private const val BUFFER_SIZE = 256
    }

    fun bootstrap() {
        try {
            server = ServerSocket()
            server.bind(InetSocketAddress(ip, port))
            while (true) {
                val socket = server.accept()
                ClientHandler(socket).start()
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
            socket.soTimeout = 2500
        }

        override fun run() {
            super.run()
            val time = System.currentTimeMillis()
            println("New client connection")

            val outputStream = socket.getOutputStream()
            val inputStream = socket.getInputStream()

            var builder = inputStream.readData()

            if (builder.toString().contains("multipart/mixed")) {
                builder = inputStream.readMultipartBody(builder)
            }

            val request = HttpRequest(builder.toString())
            val response = HttpResponse(databaseUrl)

            response.handleRequests(request, outputStream)

            if (request.method != HttpMethod.GET) {
                outputStream.write(response.getBytes())
            }

            println(System.currentTimeMillis() - time)
            interrupt()
        }

        private fun InputStream.readData(): StringBuilder {
            val buffer = ByteArray(BUFFER_SIZE)
            val builder = StringBuilder()
            val clearInput = ByteArrayOutputStream()
            var keepReading = true

            while (keepReading) {
                var rbCount = read(buffer)

                while (rbCount == -1) {
                    yield()
                    rbCount = read(buffer)
                }

                clearInput.write(buffer, 0, rbCount)
                keepReading = rbCount == BUFFER_SIZE
            }
            val charBuffer = clearInput.toByteArray().decodeToString()
            builder.append(charBuffer)

            return builder
        }

        private fun InputStream.readMultipartBody(builder: StringBuilder): StringBuilder {
            val tmp = HttpRequest(builder.toString())
            val size = tmp.headers["Content-Length"]!!.toInt() - tmp.length
            val clearInput = ByteArrayOutputStream()
            val buffer = ByteArray(262144)
            var readSize = 0

            while (readSize < size) {
                val rbCount = read(buffer)
                if (rbCount > 0) {
                    clearInput.write(buffer, 0, rbCount)
                    readSize += rbCount
                }
            }
            builder.apply {
                append(clearInput.toByteArray().decodeToString().substringBeforeLast("}"))
                append("}")
            }

            return builder
        }

        private fun OutputStream.handleGetRequests(request: HttpRequest, response: HttpResponse) {
            try {
                write(WebSocketHandlerImpl().handle(request, response))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                interrupt()
            }
        }

        private fun HttpResponse.handleRequests(request: HttpRequest, outputStream: OutputStream) {
            if (handler != null) {
                try {
                    if (request.method == HttpMethod.GET) {
                        outputStream.handleGetRequests(request, this)
                    } else {
                        val body = handler.handle(request, this)

                        if (!body.isNullOrBlank()) {
                            if (this.headers["Content-Type"] == null) {
                                this.addHeader("Content-Type", "application/json; charset=utf-8")
                            }
                            this.setBody(body)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()

                    this.statusCode = 500
                    this.status = "Internal server error"
                    this.addHeader("Content-Type", "application/json; charset=utf-8")
                }
            } else {
                this.statusCode = 404
                this.status = "Not found"
                this.addHeader("Content-Type", "application/json; charset=utf-8")
            }
        }
    }
}