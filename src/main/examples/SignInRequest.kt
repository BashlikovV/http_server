val contentType = "application/json; charset=utf-8".toMediaType()
fun SignInRequest() {
    val client = OkHttpClient.Builder().build()

    val requestBodyString = Gson().toJson(SignInRequestBody(
        email = "admin@gmail.com",
        password = "12345"
    )).toRequestBody(contentType)

    val request = Request.Builder()
        .post(requestBodyString)
        .url("http://${Repository.IP_ADDRESS}:${Repository.PORT}/sign-in")
        .build()

    val call = client.newCall(request)

    val response = call.execute()

    if (response.isSuccessful) {
        val responseBodyString = response.body!!.string()
        val signInResponseBody = Gson().fromJson(
            responseBodyString,
            SignInResponseBody::class.java
        )
        println(signInResponseBody.token)
    } else {
        throw IllegalArgumentException()
    }
}