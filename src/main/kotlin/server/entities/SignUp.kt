package server.entities

data class SignUpRequestBody(
    val username: String,
    val email: String,
    val password: String,
    val image: String
)

data class SignUpResponseBody(
    val result: String
)