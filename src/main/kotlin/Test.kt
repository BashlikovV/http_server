import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import sun.security.util.Password

data class SignInRequestBody(
    val email: String,
    val password: String
)

data class SignInResponseBody(
    val token: String
)

val contentType = "application/json; charset=utf-8".toMediaType()

fun main() {
    val client = OkHttpClient.Builder().build()

    val requestBodyString = Gson().toJson(SignInRequestBody(
        email = "test_mail@gmail.com",
        password = "test_password"
    )).toRequestBody(contentType)

    val request = Request.Builder()
        .post(requestBodyString)
        .url("http://${Repository.IP_ADDRESS}:${Repository.PORT}/sign-in")
        .build()

    val call = client.newCall(request)

    val response = call.execute()

    if (response.isSuccessful) {
        val responseBodyString = response.body!!.string()
        val signInResponseBody = Gson().fromJson(
            responseBodyString,
            SignInResponseBody::class.java
        )
        println(signInResponseBody.token)
    } else {
        throw IllegalArgumentException()
    }
}