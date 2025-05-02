package com.github.wh5.mychat.viewmodel

data class UserProfile(
    val nickname: String = "",
    val bio: String = "",
    val gender: Int = 0,
    val birthday: String = "",
    val location: String = "",
    val other: String = ""
)

data class ProfileRequest(
    val unique_id: String,
    val user_profile: UserProfile
)

data class ProfileReply(
    val message: String = "",
    val code: Int = 0
)
data class ProfileResponse(
    val profile: UserProfile,
    val phone: String,
    val msg: String
)
