package server.entities

data class UpdateUsernameRequestBody(
    val token: String,
    val newName: String
)

data class UpdateUsernameResponseBody(
    val result: String
)