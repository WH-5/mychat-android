package com.github.wh5.mychat.viewmodel

import android.R
import android.util.Log

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.github.wh5.mychat.api.ApiClient
import com.github.wh5.mychat.data.remote.ApiClient
//import com.github.wh5.mychat.model.Friend
import com.github.wh5.mychat.model.Friend
import com.github.wh5.mychat.model.FriendRequest
import com.github.wh5.mychat.model.PendingRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FriendViewModel : ViewModel() {
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends

    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests

    private val _pendingRequests = MutableStateFlow<List<PendingRequest>>(emptyList())
    val pendingRequests: StateFlow<List<PendingRequest>> = _pendingRequests

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 加载好友列表
    fun loadFriends() {
        viewModelScope.launch {
            try {
                val token = ApiClient.token
                if (token.isNullOrEmpty()) {
                    Log.e("FriendViewModel", "Token is missing in loadFriends")
                    return@launch
                }

                Log.d("FriendViewModel", "Loading friends with token: $token")
                val response = ApiClient.getFriends(token)
                _friends.value = response
                Log.d("FriendViewModel", "Friends loaded: ${response.size}")
                // 打印返回的原始数据，检查接口返回的内容
                Log.d("FriendViewModel", "Response from API: $response")
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error loading friends: ${e.message}")
            }
        }
    }

    fun loadFriendRequests() {
        viewModelScope.launch {
            try {

                val response = ApiClient.getFriendRequests()

                Log.d("FriendViewModel", "Sending request to API...")

                Log.d("FriendViewModel", "Received response: $response")
                _friendRequests.value = response

                Log.d("FriendViewModel", "Friend requests loaded: ${response.size}")
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error loading friend requests: ${e.message}")
            }
        }
    }

    fun loadPendingRequests() {
        viewModelScope.launch {
            try {
                val response = ApiClient.getPendingFriendRequests()
                _pendingRequests.value = response
                Log.d("FriendViewModel", "Pending requests loaded: ${response.size}")
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error loading pending requests: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleFriendRequest(
        requestId: String,
        accept: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = ApiClient.token
                if (token.isNullOrEmpty()) {
                    Log.e("FriendViewModel", "Token is missing")
                    onResult(false, "Token is missing")
                    return@launch
                }

                Log.d("FriendViewModel", "Handling friend request: $requestId, accept=$accept")
                val response = if (accept) {
                    ApiClient.apiService.acceptFriendRequest(
                        mapOf("other_unique_id" to requestId.toString())
                    )
                } else {
                    ApiClient.apiService.rejectFriendRequest(
                        mapOf("other_unique_id" to requestId.toString())
                    )
                }

                val message = response["message"] ?: "操作成功"
                loadFriendRequests()
                Log.d("FriendViewModel", "Friend request handled successfully: $message")
                onResult(true, message.toString())

            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error handling friend request: ${e.message}")
                onResult(false, e.message ?: "操作失败")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptRequest(requestId: String, onResult: (Boolean, String) -> Unit) {
        handleFriendRequest(requestId, accept = true, onResult)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun rejectRequest(requestId: String, onResult: (Boolean, String) -> Unit) {
        handleFriendRequest(requestId, accept = false, onResult)
    }

    private val _friendProfile = MutableStateFlow<UserProfile?>(null)
    val friendProfile: StateFlow<UserProfile?> = _friendProfile

    private val _isUpdatingNickname = MutableStateFlow(false)
    val isUpdatingNickname: StateFlow<Boolean> = _isUpdatingNickname

    // 获取好友资料
    fun getFriendProfile(uniqueId: String) {
        viewModelScope.launch {
            try {
                val token = ApiClient.token
                if (token.isNullOrEmpty()) {
                    Log.e("FriendViewModel", "Token is missing in getFriendProfile")
                    return@launch
                }

                Log.d("FriendViewModel", "Fetching friend profile with uniqueId: $uniqueId")
                val response = ApiClient.getFriendProfile(uniqueId)
                _friendProfile.value = response
                Log.d("FriendViewModel", "Friend profile loaded: $response")
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error loading friend profile: ${e.message}")
            }
        }
    }

    // 更新好友备注
    fun updateFriendRemark(uniqueId: String, newNickname: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val token = ApiClient.token
                if (token.isNullOrEmpty()) {
                    Log.e("FriendViewModel", "Token is missing in updateFriendNickname")
                    onResult(false, "Token is missing")
                    return@launch
                }

                _isUpdatingNickname.value = true
                Log.d("FriendViewModel", "Updating nickname for friend: $uniqueId to $newNickname")
                val response = ApiClient.updateFriendRemark(uniqueId, newNickname)

                val message = response["message"] ?: "操作成功"
                Log.d("FriendViewModel", "Nickname updated successfully: $message")
                _isUpdatingNickname.value = false
                onResult(true, message.toString())
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error updating nickname: ${e.message}")
                _isUpdatingNickname.value = false
                onResult(false, e.message ?: "操作失败")
            }
        }
    }
}