package com.example.sos.repository

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sos.database.UserDatabase
import com.example.sos.entity.Contacts
import com.example.sos.entity.Message
import com.example.sos.entity.UserDetail
import com.example.sos.models.LocationModel
import com.example.sos.models.UserDetailModel
import com.example.sos.utils.Utility
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener

class MainRepository(private val database: UserDatabase) {

    lateinit var docref: DocumentReference
    var firebaseAuth = FirebaseAuth.getInstance()

    private var isUserSaved = MutableLiveData<Boolean>()

    private var isUserLoggedIn = MutableLiveData<Boolean>()

    private var isPasswordReset = MutableLiveData<Boolean>()

    private var isPasswordUpdated = MutableLiveData<Boolean>()

    private var isUserSignOut = MutableLiveData<Boolean>()

    var resultId = MutableLiveData<List<Long>>()

    private var userDetailModel = MutableLiveData<UserDetailModel>()

    val userDetailModelLiveData: LiveData<UserDetailModel>
        get() = userDetailModel


    private var listOfLatLong = MutableLiveData<MutableList<LocationModel>>()

    val listOfLatLongLiveData: MutableLiveData<MutableList<LocationModel>>
        get() = listOfLatLong



    val userSavedLivedata: LiveData<Boolean>
        get() = isUserSaved

    val isUserLoggedInLiveData: LiveData<Boolean>
        get() = isUserLoggedIn

    val isPswdresetLiveData: LiveData<Boolean>
        get() = isPasswordReset

    val isPswdUpdatedLiveData: LiveData<Boolean>
        get() = isPasswordUpdated

    val isUserSignOutLiveData: LiveData<Boolean>
        get() = isUserSignOut

    /*val resultIdLiveData: LiveData<List<Long>>
        get() = resultId*/

    var listOfContact = MutableLiveData<List<Contacts>>()

    val listOfContactLiveData: LiveData<List<Contacts>>
        get() = listOfContact

    var messageModel = MutableLiveData<Message>()

    val messageModelLiveData: LiveData<Message>
        get() = messageModel

    var isNumberAdded = MutableLiveData<Boolean>()

    val isNumberAddedLiveData : LiveData<Boolean>
    get() = isNumberAdded


    /** Room Method */
    suspend fun insertNumber(number: UserDetail){
        database.userDetailDao().addNumber(number)
        isNumberAdded.postValue(true)
    }

    suspend fun createContact(list: List<Contacts>, existInDatabase: Boolean) {
        //run background thread
        //compare list with existindatabase list
        if (existInDatabase) {
            //some or all contacts are already present
            var listToBeAdded =
                list.filter { it.id == null && !TextUtils.isEmpty(it.name) && !TextUtils.isEmpty(it.contact) }
            var listToBeUpdated = list.filter { it.id != null }
            if (listToBeAdded.size > 0) {
                resultId.postValue(database.contactDao().createContact(listToBeAdded))
            }
            if (listToBeUpdated.size > 0) {
                var numberOfUpdatedRows = database.contactDao().updateContact(listToBeUpdated)
                if (numberOfUpdatedRows > 0) {
                    resultId.postValue(listOf(numberOfUpdatedRows.toLong()))
                }
            }
        } else {
            //values don't exist in room db so add new
            var listToBeAdded = list.filter {
                !TextUtils.isEmpty(it.name) && !TextUtils.isEmpty(it.contact)
            }
            resultId.postValue(database.contactDao().createContact(listToBeAdded))
        }
    }

    suspend fun getContact(email: String, token: String) {
        //run background thread
        var contacts = database.contactDao().getContacts(email, token)
        if (contacts != null && contacts.size > 0) listOfContact.postValue(contacts)
    }

    suspend fun createMessage(messageModel: Message, existInDb: Boolean) {
        //run background thread
        if (existInDb) {
            if (messageModel.id != null) {
                var messageToBeUpdated = messageModel
                var numRows = database.messageDao().updateMessage(messageToBeUpdated)
                if (numRows > 0) {
                    resultId.postValue(listOf(numRows.toLong()))
                }
            }


            if (messageModel.id == null && !TextUtils.isEmpty(messageModel.message)) {
                var messageToBeAdded = messageModel
                resultId.postValue(listOf(database.messageDao().createMessage(messageToBeAdded)))
            }

        }else {
            if ( !TextUtils.isEmpty(messageModel.message)) {
                var messageToBeAdded = messageModel
                resultId.postValue(listOf(database.messageDao().createMessage(messageToBeAdded)))
            }
        }
    }

    suspend fun getMessage(email: String, token: String) {
        //run background thread
        var msgModel = database.messageDao().getMessage(email, token)
        if (msgModel != null) messageModel.postValue(msgModel)
    }


    /** Firebase Method */
    fun signOutUser() {
        firebaseAuth.signOut()
        isUserSignOut.postValue(true)
    }

    fun updatePassword(password: String) {
        val user = firebaseAuth.currentUser
        user!!.updatePassword(password).addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                isPasswordUpdated.postValue(true)
            } else {
                Log.d("TAG", "updatePassword: ${task.exception.toString()}")
                isPasswordUpdated.postValue(false)
            }
        })
    }

    fun createUserInFirebase(user: UserDetailModel, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener(
            OnCompleteListener {
                if (it.isSuccessful) {
                    insertUserDetailInFireStore(user)
                } else {
                    isUserLoggedIn.postValue(false)
                }
            })
    }

    fun loginUserInFirebase(lMail: String, lPswd: String) {
        firebaseAuth.signInWithEmailAndPassword(lMail, lPswd).addOnCompleteListener(
            OnCompleteListener { task ->
                if (task.isSuccessful) {
                    isUserLoggedIn.postValue(true)
                } else {
                    isUserLoggedIn.postValue(false)
                }
            })
    }

    fun resetPassword(tMail: String) {
        firebaseAuth.sendPasswordResetEmail(tMail)
            .addOnCompleteListener(OnCompleteListener { task ->
                if (task.isSuccessful) {
                    isPasswordReset.postValue(true)
                } else {
                    isPasswordReset.postValue(false)
                }
            })
    }

    fun insertUserDetailInFireStore(
        userDetailModel: UserDetailModel,
    ) {
        docref = Utility.getCollectionReference().document()
        docref.set(userDetailModel).addOnCompleteListener(OnCompleteListener {
            Log.d("TAG", "insertUserDetailInFireStore: Added")
            isUserSaved.postValue(true)
        }).addOnFailureListener(OnFailureListener {
            Log.d("TAG", "insertUserDetailInFireStore: Failed")
            isUserSaved.postValue(false)
        })


        //
        //2.00pm - timestamp = 123456789, 6.00pm - timestamp = 234567891

        //document = HashMap()
        //document.add("timestamp", tiemstamp)
        /*var timestamp = 123456789
        var timestamp2 = 234567891
        Utility.getCollectionReference().whereGreaterThan("_id", timestamp)
            .whereLessThan("_id", timestamp2).get().addOnSuccessListener {
                if (it!=null) {

                }
            }*/
    }

    fun getUserDetailFromFirestore() {
        Utility.getCollectionReference()
            .addSnapshotListener(EventListener { value, error ->
                if (value != null || error == null) {
                    for (documentChange in value!!.documentChanges) {
                        val user = UserDetailModel(
                            documentChange.document.data["fname"].toString(),
                            documentChange.document.data["lname"].toString(),
                            documentChange.document.data["email"].toString(),
                            documentChange.document.data["username"].toString(),
                            documentChange.document.data["timestamp"] as Timestamp
                        )
                        userDetailModel.postValue(user)
                    }
                }
            })
    }
//.whereGreaterThan("timestamp", timestamp1)
//            .whereLessThan("timestamp", timestamp2)
    fun fetchLatLongFromFirebase(timestamp1 : Long, timestamp2: Long){
        Utility.getLocationCollectionReference().whereGreaterThan("timestamp", timestamp1)
            .whereLessThan("timestamp", timestamp2)
            .addSnapshotListener(EventListener { value, error ->
                if (value != null || error == null) {
                    val list  = mutableListOf<LocationModel>()
                    Log.d("TAG", "fetchLatLongFromFirebase: "+value?.documentChanges?.size)
                    for (documentChange in value!!.documentChanges) {
                        Log.d("TAG", "fetchLatLongFromFirebase2222: "+documentChange.document.data)
                        val locationModel = LocationModel(
                            documentChange.document.data["latitude"].toString(),
                            documentChange.document.data["longitude"].toString(),
                            documentChange.document.data["timestamp"] as Long
                        )
                        list.add(locationModel)
                    }
                    listOfLatLong.postValue(list)
                }
            })
    }
}