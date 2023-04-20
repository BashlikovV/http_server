import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.jupiter.api.Test
import server.HttpHandlerImpl
import server.HttpRequest
import server.HttpResponse
import server.Server
import server.entities.SignInRequestBody
import server.entities.SignUpRequestBody
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ServerTest {

    private val gson: Gson = GsonBuilder().setLenient().create()

    private val client = createOkHttpClient()

    private val contentType = "application/json; charset=utf-8".toMediaType()

    companion object {
        const val TEST_SQLITE_DATABASE_URL = "jdbc:sqlite:src/test/resources/test_database.db"

        const val TEST_EMAIL = "test_email@mail.com"
        const val TEST_PASSWORD = "test_password"
        const val TEST_USERNAME = "test_username"
        const val TEST_IMAGE_URI =
            "/home/bashlykovvv/IntelliJIDEAProjects/http_server/src/main/resources/images/adminPhoto.jpg"
    }

    private val thread = thread(start = true) {
        Server(
            ip = Repository.IP_ADDRESS,
            port = Repository.PORT,
            databaseUrl = TEST_SQLITE_DATABASE_URL
        ) { request: HttpRequest?, response: HttpResponse? ->
            HttpHandlerImpl().handle(request!!, response!!)
        }.bootstrap()
    }

    @Test
    fun signUpTest() {
        val randomInt = Random().nextInt().toString()

        val signUpRequestBody = SignUpRequestBody(
            TEST_USERNAME + randomInt,
            TEST_EMAIL + randomInt,
            TEST_PASSWORD + randomInt,
            TEST_IMAGE_URI + randomInt
        )
        val request = Request.Builder()
            .post(signUpRequestBody.toJsonRequestBody())
            .endpoint("/sign-up")
            .build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
        } catch (_: Exception) {  }

        assert(response!!.isSuccessful)
    }

    @Test
    fun signInTest() {
        val signInRequestBody = SignInRequestBody(
            email = TEST_EMAIL,
            password = TEST_PASSWORD
        )
        val request = Request.Builder()
            .post(signInRequestBody.toJsonRequestBody())
            .endpoint("/sign-in")
            .build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
        } catch (_: Exception) {  }

        assert(response!!.isSuccessful)
    }

    private fun Request.Builder.endpoint(endpoint: String): Request.Builder {
        url("http://${Repository.IP_ADDRESS}:${Repository.PORT}$endpoint")
        return this
    }

    private fun <T> T.toJsonRequestBody(): RequestBody {
        val json = gson.toJson(this)
        return json.toRequestBody(contentType)
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(1000, TimeUnit.MILLISECONDS)
            .callTimeout(10000, TimeUnit.MILLISECONDS)
            .writeTimeout(10000, TimeUnit.MILLISECONDS)
            .callTimeout(10000, TimeUnit.MILLISECONDS)
            .readTimeout(1000, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    init {
        thread.interrupt()
    }
}