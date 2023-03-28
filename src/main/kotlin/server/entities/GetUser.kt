package server.entities

import database.entities.User

data class GetUserRequestBody(
    val token: String
)

data class GetUserResponseBody(
    val user: User
)