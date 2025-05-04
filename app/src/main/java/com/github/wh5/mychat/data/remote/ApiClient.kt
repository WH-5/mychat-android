package com.github.wh5.mychat.data.remote

import android.util.Log
import com.github.wh5.mychat.common.AppConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 添加 TokenInterceptor：可根据需要替换为从 DataStore 获取 token
class TokenInterceptor(private val getToken: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getToken()
        val newRequest = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        Log.d("ApiClient", "Request Headers: ${newRequest.headers}")
        return chain.proceed(newRequest)
    }
}

object ApiClient {
    // 示例静态 token，正式项目应使用 DataStore 或缓存值替代
    private var token: String? = null

    fun setToken(value: String) {
        token = value
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor { token })
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
}