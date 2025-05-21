package com.example.finalterm.data.dao

import androidx.room.*
import com.example.finalterm.data.entity.ChatEntity


@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long

    @Query("SELECT * FROM chat_history ORDER BY id ASC")
    suspend fun getAllChats(): List<ChatEntity>

    @Query("SELECT * FROM chat_history WHERE id = :chatId")
    suspend fun getChatById(chatId: Int): ChatEntity

    @Query("UPDATE chat_history SET chatContent = :chatContent WHERE id = :chatId")
    suspend fun updateChatContent(chatId: Int, chatContent: String)

    @Delete
    suspend fun deleteChat(chat: ChatEntity)
}
