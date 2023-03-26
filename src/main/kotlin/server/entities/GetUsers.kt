package server.entities

import database.entities.User

data class GetUsersRequestBody(val token: String)

data class GetUsersResponseBody(val users: List<User>)