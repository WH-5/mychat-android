package com.github.wh5.mychat.ui.friend

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.wh5.mychat.data.remote.ApiClient
import org.json.JSONObject

@Composable
fun AddFriendScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "添加好友",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("请输入好友用户名") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (username.isNotEmpty()) {
                    isSending = true
                    errorMessage = null
                } else {
                    errorMessage = "用户名不能为空"
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = !isSending
        ) {
            Text("发送请求")
        }

        // LaunchedEffect 用于触发请求，只在用户名不为空时触发
        LaunchedEffect(isSending) {
            if (isSending && username.isNotEmpty()) {
                try {
                    val response = ApiClient.sendFriendRequest( username)
                    // 发送成功的处理
                } catch (e: Exception) {
                    // 获取服务器返回的错误信息
                    var errorMsg = e.message ?: "请求失败"
                    android.util.Log.e("AddFriendScreen", "请求失败: $errorMsg")

                    // 如果捕获到 Retrofit 的响应错误，可以通过 response 进行进一步分析
                    if (e is retrofit2.HttpException) {
                        val responseBody = e.response()?.errorBody()?.string()
                        // 尝试从服务器返回的错误响应中获取 message 字段
                        val jsonResponse = responseBody?.let { JSONObject(it) }
                        val errorMessage = jsonResponse?.optString("message", "未知错误")
                        android.util.Log.e("AddFriendScreen", "错误响应: $errorMessage")
                        errorMsg = errorMessage ?: "请求失败"
                    }

                    errorMsg = "请求失败: $errorMsg"
                    errorMessage = errorMsg
                } finally {
                    isSending = false
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}