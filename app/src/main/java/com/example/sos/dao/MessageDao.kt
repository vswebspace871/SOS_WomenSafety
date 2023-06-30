package com.example.sos.dao

import androidx.room.*
import com.example.sos.entity.Contacts
import com.example.sos.entity.Message

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createMessage(messageModel: Message): Long

    @Update
    suspend fun updateMessage(messageModel: Message): Int

    @Query("SELECT * FROM message WHERE user_email=:email " +
            "AND token=:token Order By id DESC LIMIT 1")
    suspend fun getMessage(email : String, token : String) : Message

}