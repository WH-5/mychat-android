package com.github.wh5.mychat.data.remote

import com.github.wh5.mychat.data.remote.RegisterRequest
import com.github.wh5.mychat.data.remote.RegisterResponse
import com.github.wh5.mychat.viewmodel.ProfileRequest
import com.github.wh5.mychat.viewmodel.ProfileReply


import com.github.wh5.mychat.viewmodel.LoginReply
import com.github.wh5.mychat.viewmodel.ProfileResponse
import com.github.wh5.mychat.viewmodel.UniqueIdReply
import com.github.wh5.mychat.viewmodel.UniqueIdRequest
import com.github.wh5.mychat.viewmodel.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {

    @POST("user/register")
    suspend fun register(
        @Body req: RegisterRequest
    ): RegisterResponse

    @POST("user/login")
    suspend fun login(@Body loginRequest: Any): Response<LoginReply>

    @GET("user/profile/info/{unique_id}")
    suspend fun getUserProfile(@Path("unique_id") uniqueId: String): ProfileResponse


    @POST("user/profile")
    suspend fun updateUserProfile(@Body request: ProfileRequest): ProfileReply

    @POST("user/unique")
    suspend fun updateUniqueId(@Body request: UniqueIdRequest): UniqueIdReply



}