package com.github.wh5.mychat.viewmodel

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

    // 假设 getToken 是从 context 中获取 token 的方法
    private fun getToken(context: Context): String? {
        // 这里你可以获取 token，比如从 SharedPreferences 或 DataStore 获取
      val token= context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("token", null)

        Log.d("FriendViewModel", "Fetched token: $token")
        return token
    }

    // 加载好友列表
    fun loadFriends(context: Context) {
        viewModelScope.launch {
            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    Log.e("FriendViewModel", "Token is missing in loadFriends")
                    return@launch
                }

                Log.d("FriendViewModel", "Loading friends with token: $token")
                val response = ApiClient.getFriends(token)
                _friends.value = response
                Log.d("FriendViewModel", "Friends loaded: ${response.size}")
            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error loading friends: ${e.message}")
            }
        }
    }

    fun loadFriendRequests(context: Context) {
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

    fun loadPendingRequests(context: Context) {
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
        context: Context,
        requestId: Long,
        accept: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val token = getToken(context)
                if (token.isNullOrEmpty()) {
                    Log.e("FriendViewModel", "Token is missing")
                    onResult(false, "Token is missing")
                    return@launch
                }

                Log.d("FriendViewModel", "Handling friend request: $requestId, accept=$accept")
                val response = if (accept) {
                    ApiClient.apiService.acceptFriendRequest(
                        mapOf("requestId" to requestId.toString())
                    )
                } else {
                    ApiClient.apiService.rejectFriendRequest(
                        mapOf("requestId" to requestId.toString())
                    )
                }

                val message = response["message"] ?: "操作成功"
                loadFriendRequests(context)
                Log.d("FriendViewModel", "Friend request handled successfully: $message")
                onResult(true, message.toString())

            } catch (e: Exception) {
                Log.e("FriendViewModel", "Error handling friend request: ${e.message}")
                onResult(false, e.message ?: "操作失败")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptRequest(context: Context, requestId: Long, onResult: (Boolean, String) -> Unit) {
        handleFriendRequest(context, requestId, accept = true, onResult)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun rejectRequest(context: Context, requestId: Long, onResult: (Boolean, String) -> Unit) {
        handleFriendRequest(context, requestId, accept = false, onResult)
    }
}