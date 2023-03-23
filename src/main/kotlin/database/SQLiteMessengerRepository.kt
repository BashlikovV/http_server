package database

import AuthException
import database.entities.Room
import database.entities.User
import utils.SecurityUtilsImpl
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*


class SQLiteMessengerRepository : MessengerRepository {

    private lateinit var connection: Connection

    private val securityUtils = SecurityUtilsImpl()

    override fun signUp(email: String, password: String, username: String) {
        try {
            connection = DriverManager.getConnection(SQLiteContract.MESSENGER_SQLITE_DATABASE_URL)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            val salt = securityUtils.generateSalt()
            val token = securityUtils.passwordToHash(
                password = password.toCharArray(),
                salt = salt
            )
            val time = Calendar.getInstance().time.toString()

            statement.execute(
                "insert into ${SQLiteContract.UsersTable.TABLE_NAME} (" +
                    "${SQLiteContract.UsersTable.COLUMN_USERNAME}," +
                    "${SQLiteContract.UsersTable.COLUMN_EMAIL}," +
                    "${SQLiteContract.UsersTable.COLUMN_TOKEN}," +
                    "${SQLiteContract.UsersTable.COLUMN_SALT}," +
                    "${SQLiteContract.UsersTable.COLUMN_CREATED_AT}) values(" +
                        "'$username', '$email', '${securityUtils.bytesToString(token)}', " +
                        "'${securityUtils.bytesToString(salt)}', '$time'"+
                    ")"
            )
        } catch (e: SQLException) {
            e.printStackTrace()
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
                    "where ${SQLiteContract.UsersTable.COLUMN_EMAIL}='$email'"
            )

            result = resultSet.getString(SQLiteContract.UsersTable.COLUMN_SALT)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return securityUtils.stringToBytes(result)
    }

    private fun getUserTokenByEmail(email: String): ByteArray {
        var result = ""

        try {
            connection = DriverManager.getConnection(SQLiteContract.MESSENGER_SQLITE_DATABASE_URL)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            val resultSet = statement.executeQuery(
                "select ${SQLiteContract.UsersTable.COLUMN_TOKEN} " +
                    "from ${SQLiteContract.UsersTable.TABLE_NAME} " +
                    "where ${SQLiteContract.UsersTable.COLUMN_EMAIL}='$email'"
            )
            result = resultSet.getString(SQLiteContract.UsersTable.COLUMN_TOKEN)
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return securityUtils.stringToBytes(result)
    }

    override fun signIn(email: String, password: String): User {
        var result = User()

        try {
            connection = DriverManager.getConnection(SQLiteContract.MESSENGER_SQLITE_DATABASE_URL)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            val salt = getUserSaltByEmail(email, statement)
            val token = securityUtils.passwordToHash(
                password = password.toCharArray(),
                salt = salt
            )
            val tableUserToken = getUserTokenByEmail(email)

            if (token.contentEquals(tableUserToken)) {
                val resultSet = statement.executeQuery(
                    "select * " +
                        "from ${SQLiteContract.UsersTable.TABLE_NAME} " +
                        "where ${SQLiteContract.UsersTable.COLUMN_EMAIL}=('$email')"
                )

                result = User(
                    id = resultSet.getInt(SQLiteContract.UsersTable.COLUMN_ID),
                    username = resultSet.getString(SQLiteContract.UsersTable.COLUMN_USERNAME),
                    email = resultSet.getString(SQLiteContract.UsersTable.COLUMN_EMAIL),
                    token = resultSet.getString(SQLiteContract.UsersTable.COLUMN_TOKEN).toByteArray(),
                    salt = resultSet.getString(SQLiteContract.UsersTable.COLUMN_SALT).toByteArray(),
                    createdAt = resultSet.getString(SQLiteContract.UsersTable.COLUMN_CREATED_AT)
                )
            } else {
                throw AuthException()
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            try {
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        return result
    }

    override fun getUserByToken(token: String): User {
        var result = User()

        try {
            connection = DriverManager.getConnection(SQLiteContract.MESSENGER_SQLITE_DATABASE_URL)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            val resultSet = statement.executeQuery(
                "select * " +
                        "from ${SQLiteContract.UsersTable.TABLE_NAME} " +
                        "where ${SQLiteContract.UsersTable.COLUMN_TOKEN}=('$token')"
            )

            result = User(
                id = resultSet.getInt(SQLiteContract.UsersTable.COLUMN_ID),
                username = resultSet.getString(SQLiteContract.UsersTable.COLUMN_USERNAME),
                email = resultSet.getString(SQLiteContract.UsersTable.COLUMN_EMAIL),
                token = resultSet.getString(SQLiteContract.UsersTable.COLUMN_TOKEN).toByteArray(),
                salt = resultSet.getString(SQLiteContract.UsersTable.COLUMN_SALT).toByteArray(),
                createdAt = resultSet.getString(SQLiteContract.UsersTable.COLUMN_CREATED_AT)
            )
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            try {
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        return result
    }

    override fun updateUsernameByToken(token: String, username: String) {
        try {
            connection = DriverManager.getConnection(SQLiteContract.MESSENGER_SQLITE_DATABASE_URL)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.execute(
                "update ${SQLiteContract.UsersTable.TABLE_NAME} " +
                        "set ${SQLiteContract.UsersTable.COLUMN_USERNAME}='$username' " +
                        "where ${SQLiteContract.UsersTable.COLUMN_TOKEN}=('$token')"
            )
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            try {
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    override fun getAllUsers(): List<User>? {
        TODO("Not yet implemented")
    }

    override fun getRoomByTwoUsers(user1: String, user2: String): Room? {
        TODO("Not yet implemented")
    }

    override fun deleteRoomByTwoUsers(user1: String, user2: String) {
        TODO("Not yet implemented")
    }
}