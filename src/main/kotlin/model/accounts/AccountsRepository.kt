package model.accounts

import server.entities.SignInRequestBody
import server.entities.SignInResponseBody
import server.entities.SignUpRequestBody
import server.entities.SignUpResponseBody

interface AccountsRepository {

    fun signUp(body: SignUpRequestBody): SignUpResponseBody

    fun signIn(body: SignInRequestBody): SignInResponseBody


}