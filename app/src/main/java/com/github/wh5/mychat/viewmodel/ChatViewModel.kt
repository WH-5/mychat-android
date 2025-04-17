package com.github.wh5.mychat.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import com.github.wh5.mychat.websocket.WebSocketManager

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input

    init {
        WebSocketManager.connect { incoming ->
            _messages.value += "对方：$incoming"
        }
    }

    fun updateInput(newInput: String) {
        _input.value = newInput
    }

    fun sendMessage() {
        val msg = _input.value
        if (msg.isNotBlank()) {
            WebSocketManager.send(msg)
            _messages.value = _messages.value + "我：$msg"
            _input.value = ""
        }
    }

    override fun onCleared() {
        WebSocketManager.close()
        super.onCleared()
    }
}