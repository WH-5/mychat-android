package com.github.wh5.mychat.ui.splash

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.wh5.mychat.data.local.LoginPreferences
import com.github.wh5.mychat.data.remote.ApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    suspend fun checkLoginStatus(context: Context) {
        val token = LoginPreferences.getTokenOnce(context)
        Log.d("SplashViewModel", "读取到 token: $token")
        val loggedIn = token.isNotBlank()
        Log.d("SplashViewModel", "设置 isLoggedIn = $loggedIn")
        delay(1000L) // 添加延迟，确保页面过渡顺利
        _isLoggedIn.value = loggedIn
    }
    // 定义一个 autoLogin 方法来处理自动登录逻辑
    suspend fun autoLogin(context: Context, onLoginSuccess: () -> Unit) {
        val token = LoginPreferences.getTokenOnce(context)
        Log.d("SplashViewModel", "autoLogin: 读取到 token: $token")
        if (token.isNullOrEmpty()) {
            Log.d("SplashViewModel", "autoLogin: token 不存在")
            _isLoggedIn.value = false
        } else {
            ApiClient.token = token// 设置 token 到 ApiClient
            _isLoggedIn.value = true
            Log.d("SplashViewModel", "autoLogin: 设置 isLoggedIn = true")
            onLoginSuccess()  // 登录成功后执行的操作
        }
    }

    fun setLoginStatus(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
    }
}