package com.github.wh5.mychat.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.github.wh5.mychat.data.remote.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName

data class UserProfile(
    val nickname: String = "",
    val bio: String = "",
    val gender: Int = 0,
    val birthday: String = "",
    val location: String = "",
    val other: String = ""
)
data class GetFriendProfileReply(
    @SerialName("user_profile")
    val userProfile: UserProfile,
    @SerialName("unique_id")
    val uniqueId: String
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
//
//// ViewModel部分
//class ProfileViewModel(private val context: Context): ViewModel() {
//
//    // 用于保存用户资料
//    private val _userProfile = MutableStateFlow<UserProfile?>(null)
//    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
//
//    // 用于保存手机号
//    private val _phone = MutableStateFlow("")
//    val phone: StateFlow<String> = _phone.asStateFlow()
//
//    // 用于保存唯一标识
//    private val _uniqueId = MutableStateFlow("")
//    val uniqueId: StateFlow<String> = _uniqueId.asStateFlow()
//
//    // 用于判断是否已登录
//    private val _isLoggedIn = MutableStateFlow(false)
//    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
//
//    // 用于获取用户资料
//    suspend fun fetchUserProfile(uniqueId: String): UserProfile {
//        // 假设通过网络请求获取用户数据
//        val response = ApiClient.authService.getUserProfile(uniqueId)  // 这里替换成你的 API 调用
//        setPhone(response.phone)
//        setUniqueId(uniqueId)
//        return response.profile // 假设返回的是 ProfileResponse 类型
//    }
//
//    // 用于设置用户资料
//    fun setUserProfile(profile: UserProfile) {
//        _userProfile.value = profile
//    }
//
//    // 用于设置手机号
//    fun setPhone(phone: String) {
//        _phone.value = phone
//    }
//
//    // 用于设置唯一标识
//    fun setUniqueId(uniqueId: String) {
//        _uniqueId.value = uniqueId
//    }
//
//    // 用于设置登录状态
//    fun setLoginStatus(isLoggedIn: Boolean) {
//        _isLoggedIn.value = isLoggedIn
//    }
//
//    // 退出登录
//    fun logout() {
//        setUserProfile(UserProfile())
//        setPhone("")
//        setUniqueId("")
//        setLoginStatus(false)
//    }
//
//    // 登录状态设置
//    fun login() {
//        setLoginStatus(true)
//    }
//
//    // 更新 UniqueId（此函数可以处理后续逻辑扩展）
//    fun updateUniqueId(oldUniqueId: String, newUniqueId: String) {
//        if (oldUniqueId != newUniqueId) {
//            setUniqueId(newUniqueId)
//        }
//    }
//
//    // 加载用户资料并更新到状态流
//    suspend fun loadUserProfile(uniqueId: String) {
//        try {
//            val profile = fetchUserProfile(uniqueId)
//            setUserProfile(profile)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // 可以根据需要添加错误状态流用于界面提示
//        }
//    }
//}
