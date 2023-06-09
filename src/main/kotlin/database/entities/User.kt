package database.entities

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("token") val token: ByteArray = byteArrayOf(),
    @SerializedName("salt") val salt: ByteArray = byteArrayOf(),
    @SerializedName("image") val image: ByteArray = byteArrayOf(),
    @SerializedName("createdAt") val createdAt: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (username != other.username) return false
        if (email != other.email) return false
        if (!token.contentEquals(other.token)) return false
        if (!salt.contentEquals(other.salt)) return false
        if (!image.contentEquals(other.image)) return false
        return createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + username.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + token.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + image.hashCode()
        return result
    }
}
