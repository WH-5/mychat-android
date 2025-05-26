package com.github.wh5.mychat.data.remote

import okhttp3.logging.HttpLoggingInterceptor

import android.util.Log
import com.github.wh5.mychat.common.AppConfig
import com.github.wh5.mychat.model.Friend
import com.github.wh5.mychat.model.FriendListResponse
import com.github.wh5.mychat.model.FriendRequest
import com.github.wh5.mychat.model.FriendRequestBody
import com.github.wh5.mychat.model.PendingRequest
import com.github.wh5.mychat.viewmodel.GetFriendProfileReply
import com.github.wh5.mychat.viewmodel.UserProfile
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body

// 添加 TokenInterceptor：可根据需要替换为从 DataStore 获取 token
class TokenInterceptor(private val getToken: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getToken()
        Log.d("TokenInterceptor", "Current token: $token") // 添加日志
        val newRequest = chain.request().newBuilder().apply {
            if (!token.isNullOrEmpty()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        try {
            val response = chain.proceed(newRequest)
            // 如果有错误响应，打印报错信息
            if (!response.isSuccessful) {
                Log.e("ApiClient", "Request failed: ${response.code} ${response.message}")
            }
            return response
        } catch (e: Exception) {
            Log.e("ApiClient", "Request failed: ${e.message}", e)
            throw e
        }
    }
}

object ApiClient {
    // 示例静态 token，正式项目应使用 DataStore 或缓存值替代
    public var token: String? = null


    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor { token })
        .addInterceptor(logging)
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

    // ApiService 用于定义后端接口，getFriends 是获取好友列表的接口
    interface ApiService {
        @retrofit2.http.GET("friend/list")
        suspend fun getFriends(): FriendListResponse

        @retrofit2.http.GET("friend-requests")
        suspend fun getFriendRequests(): List<FriendRequest>

        @retrofit2.http.POST("friend/request")
        suspend fun sendFriendRequest(
            @retrofit2.http.Body body: FriendRequestBody
        ): Map<String, String>

        @retrofit2.http.POST("friend/request/accept")
        suspend fun acceptFriendRequest(
            @Body body: Map<String, String> // 直接传递请求参数 Map
        ): Map<String, String>

        @retrofit2.http.POST("friend/request/reject")
        suspend fun rejectFriendRequest(
            @Body body: Map<String, String> // 直接传递请求参数 Map
        ): Map<String, String>

        @retrofit2.http.GET("friend/profile/{unique_id}")
        suspend fun getFriendProfile(
            @retrofit2.http.Path("unique_id") uniqueId: String
        ): GetFriendProfileReply

        @retrofit2.http.POST("friend/mark")
        suspend fun updateFriendRemark(
            @retrofit2.http.Body body: Map<String, String>
        ): Map<String, String>

        @retrofit2.http.POST("friend/delete")
        suspend fun deleteFriend(
            @retrofit2.http.Body body: Map<String, String>
        ): Map<String, String>

        @retrofit2.http.GET("friend/request/pending")
        suspend fun getPendingFriendRequests(): Map<String, List<PendingRequest>>
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // 新增：调用该方法获取好友列表
    suspend fun getFriends(token: String): List<Friend> {
        return apiService.getFriends().friends
    }

    // 获取好友请求
    suspend fun getFriendRequests(): List<FriendRequest> {
        return apiService.getFriendRequests()
    }

    // 接受好友请求
    suspend fun acceptFriendRequest(requestId: String): Map<String, String> {
        val body = mapOf("request_id" to requestId)
        return apiService.acceptFriendRequest(body)
    }

    // 拒绝好友请求
    suspend fun rejectFriendRequest(requestId: String): Map<String, String> {
        val body = mapOf("request_id" to requestId)
        return apiService.rejectFriendRequest(body)
    }

    suspend fun sendFriendRequest(targetId: String): String {
        val body = FriendRequestBody(targetId)
        apiService.sendFriendRequest(body)
        return "发送成功"
    }

    suspend fun getFriendProfile(uniqueId: String): GetFriendProfileReply {
        return apiService.getFriendProfile(uniqueId)
    }

    suspend fun updateFriendRemark( uniqueId: String, remark: String): Map<String, String> {
        val body = mapOf("unique_id" to uniqueId, "remark" to remark)
        return apiService.updateFriendRemark(body)
    }

    suspend fun deleteFriend(uniqueId: String): Map<String, String> {
        val body = mapOf("unique_id" to uniqueId)
        return apiService.deleteFriend(body)
    }

    suspend fun getPendingFriendRequests(): List<PendingRequest> {
        try {
            // 请求接口并获取响应
            val response = apiService.getPendingFriendRequests()

            // 打印整个响应内容以便调试
            Log.d("ApiClient", "Response: $response")

            // 从响应中提取 'requests' 键对应的数据
            val requests = response["requests"] ?: emptyList()

            // 打印获取到的请求数量
            Log.d("ApiClient", "Fetched pending friend requests: ${requests.size}")

            return requests
        } catch (e: Exception) {
            // 捕获异常并输出错误日志
            Log.e("ApiClient", "Failed to get pending friend requests: ${e.message}", e)
            throw e
        }
    }
}