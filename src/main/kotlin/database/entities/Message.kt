package database.entities

data class Message(
    val room: Room = Room(),
    val isImage: Boolean = false,
    val image: String = "",
    val value: String = "",
    val owner: User = User(),
    val time: String = ""
)
