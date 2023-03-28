package database.entities

data class Message(
    val room: Room = Room(),
    val image: String = "",
    val value: String = "",
    val file: ByteArray = byteArrayOf(),
    val owner: User = User(),
    val time: String = "",
    val from: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (room != other.room) return false
        if (image != other.image) return false
        if (value != other.value) return false
        if (!file.contentEquals(other.file)) return false
        if (owner != other.owner) return false
        if (from != other.from) return false
        return time == other.time
    }

    override fun hashCode(): Int {
        var result = room.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + file.contentHashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }
}
