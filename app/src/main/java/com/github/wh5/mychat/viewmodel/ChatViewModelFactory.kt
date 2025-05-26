package com.github.wh5.mychat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.wh5.mychat.data.local.MessageDao

class ChatViewModelFactory(
    private val friendId: String,
    private val messageDao: MessageDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(friendId, messageDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}