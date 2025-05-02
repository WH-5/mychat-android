package com.github.wh5.mychat.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.wh5.mychat.common.AppConfig.BASE_URL
import com.github.wh5.mychat.data.local.LoginPreferences
import com.github.wh5.mychat.data.remote.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.Body
import retrofit2.http.POST

import android.util.Base64
import android.util.Log
import com.github.wh5.mychat.data.remote.ApiClient
import com.google.gson.annotations.SerializedName
import decryptPrivateKey
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

import org.json.JSONObject

// 1. 定义 Retrofit 客户端，使用 BASE_URL 变量，增加 OkHttp 日志拦截器
object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // 打印请求和响应的全部内容
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)  // 使用带日志功能的 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

// 3. 在 ViewModel 中使用 Retrofit 发起请求
class LoginViewModel : ViewModel() {

    private val authService = RetrofitClient.instance.create(AuthService::class.java)

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    // 登录成功保存数据
    fun loginSuccess(context: Context, loginResponse: LoginReply) {
        viewModelScope.launch {
            LoginPreferences.saveLoginInfo(
                context = context,
                token = loginResponse.token,
                uniqueId = loginResponse.uniqueId,
                phone = loginResponse.phone,
                kdfSalt = loginResponse.encryption.kdfSalt,
                publicKey = loginResponse.encryption.publicKey,
                encryptedPrivateKey = loginResponse.encryption.encryptedPrivateKey
            )
        }
    }

    // 使用手机号登录
    fun loginWithPhone(phone: String, password: String, context: Context, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(phone = phone, password = password)
                val response = authService.login(loginRequest)
                Log.d("LoginResponse", "Raw response body: ${response.raw().body}")
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // 解密逻辑
                    val encryptedPrivateKeyBase64 = loginResponse.encryption.encryptedPrivateKey
                    Log.d("EncryptedPrivateKey", "Encrypted private key: $encryptedPrivateKeyBase64")
                    if (encryptedPrivateKeyBase64.isNullOrEmpty()) {
                        showError("加密的私钥为空")
                        return@launch
                    }

                    val decryptedPrivateKey = decryptPrivateKey(
                        encryptedPrivateKeyBase64 = encryptedPrivateKeyBase64,
                        password = password,
                        saltBase64 = loginResponse.encryption.kdfSalt
                    )

                    val realPrivateKey = Base64.encodeToString(decryptedPrivateKey, Base64.NO_WRAP)

                    // 保存数据
                    LoginPreferences.saveLoginInfo(
                        context = context,
                        token = loginResponse.token,
                        uniqueId = loginResponse.uniqueId,
                        phone = loginResponse.phone,
                        kdfSalt = loginResponse.encryption.kdfSalt,
                        publicKey = loginResponse.encryption.publicKey,
                        encryptedPrivateKey = realPrivateKey  // 保存解密后的真实私钥
                    )

                    ApiClient.setToken(loginResponse.token) // 设置 token
                    onLoginSuccess()  // 登录成功后跳转
                } else {
                    showError("手机号或密码错误")
                }
            } catch (e: Exception) {
                val errorMessage = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()?.let { body ->
                    try {
                        val json = JSONObject(body)
                        json.getString("message")
                    } catch (_: Exception) {
                        null
                    }
                } ?: "登录失败: ${e.message}"
                showError(errorMessage)
                Log.e("LoginError", "Login failed: $errorMessage", e)
            }
        }
    }

    // 使用唯一标识登录
    fun loginWithUniqueId(uniqueId: String, password: String, context: Context, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequestWithUniqueId(unique_id = uniqueId, password = password)
                val response = authService.login(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // 解密逻辑
                    val encryptedPrivateKeyBase64 = loginResponse.encryption.encryptedPrivateKey
                    if (encryptedPrivateKeyBase64.isNullOrEmpty()) {
                        showError("加密的私钥为空")
                        return@launch
                    }

                    val decryptedPrivateKey = decryptPrivateKey(
                        encryptedPrivateKeyBase64 = encryptedPrivateKeyBase64,
                        password = password,
                        saltBase64 = loginResponse.encryption.kdfSalt
                    )
                    val realPrivateKey = Base64.encodeToString(decryptedPrivateKey, Base64.NO_WRAP)

                    // 保存数据
                    LoginPreferences.saveLoginInfo(
                        context = context,
                        token = loginResponse.token,
                        uniqueId = loginResponse.uniqueId,
                        phone = loginResponse.phone,
                        kdfSalt = loginResponse.encryption.kdfSalt,
                        publicKey = loginResponse.encryption.publicKey,
                        encryptedPrivateKey = realPrivateKey  // 保存解密后的真实私钥
                    )

                    ApiClient.setToken(loginResponse.token) // 设置 token
                    onLoginSuccess()  // 登录成功后跳转
                } else {
                    showError("唯一标识或密码错误")
                }
            } catch (e: Exception) {
                val errorMessage = (e as? retrofit2.HttpException)?.response()?.errorBody()?.string()?.let { body ->
                    try {
                        val json = JSONObject(body)
                        json.getString("message")
                    } catch (_: Exception) {
                        null
                    }
                } ?: "登录失败: ${e.message}"
                showError(errorMessage)
                Log.e("LoginError", "Login failed: $errorMessage", e)
            }
        }
    }

    // 显示错误信息并更新 StateFlow
    private fun showError(message: String) {
        _errorMessage.value = message
    }

    // 清除错误信息
    fun clearError() {
        _errorMessage.value = ""
    }
}


data class LoginRequest(
    val phone: String,
    val password: String
)

data class LoginRequestWithUniqueId(
    val unique_id: String,
    val password: String
)

// LoginReply.kt
data class LoginReply(
    val token: String,
    val uniqueId: String,
    val phone: String,
    val encryption: EncryptionInfo
)

data class EncryptionInfo(
    val kdfSalt: String,
    val publicKey: String,
    val encryptedPrivateKey: String
)


//// 1. 派生密钥
//fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
//    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//    val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
//    val secret = factory.generateSecret(spec)
//    return SecretKeySpec(secret.encoded, "AES")
//}
//
//// 2. 解密加密的私钥
//fun decryptPrivateKey(encryptedPrivateKeyBase64: String?, password: String, saltBase64: String): ByteArray? {
//    if (encryptedPrivateKeyBase64.isNullOrEmpty()) {
//        Log.e("LoginError", "Encrypted private key is null or empty")
//        return null
//    }
//
//    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
//    Log.d("Decrypt", "Salt (Base64): $saltBase64")
//
//    val encrypted = Base64.decode(encryptedPrivateKeyBase64, Base64.NO_WRAP)
//    Log.d("Decrypt", "Encrypted input (Base64): $encryptedPrivateKeyBase64")
//
//    val key = deriveKey(password, salt)
//    Log.d("Decrypt", "Derived AES key (Base64): ${Base64.encodeToString(key.encoded, Base64.NO_WRAP)}")
//
//    val iv = encrypted.sliceArray(0 until 12)
//    Log.d("Decrypt", "Extracted IV (Base64): ${Base64.encodeToString(iv, Base64.NO_WRAP)}")
//
//    val cipherText = encrypted.sliceArray(12 until encrypted.size)
//    Log.d("Decrypt", "Extracted CipherText (Base64): ${Base64.encodeToString(cipherText, Base64.NO_WRAP)}")
//
//    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
//    val spec = GCMParameterSpec(128, iv)
//    cipher.init(Cipher.DECRYPT_MODE, key, spec)
//
//    val result = cipher.doFinal(cipherText)
//    Log.d("Decrypt", "Decrypted private key (Base64): ${Base64.encodeToString(result, Base64.NO_WRAP)}")
//
//    return result
//}
//
