package com.github.wh5.mychat.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.github.wh5.mychat.viewmodel.ChatViewModel
import com.github.wh5.mychat.viewmodel.ChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatWindowScreen(
    navController: NavController? = null,
    friendId: String
) {
    val context = LocalContext.current
    val db = com.github.wh5.mychat.data.local.AppDatabase.getDatabase(context)
    val viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.github.wh5.mychat.viewmodel.ChatViewModelFactory(friendId, db.messageDao(),context)
    )

    val messages = viewModel.chatHistories.getOrPut(friendId) { mutableStateListOf() }
    val input by viewModel.input.collectAsState()

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("与 $friendId 聊天", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = input,
                        onValueChange = { viewModel.updateInput(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入消息...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (input.isNotBlank()) {
                            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                            val sharedPrefs = context.getSharedPreferences("friend_keys", 0)
                            val keyString = sharedPrefs.getString("key_$friendId", null)

                            android.util.Log.d("WebSocket", "点击发送按钮，准备发送消息: $input")
                            android.util.Log.d("WebSocket", "调用 sendMessage，使用的 key: $keyString")
                            viewModel.sendMessage(keyString.toString())

                            val newMessage = ChatMessage(content = input, isMe = true, timestamp = timestamp)
                            viewModel.chatHistories.getOrPut(friendId) { mutableStateListOf() }.add(newMessage)

                            viewModel.updateInput("") // 清空输入框

                            coroutineScope.launch {
                                listState.animateScrollToItem(viewModel.chatHistories[friendId]?.lastIndex ?: 0)
                            }
                        }
                    }) {
                        Text("发送")
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            items(messages) { msg ->
                if (msg.content.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.content,
                                modifier = Modifier.padding(12.dp),
                                color = if (msg.isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
