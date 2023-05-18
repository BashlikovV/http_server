package server

import Repository
import database.SQLiteContract
import okio.ByteString.Companion.toByteString
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

class Server(
    private val ip: String = Repository.IP_ADDRESS,
    private val port: Int = Repository.PORT,
    private val databaseUrl: String = SQLiteContract.MESSENGER_SQLITE_DATABASE_URL,
    private val handler: HttpHandler?
) {

    private lateinit var server : ServerSocket

    private var count = AtomicInteger(0)

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
            count.incrementAndGet()
        }

        override fun run() {
            super.run()
            println("New client connection")

            val stream = socket.getOutputStream()
            val inputStream = socket.getInputStream()

            while (!socket.isClosed) {
                val buffer = ByteArray(BUFFER_SIZE)
                val builder = StringBuilder()
                val clearInput = ByteArrayOutputStream()
                var keepReading = true

//                while (keepReading) {
//                    val readResult = inputStream.read(buffer)
//
//                    keepReading = readResult == BUFFER_SIZE
//                    val charBuffer = StandardCharsets.UTF_8.decode(buffer.toByteString().asByteBuffer())
//                    builder.append(charBuffer)
//                    buffer.fill(0)
//                }

                while (keepReading) {
                    val readResult = inputStream.read(buffer)
                    clearInput.write(buffer, 0, readResult)

                    keepReading = readResult == BUFFER_SIZE
                }
                val charBuffer = StandardCharsets.UTF_8.decode(clearInput.toByteArray().toByteString().asByteBuffer())
                builder.append(charBuffer)
                clearInput.reset()

                if (builder.toString().contains("multipart/mixed")) {
                    val time = System.currentTimeMillis()
                    val tmp = HttpRequest(builder.toString(), true)
                    val size = tmp.headers["Content-Length"]!!.toInt() - tmp.length
                    val bytes = ByteArray(262144)
                    var readSize = 0
                    while (readSize < size) {
                        val count = inputStream.read(bytes)
                        if (count > 0) {
                            clearInput.write(bytes, 0, count)
                            readSize += count
                        }
                    }
                    builder.apply {
                        append(clearInput.toByteArray().decodeToString().substringBeforeLast("}"))
                        append("}")
                    }
                    println(System.currentTimeMillis() - time)
                }

                val str = builder.toString()
                val request = HttpRequest(str, false)
                val response = HttpResponse(databaseUrl)

                if (handler != null) {
                    try {
                        if (request.method == HttpMethod.GET) {
                            try {
                                WebSocketHandlerImpl().handle(request, response).let {
                                    stream.write(it)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                stream.run {
//                                    flush()
                                    close()
                                }
                                this.interrupt()
                            }
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

                if (request.method != HttpMethod.GET) {
                    stream.write(response.getBytes())
                }
//                stream.flush()
                stream.close()
                socket.close()
            }
            count.decrementAndGet()
            println(count.get())
            this.interrupt()
        }
    }
}