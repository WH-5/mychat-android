package com.github.wh5.mychat.viewmodel

import android.content.Context
import com.github.wh5.mychat.viewmodel.ChatViewModel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.wh5.mychat.data.local.MessageDao

class ChatViewModelFactory(
    private val friendId: String,
    private val messageDao: MessageDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(context,  friendId,messageDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}