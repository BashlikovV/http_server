package server.entities

import database.entities.Room

data class GetRoomRequestBody(
    val user1: String,
    val user2: String
)

data class GetRoomResponseBody(
    val room: Room
)