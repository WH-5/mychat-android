package com.github.wh5.mychat.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.wh5.mychat.data.local.LoginPreferences
import com.github.wh5.mychat.data.local.MessageDao
import com.github.wh5.mychat.data.local.MessageEntity
import com.github.wh5.mychat.data.remote.ws.WebSocketManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.util.Base64
import androidx.compose.runtime.toMutableStateList

data class ChatMessage(val content: String, val isMe: Boolean, val timestamp: String)

class ChatViewModel(
    private val context: Context,
    private val currentFriendId: String,
    private val messageDao: MessageDao
) : ViewModel() {
    private val _chatHistories = mutableStateMapOf<String, SnapshotStateList<ChatMessage>>()
    val chatHistories: SnapshotStateMap<String, SnapshotStateList<ChatMessage>> = _chatHistories

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input

    init {
        viewModelScope.launch {
            val userId = LoginPreferences.getUniqueIdOnce(context)
            messageDao.getMessagesForFriend(userId = userId, friendId = currentFriendId).collect { list ->
                _chatHistories[currentFriendId] = list.map {
                    ChatMessage(it.content, it.isMe, it.timestamp)
                }.toMutableStateList()
            }
        }
        // 注册 WebSocket 消息回调
        WebSocketManager.setOnMessageCallback { raw ->
            try {
                val json = JSONObject(raw)
                val type = json.getInt("type")
                if (type == 1) {
                    val from = json.getString("unique_id")
                    val timestamp = json.getString("timestamp")
                    val payloadBase64 = json.getString("payload")
                    val payloadJson = JSONObject(
                        String(Base64.decode(payloadBase64, Base64.DEFAULT))
                    )
                    val content = payloadJson.optString("content")

                    val message = ChatMessage(content, isMe = false, timestamp = timestamp)
                    _chatHistories.getOrPut(from) { mutableStateListOf() }.add(message)

                    viewModelScope.launch {
                        val userId = LoginPreferences.getUniqueIdOnce(context)
                        messageDao.insertMessage(
                            MessageEntity(
                                userId = userId,
                                friendId = from,
                                content = content,
                                timestamp = timestamp,
                                isMe = false
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatViewModel", "接收消息处理失败: ${e.message}")
            }
        }
    }

    fun updateInput(newInput: String) {
        _input.value = newInput
    }

    fun getMessagesFor(friendId: String): List<ChatMessage> {
        return _chatHistories[friendId] ?: mutableListOf()
    }

    fun sendMessage(friendKey: String) {
        val msg = _input.value
        if (msg.isNotBlank()) {
            try {
                // 使用公钥加密消息内容
//                val publicKey = base64ToPublicKey(friendKey) // 得到一个 PublicKey
//                val encryptedMsg = encryptWithKey(publicKey, msg)
//                android.util.Log.d("WebSocket", "加密后的消息内容: $encryptedMsg")
                val payloadJson = JSONObject().apply {
                    put("content", msg)
                    put("message_type", "text")
                }
                val payloadBase64 = Base64.encodeToString(
                    payloadJson.toString().toByteArray(),
                    Base64.NO_WRAP
                )

                val messageJson = JSONObject().apply {
                    put("type", 1)
                    put("target_unique", currentFriendId)
                    put("payload", payloadBase64)
                }
                android.util.Log.d("WebSocket", "准备发送消息: $messageJson")

                WebSocketManager.send(messageJson.toString())
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                val message = ChatMessage(msg, isMe = true, timestamp = timestamp)
                _chatHistories.getOrPut(currentFriendId) { mutableStateListOf() }.add(message)
                android.util.Log.d("WebSocket", "已添加本地消息: $message")
                _input.value = ""

                viewModelScope.launch {
                    val userId = LoginPreferences.getUniqueIdOnce(context)
                    messageDao.insertMessage(
                        MessageEntity(
                            userId = userId,
                            friendId = currentFriendId,
                            content = msg,
                            timestamp = timestamp,
                            isMe = true
                        )
                    )
                }
            } catch (e: Exception) {
                // 可选：记录发送失败
                android.util.Log.e("WebSocket", "发送消息出错: ${e.message}", e)
            }
        }
    }

    override fun onCleared() {
        WebSocketManager.close()
        super.onCleared()
    }
}

data class ChatSession(
    val friendId: String,
    val friendName: String,
    val lastMessage: String,
    val lastTime: String
)

class ChatListViewModel(
    private val messageDao: MessageDao,
    private val context: Context
) : ViewModel() {

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = LoginPreferences.getUniqueIdOnce(context)
            messageDao.getLatestMessages(userId = userId).collect { messages ->
                _chatSessions.value = messages.map {
                    ChatSession(
                        friendId = it.friendId,
                        friendName = it.friendId, // 暂时用 friendId 作为显示名
                        lastMessage = it.content,
                        lastTime = it.timestamp
                    )
                }
            }
        }
    }
}