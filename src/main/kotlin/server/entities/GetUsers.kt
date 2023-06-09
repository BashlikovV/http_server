package server.entities

import com.google.gson.annotations.SerializedName
import database.entities.User

data class GetUsersRequestBody(val token: String)

data class GetUsersResponseBody(@SerializedName("users") val users: List<User>)