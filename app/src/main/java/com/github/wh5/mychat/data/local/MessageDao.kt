package com.github.wh5.mychat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.github.wh5.mychat.data.local.MessageEntity
@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE friendId = :friendId ORDER BY timestamp ASC")
    fun getMessagesForFriend(friendId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    @Query("""
        SELECT * FROM messages
        WHERE id IN (
            SELECT MAX(id) FROM messages GROUP BY friendId
        )
        ORDER BY timestamp DESC
    """)
    fun getLatestMessages(): Flow<List<MessageEntity>>
}