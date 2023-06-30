package com.example.sos.dao

import androidx.room.*
import com.example.sos.entity.Contacts
import com.google.firebase.firestore.auth.User


@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createContact(list : List<Contacts>): List<Long>

    @Update
    suspend fun updateContact(list : List<Contacts>): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createContactNew(contact : Contacts): Long

    @Query("SELECT * FROM contacts WHERE user_email=:email " +
            "AND token=:token")
    suspend fun getContacts(email : String, token : String) : List<Contacts>

    @Query("SELECT * FROM contacts WHERE user_email=:email " +
            "AND token=:token")
    fun gettContacts(email : String, token : String) : List<Contacts>

    @Query("SELECT * FROM contacts WHERE user_email=:email " +
            "AND token=:token")
    suspend fun checkContactAlreadyExist(email : String, token : String) : List<Contacts>

}