package com.github.wh5.mychat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.wh5.mychat.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val inputText by viewModel.input.collectAsState()
    val messages = viewModel.messages.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).imePadding()) {
        Text("聊天窗口", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // 消息列表（简单展示字符串）
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            messages.value.forEach { msg ->
                Text(text = msg, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                    .padding(8.dp)
            ) {
                if (inputText.isEmpty()) {
                    Text("请输入消息...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(onClick = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage()
                }
            }) {
                Text("发送")
            }
        }
    }
}