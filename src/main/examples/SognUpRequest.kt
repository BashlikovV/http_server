
fun signUpRequest() {
    val client = OkHttpClient.Builder().build()

    val requestBodyString = Gson().toJson(SignUpRequestBody(
        username = "new_user",
        email = "new_user@gmail.com",
        password = "new_user"
    )
    ).toRequestBody(contentType)

    val request = Request.Builder()
        .post(requestBodyString)
        .url("http://${Repository.IP_ADDRESS}:${Repository.PORT}/sign-up")
        .build()

    val call = client.newCall(request)

    val response = call.execute()

    if (response.isSuccessful) {
        val responseBodyString = response.body!!.string()
        val signInResponseBody = Gson().fromJson(
            responseBodyString,
            SignUpResponseBody::class.java
        )
        println(signInResponseBody.result)
    } else {
        throw IllegalArgumentException()
    }
}