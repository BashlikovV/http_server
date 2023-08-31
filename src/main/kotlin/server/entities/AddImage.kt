package server.entities

data class AddImageRequestBody(
    val image: ByteArray,
    val room: ByteArray,
    val owner: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddImageRequestBody

        if (!image.contentEquals(other.image)) return false
        if (!room.contentEquals(other.room)) return false
        return owner.contentEquals(other.owner)
    }

    override fun hashCode(): Int {
        var result = image.contentHashCode()
        result = 31 * result + room.contentHashCode()
        result = 31 * result + owner.contentHashCode()
        return result
    }
}

data class AddImageResponseBody(
    val imageUri: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddImageResponseBody

        return imageUri.contentEquals(other.imageUri)
    }

    override fun hashCode(): Int {
        return imageUri.contentHashCode()
    }
}