package database.entities

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("room") val room: Room = Room(),
    @SerializedName("image") val image: String = "",
    @SerializedName("value") val value: ByteArray = byteArrayOf(),
    @SerializedName("file") val file: ByteArray = byteArrayOf(),
    @SerializedName("owner") val owner: User = User(),
    @SerializedName("time") val time: String = "",
    @SerializedName("from") val from: String = "",
    @SerializedName("isRead") val isRead: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (room != other.room) return false
        if (image != other.image) return false
        if (!value.contentEquals(other.value)) return false
        if (!file.contentEquals(other.file)) return false
        if (owner != other.owner) return false
        if (time != other.time) return false
        if (isRead != other.isRead) return false
        return from == other.from
    }

    override fun hashCode(): Int {
        var result = room.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + value.contentHashCode()
        result = 31 * result + file.contentHashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + from.hashCode()
        result = 31 * result + isRead.hashCode()
        return result
    }
}
