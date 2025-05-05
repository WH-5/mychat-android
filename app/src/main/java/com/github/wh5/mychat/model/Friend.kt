package com.github.wh5.mychat.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * 好友数据模型
 * @param id 用户唯一标识
 * @param username 用户名（用于登录）
 * @param nickname 用户设置的昵称（显示优先级高于用户名）
 * @param avatarUrl 用户头像链接（可选）
 */
@Serializable
data class Friend(
    val id: String,
    val username: String,
    val nickname: String,
    val avatarUrl: String? = null
) {
    /**
     * 获取显示名称：优先昵称，没有则返回用户名
     */
    fun getDisplayName(): String {
        return if (nickname.isNotBlank()) nickname else username
    }
}
/**
 * 好友请求数据模型
 * @param id 请求唯一标识
 * @param senderId 请求发送者的用户ID
 * @param receiverId 请求接收者的用户ID
 * @param status 请求的状态（例如待处理、已接受、已拒绝等）
 * @param timestamp 请求的时间戳
 */
@Serializable
data class FriendRequest(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val status: String,  // 例如：pending, accepted, rejected
    val timestamp: Long
)

/**
 * 发送好友请求请求体
 * @param target_unique_id 目标用户的唯一ID
 */
@Serializable
data class FriendRequestBody(
    @SerialName("target_unique_id")
    val targetUniqueId: String
)

@Serializable
data class PendingRequest(
    @SerializedName("fromId")
    val senderId: String,

    @SerializedName("requestTime")
    val requestTime: String
)