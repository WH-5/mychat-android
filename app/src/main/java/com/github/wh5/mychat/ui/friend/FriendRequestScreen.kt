package com.github.wh5.mychat.ui.friend

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.wh5.mychat.viewmodel.FriendViewModel
import com.github.wh5.mychat.model.PendingRequest
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestScreen(
    navController: NavController,
    context: Context,
    viewModel: FriendViewModel = viewModel()
) {
    val friendRequests by viewModel.pendingRequests.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    // 页面加载时不再自动调用，改为点击按钮后加载
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("好友请求", fontSize = 20.sp) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Button(onClick = {
                // 点击按钮后加载好友请求
                viewModel.loadPendingRequests(context)
                android.util.Log.d("FriendRequestScreen", "加载好友请求接口调用了")
            }) {
                Text("加载好友请求")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                friendRequests.isEmpty() -> {
                    Text("暂无好友请求")
                }
                else -> {
                    friendRequests.forEach { request ->
                        PendingRequestItem(
                            request = request,
                            onAccept = { requestId ->
                                viewModel.acceptRequest(context = context, requestId = requestId) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onReject = { requestId ->
                                viewModel.rejectRequest(context = context, requestId = requestId) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingRequestItem(
    request: PendingRequest,
    onAccept: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = request.senderId, style = MaterialTheme.typography.bodyLarge)
            Row {
                Button(onClick = { onAccept(request.senderId.toLong()) }, modifier = Modifier.padding(end = 8.dp)) {
                    Text("同意")
                }
                OutlinedButton(onClick = { onReject(request.senderId.toLong()) }) {
                    Text("拒绝")
                }
            }
        }
    }
}