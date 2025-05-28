package com.github.wh5.mychat.ui.friend

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.wh5.mychat.viewmodel.FriendViewModel
import com.github.wh5.mychat.viewmodel.UserProfile
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(friendName: String = "好友昵称", friendId: String, navController: NavController? = null, viewModel: FriendViewModel = viewModel()) {
    // 获取好友资料
    val friendProfile by viewModel.friendProfile.collectAsState(initial = null)

    var showEditDialog by remember { mutableStateOf(false) }
    var tempRemark by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    // 获取失败时的提示
    val isLoading = friendProfile == null

    LaunchedEffect(friendId) {
        // 加载好友资料
        viewModel.getFriendProfile(friendId)
        // 保存 publicKey 到 SharedPreferences
        val publicKey = viewModel.friendPublicKey.value
        if (!publicKey.isNullOrBlank()) {
            context.getSharedPreferences("friend_keys", 0)
                .edit() {
                    putString("key_$friendId", publicKey)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("好友资料") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // 显示好友资料
                ProfileCard(friendProfile, friendId, tempRemark, showEditDialog, onEditClick = {
                    tempRemark = TextFieldValue(friendProfile?.nickname ?: "")
                    showEditDialog = true
                })

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // 跳转到聊天页面
                        navController?.navigate("chat_window/$friendId")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("发送消息")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.deleteFriend(friendId) { success, message ->
                            Toast.makeText(context, if (success) "删除成功" else "删除失败：$message", Toast.LENGTH_SHORT).show()
                            if (success) {
                                navController?.popBackStack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("删除好友", color = Color.White)
                }
            }
        }
    }

    if (showEditDialog) {
        EditRemarkDialog(
            tempRemark = tempRemark,
            onRemarkChange = { tempRemark = it },
            onDismiss = { showEditDialog = false },
            onConfirm = {
                viewModel.updateFriendRemark(friendId, tempRemark.text) { success, message ->
                    Toast.makeText(context, if (success) "备注更新成功" else "备注更新失败", Toast.LENGTH_SHORT).show()
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ProfileCard(
    friendProfile: UserProfile?,
    friendId: String,
    tempRemark: TextFieldValue,
    showEditDialog: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "备注：${friendProfile?.nickname.takeIf { !it.isNullOrBlank() } ?: friendId}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onEditClick() }
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "个性签名：${friendProfile?.bio ?: "无"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "性别：${when(friendProfile?.gender) {
                    1 -> "男"
                    2 -> "女"
                    else -> "保密"
                }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "生日：${friendProfile?.birthday ?: "未填写"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "位置：${friendProfile?.location ?: "未填写"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun EditRemarkDialog(
    tempRemark: TextFieldValue,
    onRemarkChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改备注") },
        text = {
            OutlinedTextField(
                value = tempRemark,
                onValueChange = onRemarkChange,
                label = { Text("备注") }
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("确定")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}