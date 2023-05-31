package model.users

import database.SQLiteContract
import database.SQLiteMessengerRepository
import database.entities.User
import server.entities.*

class SQLiteUsersRepository(
    databaseUrl: String = SQLiteContract.MESSENGER_SQLITE_DATABASE_URL
) : UsersRepository {

    private val messengerRepository = SQLiteMessengerRepository(databaseUrl)

    override fun getAllUsers(): GetUsersResponseBody {
        return try {
            GetUsersResponseBody(messengerRepository.getAllUsers())
        } catch (e: Exception) {
            e.printStackTrace()
            GetUsersResponseBody(listOf())
        }
    }

    override fun getUsername(body: GetUsernameRequestBody): GetUsernameResponseBody {
        return try {
            val user = messengerRepository.getUserByToken(body.token)

            GetUsernameResponseBody(username = user.username)
        } catch (e: Exception) {
            e.printStackTrace()
            GetUsernameResponseBody("500 ERROR")
        }
    }

    override fun getUser(body: GetUserRequestBody): GetUserResponseBody {
        return try {
            GetUserResponseBody(messengerRepository.getUserByToken(body.token))
        } catch (e: Exception) {
            e.printStackTrace()
            GetUserResponseBody(User())
        }
    }

    override fun updateUsername(body: UpdateUsernameRequestBody): UpdateUsernameResponseBody {
        return try {
            messengerRepository.updateUsernameByToken(body.token, body.newName)

            UpdateUsernameResponseBody("200 OK")
        } catch (e: Exception) {
            e.printStackTrace()
            UpdateUsernameResponseBody("500 ERROR")
        }
    }

    override fun checkUserToken(token: String): Boolean {
        return try {
            messengerRepository.getUserByToken(token)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}