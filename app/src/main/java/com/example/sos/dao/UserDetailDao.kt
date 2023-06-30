package com.example.sos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.sos.entity.UserDetail

@Dao
interface UserDetailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNumber(number : UserDetail)
}