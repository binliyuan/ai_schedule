package com.solunis.schedule.data.network

import com.solunis.schedule.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = TokenManager.getToken()

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("x-token", token)
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        val newToken = response.header("new-token")
        if (!newToken.isNullOrBlank()) {
            TokenManager.saveToken(newToken)
        }

        return response
    }
}
