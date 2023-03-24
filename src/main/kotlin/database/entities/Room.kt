package database.entities

data class Room(
    val user1: User = User(),
    val user2: User = User(),
    val token: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (user1 != other.user1) return false
        if (user2 != other.user2) return false
        return token.contentEquals(other.token)
    }

    override fun hashCode(): Int {
        var result = user1.hashCode()
        result = 31 * result + user2.hashCode()
        result = 31 * result + token.contentHashCode()
        return result
    }
}
