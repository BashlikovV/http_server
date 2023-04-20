import com.google.gson.Gson
import com.google.gson.GsonBuilder
import database.SQLiteMessengerRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.junit.jupiter.api.Test
import server.*
import server.entities.*
import utils.SecurityUtilsImpl
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ServerTest {

    private val testMessengerRepository = SQLiteMessengerRepository(TEST_SQLITE_DATABASE_URL)

    private val gson: Gson = GsonBuilder().setLenient().create()

    private val securityUtils = SecurityUtilsImpl()

    private val client = createOkHttpClient()

    private val contentType = "application/json; charset=utf-8".toMediaType()

    companion object {
        const val TEST_SQLITE_DATABASE_URL = "jdbc:sqlite:src/test/resources/test_database.db"

        const val TEST_EMAIL = "test_email@mail.com"
        const val TEST_PASSWORD = "test_password"
        const val TEST_USERNAME = "test_username"
        const val TEST_IMAGE_URI =
            "/home/bashlykovvv/IntelliJIDEAProjects/http_server/src/main/resources/images/adminPhoto.jpg"

        const val TEST_EMAIL_1 = "test_email_1@email.com"
        const val TEST_PASSWORD_1 = "test_password_1"
        const val TEST_USERNAME_1 = "test_username_1"
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
            .endpoint("/${HttpContract.UrlMethods.SIGN_UP}")
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
            .endpoint("/${HttpContract.UrlMethods.SIGN_IN}")
            .build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
        } catch (_: Exception) {  }

        assert(response!!.isSuccessful)
    }

    @Test
    fun getRoomMessagesTest() {
        val user = testMessengerRepository.signIn(
            email = TEST_EMAIL,
            password = TEST_PASSWORD
        )
        val rooms = testMessengerRepository.getRoomsByUser(user)

        val getRoomMessagesRequestBody = RoomMessagesRequestBody(
            room = securityUtils.bytesToString(rooms.random().token),
            pagination = IntRange(0, 1)
        )
        val request = Request.Builder()
            .post(getRoomMessagesRequestBody.toJsonRequestBody())
            .endpoint("/${HttpContract.UrlMethods.ROOM_MESSAGES}")
            .build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
        } catch (_: Exception) {  }

        assert(response!!.isSuccessful)
    }

    @Test
    fun getRoomsByUserTest() {
        val userToken = securityUtils.bytesToString(
            testMessengerRepository.signIn(
                email = TEST_EMAIL,
                password = TEST_PASSWORD
            ).token
        )

        val getRoomsRequestBody = GetRoomsRequestBody(
            user = userToken
        )
        val request = Request.Builder()
            .post(getRoomsRequestBody.toJsonRequestBody())
            .endpoint("/${HttpContract.UrlMethods.GET_ROOMS}")
            .build()
        var response: Response? = null
        try {
            response = client.newCall(request).execute()
        } catch (_: Exception) {  }

        assert(response!!.isSuccessful)
    }

    @Test
    fun addRoomByTwoUsersTest() {
        val randomInt = Random().nextInt().toString()

        testMessengerRepository.signUp(
            email = TEST_EMAIL + randomInt,
            password = TEST_PASSWORD,
            username = TEST_EMAIL + randomInt,
            imageUri = TEST_IMAGE_URI
        )
        testMessengerRepository.signUp(
            email = TEST_EMAIL_1 + randomInt,
            password = TEST_PASSWORD_1,
            username = TEST_EMAIL_1 + randomInt,
            imageUri = TEST_IMAGE_URI
        )

        val firstUser = testMessengerRepository.signIn(
            email = TEST_EMAIL + randomInt,
            password = TEST_PASSWORD
        )
        val secondUser = testMessengerRepository.signIn(
            email = TEST_EMAIL_1 + randomInt,
            password = TEST_PASSWORD_1
        )

        val addRoomRequestBody = AddRoomRequestBody(
            user1 = securityUtils.bytesToString(firstUser.token)   ,
            user2 = securityUtils.bytesToString(secondUser.token)
        )
        val request = Request.Builder()
            .post(addRoomRequestBody.toJsonRequestBody())
            .endpoint("/${HttpContract.UrlMethods.ADD_ROOM}")
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