package server.entities

data class AddRoomRequestBody(
    val user1: String,
    val user2: String
)

data class AddRoomResponseBody(
    val token: String
)