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
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.flow.filter
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListScope

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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的信息",
                style = MaterialTheme.typography.headlineLarge,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigate("edit_profile") },
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑资料",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.logout() },
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "退出登录",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text("唯一 ID") },
                            supportingContent = { Text(uniqueId) },
                            modifier = Modifier
                                .clickable {
                                    newUniqueId = uniqueId
                                    showDialog = true
                                }
                        )
                        Divider()
                        ListItem(
                            headlineContent = { Text("手机号") },
                            supportingContent = { Text(phone) }
                        )
                        if (profile != null) {
                            Divider()
                            ProfileDetail(profile!!)
                        } else {
                            Divider()
                            ListItem(
                                headlineContent = { Text("资料") },
                                supportingContent = { Text("加载中…") }
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
    // 使用 ListItem 显示各项资料，统一风格
    ListItem(
        headlineContent = { Text("昵称") },
        supportingContent = { Text(profile.nickname.ifBlank { "未设置" }) }
    )
    Divider()
    ListItem(
        headlineContent = { Text("性别") },
        supportingContent = {
            Text(
                when (profile.gender) {
                    1 -> "男"
                    2 -> "女"
                    else -> "未知"
                }
            )
        }
    )
    Divider()
    ListItem(
        headlineContent = { Text("生日") },
        supportingContent = { Text(profile.birthday.ifBlank { "未填写" }) }
    )
    Divider()
    ListItem(
        headlineContent = { Text("地区") },
        supportingContent = { Text(profile.location.ifBlank { "未填写" }) }
    )
    Divider()
    ListItem(
        headlineContent = { Text("个性签名") },
        supportingContent = { Text(profile.bio.ifBlank { "未填写" }) }
    )
}