package com.github.wh5.mychat.ui.friend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
//import com.github.wh5.mychat.ui.friend.viewmodel.FriendViewModel
import com.github.wh5.mychat.model.Friend
import com.github.wh5.mychat.viewmodel.FriendViewModel

@Composable
fun FriendListScreen(navController: NavController, viewModel: FriendViewModel = viewModel()) {
    // 获取好友列表数据
    val friends by viewModel.friends.collectAsState(initial = emptyList())

    val context = LocalContext.current


    LaunchedEffect(Unit) {
        // 在页面加载时调用 ViewModel 的方法加载数据
        viewModel.loadFriends()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(
            text = "好友列表",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    navController.navigate("friend_requests")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("好友请求")
            }
            Button(
                onClick = {
                    navController.navigate("add_friend")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("添加好友")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(friends) { friend ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            navController.navigate("friend_detail/${friend.uniqueId}")
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (!friend.nickname.isNullOrEmpty()) friend.nickname!! else (friend.uniqueId ?: ""),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "账号：${friend.uniqueId ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}