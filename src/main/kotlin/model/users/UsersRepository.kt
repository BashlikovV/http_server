package model.users

import server.HttpRequest
import server.entities.*

interface UsersRepository {

    fun getAllUsers(): GetUsersResponseBody

    fun getUsername(body: GetUsernameRequestBody): GetUsernameResponseBody

    fun getUser(body: GetUserRequestBody): GetUserResponseBody

    fun updateUsername(body: UpdateUsernameRequestBody): UpdateUsernameResponseBody
}