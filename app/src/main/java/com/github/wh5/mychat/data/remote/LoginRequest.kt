package com.github.wh5.mychat.data.remote

import com.github.wh5.mychat.viewmodel.EncryptionInfo

// data/remote/LoginRequest.kt
data class LoginRequest(val username: String, val password: String)

// data/remote/LoginResponse.kt
data class LoginResponse(val token: String)

data class RegisterRequest(
    val phone: String,
    val password: String,
    val deviceId: String,
    val encryption: EncryptionInfo
)

data class RegisterResponse(
    val msg: String,
    val uniqueId: String
)