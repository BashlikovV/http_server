import database.SQLiteContract
import database.SQLiteMessengerRepository
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
        const val TEST_IMAGE_URI =
            "/home/bashlykovvv/IntelliJIDEAProjects/http_server/src/main/resources/images/adminPhoto.jpg"
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
}