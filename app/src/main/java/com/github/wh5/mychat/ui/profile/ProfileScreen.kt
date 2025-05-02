package com.github.wh5.mychat.ui.profile

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

    var newUniqueId by remember { mutableStateOf("") }

    LaunchedEffect(uniqueId) {
        if (uniqueId.isNotBlank()) {
            viewModel.loadUserProfile(uniqueId)
            Log.d("ProfileScreen", "请求用户资料，uniqueId: $uniqueId")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "我的信息", style = MaterialTheme.typography.headlineSmall)

        Text(text = "唯一 ID：$uniqueId")
        Text(text = "手机号：$phone")

        // Check if profile is available
        if (profile != null) {
            ProfileDetail(profile!!)
        } else {
            // If profile is null, show loading state
            Text(text = "加载中…")
        }

        // 修改唯一标识输入框
        OutlinedTextField(
            value = newUniqueId,
            onValueChange = { newUniqueId = it },
            label = { Text("新的唯一标识") },
            modifier = Modifier.fillMaxWidth(),
            isError = newUniqueId.isBlank()  // 输入为空时显示错误
        )

        // 提交修改唯一标识按钮
        Button(
            onClick = {
                if (newUniqueId.isNotBlank()) {
                    viewModel.updateUniqueId(uniqueId, newUniqueId)
                    // 更新唯一标识后，重新加载用户资料
                    viewModel.loadUserProfile(newUniqueId)
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("修改唯一标识")
        }

        Button(
            onClick = {
                navController.navigate("edit_profile")  // 跳转到编辑资料页面
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("编辑资料")
        }
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