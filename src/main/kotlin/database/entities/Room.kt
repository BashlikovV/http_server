package database.entities

data class Room(
    val user1: User = User(),
    val user2: User = User(),
    val token: String = ""
)
