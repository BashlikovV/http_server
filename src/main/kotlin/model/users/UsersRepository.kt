package model.users

import server.entities.*

interface UsersRepository {

    fun getAllUsers(token: String): GetUsersResponseBody

    fun getUsername(body: GetUsernameRequestBody): GetUsernameResponseBody

    fun getUser(body: GetUserRequestBody): GetUserResponseBody

    fun updateUsername(body: UpdateUsernameRequestBody): UpdateUsernameResponseBody

    fun checkUserToken(token: String): Boolean
}