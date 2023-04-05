package server.entities

data class AddMessageRequestBody(
    val image: String,
    val file: ByteArray,
    val value: String,
    val time: String,
    val owner: String,
    val receiver: String,
    val from: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddMessageRequestBody

        if (image != other.image) return false
        if (!file.contentEquals(other.file)) return false
        if (value != other.value) return false
        if (time != other.time) return false
        if (owner != other.owner) return false
        if (receiver != other.receiver) return false
        return from == other.from
    }

    override fun hashCode(): Int {
        var result = image.hashCode()
        result = 31 * result + file.contentHashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + receiver.hashCode()
        result = 31 * result + from.hashCode()
        return result
    }
}

data class AddMessageResponseBody(
    val result: String
)