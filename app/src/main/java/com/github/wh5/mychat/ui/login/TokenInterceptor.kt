package com.github.wh5.mychat.ui.login

import okhttp3.Interceptor
import okhttp3.Response

/**
 * TokenInterceptor 用于在每个请求中自动添加 Authorization 头。
 * 使用方式：
 * 创建 OkHttpClient 时传入此拦截器，并提供一个函数用于获取当前 token。
 *
 * 例如：
 * OkHttpClient.Builder()
 *     .addInterceptor(TokenInterceptor { tokenProvider.getToken() })
 *     .build()
 */
class TokenInterceptor(private val getToken: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getToken()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}