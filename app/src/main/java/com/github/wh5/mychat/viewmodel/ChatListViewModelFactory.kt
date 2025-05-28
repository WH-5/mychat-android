package com.github.wh5.mychat.viewmodel

import android.content.Context
import com.github.wh5.mychat.viewmodel.ChatListViewModel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.wh5.mychat.data.local.MessageDao

class ChatListViewModelFactory(
    private val messageDao: MessageDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatListViewModel(messageDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}