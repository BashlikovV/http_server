package model.accounts

import database.SQLiteContract
import database.SQLiteMessengerRepository
import server.entities.SignInRequestBody
import server.entities.SignInResponseBody
import server.entities.SignUpRequestBody
import server.entities.SignUpResponseBody

class SQLiteAccountsRepository(
    databaseUrl: String = SQLiteContract.MESSENGER_SQLITE_DATABASE_URL
) : AccountsRepository {

    private val messengerRepository = SQLiteMessengerRepository(databaseUrl)

    override fun signUp(body: SignUpRequestBody): SignUpResponseBody {
        return try {
            messengerRepository.signUp(
                email = body.email,
                username = body.username,
                password = body.password,
                imageUri = body.image
            )

            SignUpResponseBody("200 OK")
        } catch (e: Exception) {
            e.printStackTrace()
            SignUpResponseBody("500 ERROR")
        }
    }

    override fun signIn(body: SignInRequestBody): SignInResponseBody {
        return try {
            val user = messengerRepository.signIn(
                email = body.email,
                password = body.password
            )
            val token = messengerRepository.securityUtils.bytesToString(user.token)

            SignInResponseBody(token)
        } catch (e: Exception) {
            e.printStackTrace()
            SignInResponseBody("500 ERROR")
        }
    }
}