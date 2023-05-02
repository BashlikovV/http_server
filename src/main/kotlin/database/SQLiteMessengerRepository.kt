package database

import AuthException
import database.entities.Message
import database.entities.Room
import database.entities.User
import utils.SecurityUtilsImpl
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*

class SQLiteMessengerRepository(
    private val databaseUrl: String = SQLiteContract.MESSENGER_SQLITE_DATABASE_URL
) : MessengerRepository {

    private lateinit var connection: Connection

    val securityUtils = SecurityUtilsImpl()

    override fun signUp(email: String, password: String, username: String, imageUri: String) {
        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            val salt = securityUtils.generateSalt()
            val token = securityUtils.passwordToHash(
                password = password.toCharArray(),
                salt = salt
            )
            val time = Calendar.getInstance().time.toString()

            statement.use {
                it.execute(
                    "insert into ${SQLiteContract.UsersTable.TABLE_NAME} (" +
                        "${SQLiteContract.UsersTable.COLUMN_USERNAME}," +
                        "${SQLiteContract.UsersTable.COLUMN_EMAIL}," +
                        "${SQLiteContract.UsersTable.COLUMN_TOKEN}," +
                        "${SQLiteContract.UsersTable.COLUMN_SALT}," +
                        "${SQLiteContract.UsersTable.COLUMN_CREATED_AT}," +
                        SQLiteContract.UsersTable.COLUMN_IMAGE +
                    ") values (" +
                        "'$username', '$email', '${securityUtils.bytesToString(token)}', " +
                        "'${securityUtils.bytesToString(salt)}', '$time', '$imageUri'" +
                    ");"
                )
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

    private fun getUserTokenByEmail(email: String): ByteArray {
        var result = ""

        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                val resultSet = it.executeQuery(
                    "select ${SQLiteContract.UsersTable.COLUMN_TOKEN} " +
                        "from ${SQLiteContract.UsersTable.TABLE_NAME} " +
                        "where ${SQLiteContract.UsersTable.COLUMN_EMAIL}='$email';"
                )
                result = resultSet.getString(SQLiteContract.UsersTable.COLUMN_TOKEN)
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return securityUtils.stringToBytes(result)
    }

    private fun checkEmailAndPassword(email: String, password: String, statement: Statement): Boolean {
        val salt = getUserSaltByEmail(email, statement)
        val token = securityUtils.passwordToHash(
            password = password.toCharArray(),
            salt = salt
        )
        val tableUserToken = getUserTokenByEmail(email)

        return token.contentEquals(tableUserToken)
    }

    override fun signIn(email: String, password: String): User {
        var result = User()

        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            if (checkEmailAndPassword(email, password, statement)) {
                statement.use {
                    val resultSet = it.executeQuery(
                        "select * " +
                            "from ${SQLiteContract.UsersTable.TABLE_NAME} " +
                            "where ${SQLiteContract.UsersTable.COLUMN_EMAIL}=('$email');"
                    )

                    result = User(
                        id = resultSet.getInt(SQLiteContract.UsersTable.COLUMN_ID),
                        username = resultSet.getString(SQLiteContract.UsersTable.COLUMN_USERNAME),
                        email = resultSet.getString(SQLiteContract.UsersTable.COLUMN_EMAIL),
                        token = securityUtils.stringToBytes(resultSet.getString(SQLiteContract.UsersTable.COLUMN_TOKEN)),
                        salt = securityUtils.stringToBytes(resultSet.getString(SQLiteContract.UsersTable.COLUMN_SALT)),
                        createdAt = resultSet.getString(SQLiteContract.UsersTable.COLUMN_CREATED_AT),
                        image = resultSet.getString(SQLiteContract.UsersTable.COLUMN_IMAGE).encodeToByteArray()
                    )
                }
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
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                val resultSet = it.executeQuery(
                    "select * " +
                        "from ${SQLiteContract.UsersTable.TABLE_NAME} " +
                        "where ${SQLiteContract.UsersTable.COLUMN_TOKEN}='$token';"
                )

                result = User(
                    id = resultSet.getInt(SQLiteContract.UsersTable.COLUMN_ID),
                    username = resultSet.getString(SQLiteContract.UsersTable.COLUMN_USERNAME) ?: "",
                    email = resultSet.getString(SQLiteContract.UsersTable.COLUMN_EMAIL) ?: "",
                    token = securityUtils.stringToBytes(token),
                    salt = securityUtils.stringToBytes(resultSet.getString(SQLiteContract.UsersTable.COLUMN_SALT)),
                    createdAt = resultSet.getString(SQLiteContract.UsersTable.COLUMN_CREATED_AT),
                    image = resultSet.getString(SQLiteContract.UsersTable.COLUMN_IMAGE).encodeToByteArray()
                )
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

    override fun updateUsernameByToken(token: String, username: String) {
        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                it.execute(
                    "update ${SQLiteContract.UsersTable.TABLE_NAME} " +
                        "set ${SQLiteContract.UsersTable.COLUMN_USERNAME}='$username' " +
                        "where ${SQLiteContract.UsersTable.COLUMN_TOKEN}=('$token');"
                )
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
    }

    override fun getAllUsers(): List<User> {
        val result = mutableListOf<User>()

        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                val resultSet = it.executeQuery(
                    "select * from ${SQLiteContract.UsersTable.TABLE_NAME};"
                )

                while (resultSet.next()) {
                    result.add(
                        User(
                            id = resultSet.getInt(SQLiteContract.UsersTable.COLUMN_ID),
                            username = resultSet.getString(SQLiteContract.UsersTable.COLUMN_USERNAME),
                            email = resultSet.getString(SQLiteContract.UsersTable.COLUMN_EMAIL),
                            token = securityUtils.stringToBytes(resultSet.getString(SQLiteContract.UsersTable.COLUMN_TOKEN)),
                            salt = securityUtils.stringToBytes(resultSet.getString(SQLiteContract.UsersTable.COLUMN_SALT)),
                            createdAt = resultSet.getString(SQLiteContract.UsersTable.COLUMN_CREATED_AT),
                            image = resultSet.getString(SQLiteContract.UsersTable.COLUMN_IMAGE).encodeToByteArray()
                        )
                    )
                }
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

    override fun getRoomByTwoUsers(user1: User, user2: User): Room {
        var result = Room()

        try {
            connection = DriverManager.getConnection(databaseUrl)
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

                result = Room(
                    user1 = getUserByToken(resultSet.getString(SQLiteContract.RoomsTable.COLUMN_USER_1)),
                    user2 = getUserByToken(resultSet.getString(SQLiteContract.RoomsTable.COLUMN_USER_2)),
                    token = securityUtils.stringToBytes(resultSet.getString(SQLiteContract.RoomsTable.COLUMN_TOKEN))
                )
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

    override fun deleteRoomByTwoUsers(user1: User, user2: User) {
        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            val room = getRoomByTwoUsers(user1, user2)

            statement.use {
                it.execute(
                    "delete from ${SQLiteContract.RoomsTable.TABLE_NAME} " +
                        "where ${SQLiteContract.RoomsTable.COLUMN_USER_1}='${securityUtils.bytesToString(user1.token)}' " +
                        "and ${SQLiteContract.RoomsTable.COLUMN_USER_2}='${securityUtils.bytesToString(user2.token)}' " +
                        "or ${SQLiteContract.RoomsTable.COLUMN_USER_2}='${securityUtils.bytesToString(user1.token)}' " +
                        "and ${SQLiteContract.RoomsTable.COLUMN_USER_1}='${securityUtils.bytesToString(user2.token)}';"
                )
            }
            statement.use {
                it.execute(
                    "delete from ${SQLiteContract.MessagesTable.TABLE_NAME} " +
                        "where ${SQLiteContract.MessagesTable.COLUMN_ROOM}='${securityUtils.bytesToString(room.token)}';"
                )
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
    }

    override fun addRoomByTwoUsers(user1: User, user2: User): ByteArray {
        var roomToken = byteArrayOf()

        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            roomToken = user1.token + user2.token

            statement.use {
                it.execute(
                    "insert into ${SQLiteContract.RoomsTable.TABLE_NAME} (" +
                            "${SQLiteContract.RoomsTable.COLUMN_USER_1}, " +
                            "${SQLiteContract.RoomsTable.COLUMN_USER_2}, " +
                            SQLiteContract.RoomsTable.COLUMN_TOKEN +
                        ") values (" +
                            "'${securityUtils.bytesToString(user1.token)}', " +
                            "'${securityUtils.bytesToString(user2.token)}', " +
                            "'${securityUtils.bytesToString(roomToken)}'" +
                        ");"
                )
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

        return roomToken
    }

    override fun addMessage(message: Message) {
        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                val from = "\"${SQLiteContract.MessagesTable.COLUMN_FROM}\""
                it.execute(
                    "insert into ${SQLiteContract.MessagesTable.TABLE_NAME} (" +
                            "${SQLiteContract.MessagesTable.COLUMN_ROOM}, " +
                            "${SQLiteContract.MessagesTable.COLUMN_IMAGE}, " +
                            "${SQLiteContract.MessagesTable.COLUMN_VALUE}, " +
                            "${SQLiteContract.MessagesTable.COLUMN_FILE}, " +
                            "${SQLiteContract.MessagesTable.COLUMN_OWNER}, " +
                            "${SQLiteContract.MessagesTable.COLUMN_TIME}, " +
                            from +
                        ") values (" +
                            "'${securityUtils.bytesToString(message.room.token)}', " +
                            "'${message.image}', " +
                            "'${message.value.decodeToString()}', " +
                            "'${message.file.decodeToString()}', " +
                            "'${securityUtils.bytesToString(message.owner.token)}', " +
                            "'${message.time}', " +
                            "'${message.from}'" +
                        ");"
                )
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
    }

    override fun deleteMessage(message: Message) {
        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                it.execute(
                    "delete from ${SQLiteContract.MessagesTable.TABLE_NAME} " +
                        "where ${SQLiteContract.MessagesTable.COLUMN_ROOM}='${securityUtils.bytesToString(message.room.token)}' " +
//                        "and ${SQLiteContract.MessagesTable.COLUMN_OWNER}='${securityUtils.bytesToString(message.owner.token)}' " +
                        "and ${SQLiteContract.MessagesTable.COLUMN_FILE}='${message.file.decodeToString()}' " +
                        "and ${SQLiteContract.MessagesTable.COLUMN_TIME}='${message.time}' " +
                        "and ${SQLiteContract.MessagesTable.COLUMN_VALUE}='${message.value.decodeToString()}' " +
                        "and ${SQLiteContract.MessagesTable.COLUMN_IMAGE}='${message.image}';"
                )
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
    }

    override fun getMessagesByRoom(room: Room, pagination: IntRange): List<Message> {
        val result = mutableListOf<Message>()

        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                val resultSet = it.executeQuery(
                    "select * from ${SQLiteContract.MessagesTable.TABLE_NAME} " +
                        "where ${SQLiteContract.MessagesTable.COLUMN_ROOM}='${securityUtils.bytesToString(room.token)}';"
                )

                while (resultSet.next()) {
                    result.add(
                        Message(
                            room = room,
                            image = resultSet.getString(SQLiteContract.MessagesTable.COLUMN_IMAGE),
                            value = resultSet.getString(SQLiteContract.MessagesTable.COLUMN_VALUE).encodeToByteArray(),
                            file = resultSet.getString(SQLiteContract.MessagesTable.COLUMN_FILE).encodeToByteArray(),
                            owner = room.user1,
                            time = resultSet.getString(SQLiteContract.MessagesTable.COLUMN_TIME),
                            from = resultSet.getString(SQLiteContract.MessagesTable.COLUMN_FROM)
                        )
                    )
                }
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

        return result.calculateSubList(pagination)
    }

    private fun List<Message>.calculateSubList(pagination: IntRange): List<Message> {
        var tmp = this
        tmp = if (tmp.size > pagination.last) {
            tmp.subList(tmp.size - pagination.last, tmp.size - pagination.first)
        } else {
            val low = if (pagination.last - 30 < 0) {
                0
            } else {
                pagination.last - 30
            }
            val high = if (pagination.last > tmp.size) {
                tmp.size
            } else {
                pagination.last
            }
            tmp.subList(tmp.size - high, tmp.size - low)
        }
        return tmp
    }

    override fun getRoomsByUser(user: User): List<Room> {
        val result = mutableListOf<Room>()

        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                val resultSet = it.executeQuery(
                    "select * from ${SQLiteContract.RoomsTable.TABLE_NAME} " +
                            "where ${SQLiteContract.RoomsTable.COLUMN_USER_1}='${securityUtils.bytesToString(user.token)}' " +
                            "or ${SQLiteContract.RoomsTable.COLUMN_USER_2}='${securityUtils.bytesToString(user.token)}';"
                )

                while (resultSet.next()) {
                    result.add(
                        Room(
                            user1 = getUserByToken(
                                resultSet.getString(SQLiteContract.RoomsTable.COLUMN_USER_1)
                            ),
                            user2 = getUserByToken(
                                resultSet.getString(SQLiteContract.RoomsTable.COLUMN_USER_2)
                            ),
                            token = securityUtils.stringToBytes(
                                resultSet.getString(SQLiteContract.RoomsTable.COLUMN_TOKEN)
                            )
                        )
                    )
                }
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

    override fun getRoomByToken(token: String): Room {
        var result = Room()

        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                val resultSet = it.executeQuery(
                    "select * from ${SQLiteContract.RoomsTable.TABLE_NAME} " +
                            "where ${SQLiteContract.RoomsTable.COLUMN_TOKEN}='$token';"
                )

                result = Room(
                    user1 = getUserByToken(
                        resultSet.getString(SQLiteContract.RoomsTable.COLUMN_USER_1)
                    ),
                    user2 = getUserByToken(
                        resultSet.getString(SQLiteContract.RoomsTable.COLUMN_USER_2)
                    ),
                    token = securityUtils.stringToBytes(
                        resultSet.getString(SQLiteContract.RoomsTable.COLUMN_TOKEN)
                    )
                )
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

    override fun addImage(imageUri: String) {
        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                it.execute(
                    "insert into ${SQLiteContract.ImagesTable.TABLE_NAME} (" +
                            SQLiteContract.ImagesTable.COLUMN_IMAGE +
                        ") values (" +
                            "'$imageUri'" +
                        ");"
                )
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
    }

    override fun getMaxId(): Int {
        var result = 0

        try {
            connection = DriverManager.getConnection(databaseUrl)
            val statement = connection.createStatement()
            statement.queryTimeout = 30

            statement.use {
                val resultSet = it.executeQuery(
                    "SELECT ${SQLiteContract.ImagesTable.COLUMN_ID} " +
                        "FROM ${SQLiteContract.ImagesTable.TABLE_NAME} " +
                        "WHERE ${SQLiteContract.ImagesTable.COLUMN_ID}=(" +
                            "SELECT max(${SQLiteContract.ImagesTable.COLUMN_ID}) " +
                            "FROM ${SQLiteContract.ImagesTable.TABLE_NAME}" +
                        ");"
                )

                result = resultSet.getInt(SQLiteContract.ImagesTable.COLUMN_ID)
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
}