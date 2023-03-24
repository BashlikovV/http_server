import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import server.entities.RoomMessagesRequestBody
import server.entities.RoomMessagesResponseBody


val contentType = "application/json; charset=utf-8".toMediaType()

fun main() {
    val client = OkHttpClient.Builder().build()

    val requestBodyString = Gson().toJson(
        RoomMessagesRequestBody(
            user1 = "MTm4NuLnbvRXmATDfFuvN9Qhc9M=",
            user2 = "I88/J9VXbL/MWmBvag0YJZPFa5k="
        )
    ).toRequestBody(contentType)

    val request = Request.Builder()
        .post(requestBodyString)
        .url("http://${Repository.IP_ADDRESS}:${Repository.PORT}/room-messages")
        .build()

    val call = client.newCall(request)

    val response = call.execute()

    if (response.isSuccessful) {
        val responseBodyString = response.body!!.string()
        val signInResponseBody = Gson().fromJson(
            responseBodyString,
            RoomMessagesResponseBody::class.java
        )
        signInResponseBody.messages.forEach {
            println("${it.value} -> ${it.time}")
        }
    } else {
        throw IllegalArgumentException()
    }
}