package com.github.wh5.mychat.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.provider.Settings
import android.content.Context
import android.os.Build
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.wh5.mychat.viewmodel.RegisterViewModel
import androidx.annotation.RequiresApi
import com.github.wh5.mychat.viewmodel.EncryptionInfo
import generateEncryptionInfo
import org.json.JSONObject

fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegisterScreen(
    context: Context,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showEmptyFieldsError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val deviceId = getDeviceId(context)
    val viewModel: RegisterViewModel = viewModel()
    val registerResult by viewModel.registerResult.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Text("注册账号", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("手机号") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    val encryptionInfo = generateEncryptionInfo(password)
                    viewModel.register(username, password, deviceId, encryptionInfo)
                } else {
                    showEmptyFieldsError = true
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("注册")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBackToLogin) {
                Text("返回登录")
            }

            if (registerResult.isNotBlank()) {
                if (registerResult.contains("注册失败")) {
                    LaunchedEffect(registerResult) {
                        snackbarHostState.showSnackbar(registerResult)
                    }
                } else {
                    onRegisterSuccess()
                }
            }

            if (showEmptyFieldsError) {
                LaunchedEffect(Unit) {
                    snackbarHostState.showSnackbar("请输入用户名和密码")
                    showEmptyFieldsError = false
                }
            }
        }
    }
}
