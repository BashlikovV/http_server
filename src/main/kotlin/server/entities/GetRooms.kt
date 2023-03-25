package server.entities

import database.entities.Room

data class GetRoomsRequestBody(
    val user: String
)

data class GetRoomsResponseBody(
    val rooms: List<Room>
)