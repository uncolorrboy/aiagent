package ru.sapozhnikov.aiagent.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** OkHttp-интерцептор, добавляющий заголовок авторизации Bearer для DeepSeek API. */
internal class AuthInterceptor(
    private val apiKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}
