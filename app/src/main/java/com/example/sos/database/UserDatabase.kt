package com.example.sos.database

import android.content.Context
import androidx.room.*
import com.example.sos.dao.ContactDao
import com.example.sos.dao.MessageDao
import com.example.sos.dao.UserDetailDao
import com.example.sos.entity.Contacts
import com.example.sos.entity.Message
import com.example.sos.entity.UserDetail

@Database(
    entities = [
        Contacts::class,
        Message::class,
        UserDetail::class
    ], version = 1
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun contactDao() : ContactDao
    abstract fun messageDao(): MessageDao
    abstract fun userDetailDao(): UserDetailDao

    //database singleton pattern
    companion object {
        @Volatile  //for update realtime
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {

            synchronized(this) {

                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, UserDatabase::class.java, "UserDB")
                        .fallbackToDestructiveMigration()
                        .build()
                    //instance of DB creation
                }
            }
            return INSTANCE!!
        }
    }

}