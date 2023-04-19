import database.SQLiteContract
import database.SQLiteMessengerRepository
import database.entities.Room
import database.entities.User
import org.junit.jupiter.api.Test
import utils.SecurityUtilsImpl
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import kotlin.test.assertEquals

class SQLiteMessengerRepositoryTest {

    companion object {
        const val TEST_SQLITE_DATABASE_URL = "jdbc:sqlite:src/test/resources/test_database.db"

        const val TEST_EMAIL = "test_email@mail.com"
        const val TEST_PASSWORD = "test_password"
        const val TEST_USERNAME = "test_username"
        const val TEST_UPDATED_USERNAME = "updated_username"
        const val TEST_IMAGE_URI =
            "/home/bashlykovvv/IntelliJIDEAProjects/http_server/src/main/resources/images/adminPhoto.jpg"

        const val TEST_EMAIL_1 = "test_email_1@email.com"
        const val TEST_PASSWORD_1 = "test_password_1"
        const val TEST_USERNAME_1 = "test_username_1"
    }

    private val testMessengerRepository = SQLiteMessengerRepository(TEST_SQLITE_DATABASE_URL)

    private lateinit var connection: Connection

    private val securityUtils = SecurityUtilsImpl()

    @Test
    fun signUpTest() {
        try {
            testMessengerRepository.signUp(
                email = TEST_EMAIL,
                password = TEST_PASSWORD,
                username = TEST_USERNAME,
                imageUri = TEST_IMAGE_URI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        assertEquals(
            true,
            checkDatabaseContainsUser(
                email = TEST_EMAIL,
                password = TEST_PASSWORD,
                username = TEST_USERNAME,
                imageUri = TEST_IMAGE_URI
            )
        )
    }

    @Test
    fun signInTest() {
        var user: User? = null
        try {
            user = testMessengerRepository.signIn(
                email = TEST_EMAIL,
                password = TEST_PASSWORD
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        assert(user != null)
    }

    @Test
    fun getUserByTokenTest() {
        val userToken = securityUtils.bytesToString(
            testMessengerRepository.signIn(
                email = TEST_EMAIL,
                password = TEST_PASSWORD
            ).token
        )

        var user: User? = null
        try {
            user = testMessengerRepository.getUserByToken(
                userToken
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        assert(user != null)
    }

    @Test
    fun updateUsernameByTokeTest() {
        val userToken = securityUtils.bytesToString(
            testMessengerRepository.signIn(
                email = TEST_EMAIL,
                password = TEST_PASSWORD
            ).token
        )

        testMessengerRepository.updateUsernameByToken(
            token = userToken,
            username = TEST_UPDATED_USERNAME
        )

        var user: User? = null
        try {
            user = testMessengerRepository.getUserByToken(userToken)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        assertEquals(TEST_UPDATED_USERNAME, user?.username)

        testMessengerRepository.updateUsernameByToken(
            token = userToken,
            username = TEST_USERNAME
        )

        try {
            user = testMessengerRepository.getUserByToken(userToken)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        assertEquals(TEST_USERNAME, user?.username)
    }

    @Test
    fun getAllUsersTest() {
        var users: List<User>? = null

        try {
            users = testMessengerRepository.getAllUsers()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        assert(users?.isNotEmpty() == true)
    }

    @Test
    fun addRoomTest() {
        try {
            testMessengerRepository.signUp(
                email = TEST_EMAIL_1,
                password = TEST_PASSWORD_1,
                username = TEST_USERNAME_1,
                imageUri = TEST_IMAGE_URI
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val firstUserToken = securityUtils.bytesToString(
            testMessengerRepository.signIn(
                email = TEST_EMAIL,
                password = TEST_PASSWORD
            ).token
        )
        val secondUserToken = securityUtils.bytesToString(
            testMessengerRepository.signIn(
                email = TEST_EMAIL_1,
                password = TEST_PASSWORD_1
            ).token
        )
        val firstUser = testMessengerRepository.getUserByToken(firstUserToken)
        val secondUser = testMessengerRepository.getUserByToken(secondUserToken)

        testMessengerRepository.addRoomByTwoUsers(firstUser, secondUser)

        assertEquals(true, checkDatabaseContainsRoom(firstUser, secondUser))
    }

    @Test
    fun getRoomByTwoUserTest() {
        val firstUserToken = securityUtils.bytesToString(
            testMessengerRepository.signIn(
                email = TEST_EMAIL,
                password = TEST_PASSWORD
            ).token
        )
        val secondUserToken = securityUtils.bytesToString(
            testMessengerRepository.signIn(
                email = TEST_EMAIL_1,
                password = TEST_PASSWORD_1
            ).token
        )
        val firstUser = testMessengerRepository.getUserByToken(firstUserToken)
        val secondUser = testMessengerRepository.getUserByToken(secondUserToken)

        var room: Room? = null
        try {
            room = testMessengerRepository.getRoomByTwoUsers(firstUser, secondUser)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        assert(room != null)
    }

    private fun checkDatabaseContainsUser(
        email: String, password: String, username: String, imageUri: String
    ): Boolean {
        try {
            connection = DriverManager.getConnection(TEST_SQLITE_DATABASE_URL)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            val salt = getUserSaltByEmail(email, statement)
            val token = securityUtils.passwordToHash(
                password = password.toCharArray(),
                salt = salt
            )

            statement.use {
                val resultSet = it.executeQuery(
                    "select * " +
                        "from ${SQLiteContract.UsersTable.TABLE_NAME} " +
                        "where ${SQLiteContract.UsersTable.COLUMN_EMAIL}=('$email') " +
                            "and ${SQLiteContract.UsersTable.COLUMN_USERNAME}=('$username') " +
                            "and ${SQLiteContract.UsersTable.COLUMN_IMAGE}=('$imageUri') " +
                            "and ${SQLiteContract.UsersTable.COLUMN_TOKEN}=('${securityUtils.bytesToString(token)}');"
                )
                return resultSet.next()
            }
        } catch (e: SQLException) {
            throw e
        } finally {
            try {
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    private fun getUserSaltByEmail(email: String, statement: Statement): ByteArray {
        var result = ""

        try {
            val resultSet = statement.executeQuery(
                "select ${SQLiteContract.UsersTable.COLUMN_SALT} " +
                        "from ${SQLiteContract.UsersTable.TABLE_NAME} " +
                        "where ${SQLiteContract.UsersTable.COLUMN_EMAIL}='$email';"
            )

            result = resultSet.getString(SQLiteContract.UsersTable.COLUMN_SALT)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return securityUtils.stringToBytes(result)
    }

    private fun checkDatabaseContainsRoom(user1: User, user2: User): Boolean {
        try {
            connection = DriverManager.getConnection(TEST_SQLITE_DATABASE_URL)
            val statement = connection.createStatement()
            statement.queryTimeout = 30


            statement.use {
                val resultSet = it.executeQuery(
                    "select * " +
                        "from ${SQLiteContract.RoomsTable.TABLE_NAME} " +
                        "where ${SQLiteContract.RoomsTable.COLUMN_USER_1}='${securityUtils.bytesToString(user1.token)}' " +
                        "and ${SQLiteContract.RoomsTable.COLUMN_USER_2}='${securityUtils.bytesToString(user2.token)}' " +
                        "or ${SQLiteContract.RoomsTable.COLUMN_USER_2}='${securityUtils.bytesToString(user1.token)}' " +
                        "and ${SQLiteContract.RoomsTable.COLUMN_USER_1}='${securityUtils.bytesToString(user2.token)}';"
                )
                return resultSet.next()
            }
        } catch (e: SQLException) {
            throw e
        } finally {
            try {
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
}