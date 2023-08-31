package server.entities

data class GetUsernameRequestBody(
    val token: String
)

data class GetUsernameResponseBody(
    val username: String
)