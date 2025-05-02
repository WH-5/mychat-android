package com.github.wh5.mychat.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.wh5.mychat.data.remote.ApiClient
import com.github.wh5.mychat.data.remote.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegisterViewModel : ViewModel() {

    private val _registerResult = MutableStateFlow("")
    val registerResult: StateFlow<String> = _registerResult

    fun register(phone: String, password: String, deviceId: String, encryption: EncryptionInfo) {
        viewModelScope.launch {
            try {
                val request = RegisterRequest(phone, password, deviceId, encryption)  // 传入 encryption 信息
                val response = ApiClient.authService.register(request)
                _registerResult.value = response.msg  // 将返回的消息存到 registerResult
            } catch (e: Exception) {
                val errorMessage = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()?.let { body ->
                    try {
                        val json = JSONObject(body)
                        json.getString("message")
                    } catch (_: Exception) {
                        null
                    }
                } ?: "注册失败: ${e.message}"
                _registerResult.value = "注册失败: $errorMessage"

            }
        }
    }
}
data class RegisterRequest(
    val phone: String,
    val password: String,
    val device_id: String,
    val encryption: EncryptionInfo  // 加入 encryption 字段
)

