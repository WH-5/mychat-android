package com.github.wh5.mychat.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
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
        factory = com.github.wh5.mychat.viewmodel.ChatViewModelFactory(friendId, db.messageDao())
    )

    val messages = viewModel.chatHistories[friendId] ?: emptyList()
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
            TopAppBar(
                title = { Text("与 $friendId 聊天") }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = input,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (input.isNotBlank()) {
                        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

                        viewModel.sendMessage()

                        val newMessage = ChatMessage(content = input, isMe = true, timestamp = timestamp)
                        viewModel.chatHistories.getOrPut(friendId) { mutableListOf() }.add(newMessage)

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
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        text = msg.content,
                        modifier = Modifier
                            .background(
                                if (msg.isMe) MaterialTheme.colorScheme.primary
                                else Color.LightGray
                            )
                            .padding(12.dp),
                        color = if (msg.isMe) Color.White else Color.Black
                    )
                }
            }
        }
    }
}