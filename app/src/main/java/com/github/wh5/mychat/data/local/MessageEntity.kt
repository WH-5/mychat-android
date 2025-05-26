package com.github.wh5.mychat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val friendId: String,
    val content: String,
    val timestamp: String,
    val isMe: Boolean
)