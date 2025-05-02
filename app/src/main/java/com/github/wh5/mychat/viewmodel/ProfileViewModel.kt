package com.github.wh5.mychat.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.wh5.mychat.data.local.LoginPreferences
import com.github.wh5.mychat.data.remote.ApiClient
import com.github.wh5.mychat.viewmodel.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val context: Context): ViewModel() {
    val uniqueIdFlow = LoginPreferences.getUniqueId(context)
    val phoneFlow = LoginPreferences.getPhone(context)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    fun loadUserProfile(uniqueId: String) {
        Log.d("ProfileViewModel", "调用 loadUserProfile，uniqueId = $uniqueId")
        viewModelScope.launch {
            try {
                val profile = ApiClient.authService.getUserProfile(uniqueId)
                Log.d("ProfileViewModel", "获取用户信息成功: $profile")  // 打印响应体
                _userProfile.value = profile.profile  // 赋值给 _userProfile
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "获取用户信息失败", e)
            }
        }
    }

    fun updateProfile(uniqueId: String, updatedProfile: UserProfile) {
        viewModelScope.launch {
            try {
                val request = ProfileRequest(unique_id = uniqueId, user_profile = updatedProfile)
                val result = ApiClient.authService.updateUserProfile(request)
                Log.d("ProfileViewModel", "资料更新成功：${result.message}")
                _userProfile.value = updatedProfile
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "资料更新失败", e)
            }
        }
    }

    // 实现修改唯一标识的功能
    fun updateUniqueId(oldUniqueId: String, newUniqueId: String) {
        viewModelScope.launch {
            try {
                // 创建请求体
                val request = UniqueIdRequest(uniqueId = oldUniqueId, newUniqueId = newUniqueId)

                // 调用后端接口更新唯一标识
                val response = ApiClient.authService.updateUniqueId(request)

                // 打印响应内容
                Log.d("ProfileViewModel", "唯一标识更新成功: ${response.msg}, 新的唯一标识: ${response.newUniqueId}")

                // 更新本地存储的 unique_id
                LoginPreferences.saveUniqueId(context, response.newUniqueId)

                loadUserProfile(response.newUniqueId)  // 重新加载资料
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "更新唯一标识失败", e)
            }
        }
    }
}