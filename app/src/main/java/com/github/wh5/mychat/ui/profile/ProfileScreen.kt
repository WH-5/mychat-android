package com.github.wh5.mychat.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.github.wh5.mychat.viewmodel.ProfileViewModel
import com.github.wh5.mychat.viewmodel.UserProfile
import android.util.Log
import kotlinx.coroutines.flow.filter

@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(context) as T
        }
    })

    val uniqueId by viewModel.uniqueIdFlow.collectAsState(initial = "")


    val phone by viewModel.phoneFlow.collectAsState(initial = "")
    val profile by viewModel.userProfile.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var newUniqueId by remember { mutableStateOf("") }

    LaunchedEffect(uniqueId) {
        Log.d("ProfileScreen", "当前 uniqueId: $uniqueId")
        if (uniqueId.isNotBlank()) {
            try {
                val newProfile = viewModel.fetchUserProfile(uniqueId)
                if (newProfile != viewModel.userProfile.value) {
                    viewModel.setUserProfile(newProfile)
                    Log.d("ProfileScreen", "用户资料已更新: $newProfile")
                } else {
                    Log.d("ProfileScreen", "用户资料未变化")
                }
            } catch (e: Exception) {
                Log.e("ProfileScreen", "加载用户资料失败: ${e.message}")
                errorMessage = "加载用户资料失败: ${e.message}"
                showErrorDialog = true
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        Log.d("ProfileScreen", "isLoggedIn changed: $isLoggedIn")
        if (isLoggedIn == false) {
            navController.navigate("login") {
                popUpTo("profile") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "我的信息", style = MaterialTheme.typography.headlineSmall)

        Text(
            text = "唯一 ID：$uniqueId",
            modifier = Modifier
                .clickable {
                    newUniqueId = uniqueId
                    showDialog = true
                }
        )
        Text(text = "手机号：$phone")

        if (profile != null) {
            ProfileDetail(profile!!)
        } else {
            Text(text = "加载中…")
        }

        Button(
            onClick = {
                navController.navigate("edit_profile")
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("编辑资料")
        }

        Button(
            onClick = {
                viewModel.logout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("退出登录", color = MaterialTheme.colorScheme.onError)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    if (newUniqueId.isNotBlank()) {

                        viewModel.updateUniqueId(uniqueId, newUniqueId)
                        // 移除 viewModel.loadUserProfile(newUniqueId)，由 LaunchedEffect 响应 uniqueId 变化自动加载
                    }
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("取消")
                }
            },
            title = { Text("修改唯一标识") },
            text = {
                OutlinedTextField(
                    value = newUniqueId,
                    onValueChange = { newUniqueId = it },
                    label = { Text("新的唯一标识") },
                    singleLine = true
                )
            }
        )
    }

    if (errorMessage != null) {
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage!!)
            errorMessage = null
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )

    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("确定")
                }
            },
            title = { Text("错误") },
            text = { Text(errorMessage ?: "") }
        )
    }
}

@Composable
fun ProfileDetail(profile: UserProfile) {
    // Show user profile details, with fallback to "未设置" for blank values
    Text(text = "昵称：${profile.nickname.ifBlank { "未设置" }}")
    Text(text = "性别：${when (profile.gender) {
        1 -> "男"
        2 -> "女"
        else -> "未知"
    }}")
    Text(text = "生日：${profile.birthday.ifBlank { "未填写" }}")
    Text(text = "地区：${profile.location.ifBlank { "未填写" }}")
    Text(text = "个性签名：${profile.bio.ifBlank { "未填写" }}")
}