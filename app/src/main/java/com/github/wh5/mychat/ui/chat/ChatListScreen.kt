package com.github.wh5.mychat.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.github.wh5.mychat.data.local.AppDatabase
import com.github.wh5.mychat.viewmodel.ChatListViewModel
import com.github.wh5.mychat.viewmodel.ChatListViewModelFactory
import com.github.wh5.mychat.viewmodel.ChatSession
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape

data class ChatSession(
    val friendId: String,
    val friendName: String,
    val lastMessage: String,
    val lastTime: String
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val factory = ChatListViewModelFactory(db.messageDao())
    val viewModel: ChatListViewModel = viewModel(factory = factory)

    val chatSessions by viewModel.chatSessions.collectAsState()
    var selfId by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        selfId = com.github.wh5.mychat.data.local.LoginPreferences.getUniqueIdOnce(context)
    }
    LaunchedEffect(chatSessions) {
        chatSessions.forEach {
            Log.d("ChatListScreen", "Session: friendId=${it.friendId}, name=${it.friendName}")
        }
    }
    val filteredSessions = chatSessions.filter { it.friendId != selfId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("聊天列表") }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(filteredSessions) { session ->
                Log.d(
                    "ChatListScreen",
                    "Rendering session: friendId=${session.friendId}, name=${session.friendName}, lastMessage=${session.lastMessage}, lastTime=${session.lastTime}"
                )
                ChatSessionItem(session) {
                    navController.navigate("chat_window/${session.friendId}")
                }
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            }
        }
    }
}

@Composable
fun ChatSessionItem(session: ChatSession, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = session.friendName, style = MaterialTheme.typography.titleMedium)
                Text(text = session.lastTime, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = session.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}