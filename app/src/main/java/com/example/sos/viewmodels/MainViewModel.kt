package com.example.sos.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.sos.MyApplication.Companion.database
import com.example.sos.entity.Contacts
import com.example.sos.entity.Message
import com.example.sos.entity.UserDetail
import com.example.sos.models.LocationModel
import com.example.sos.models.UserDetailModel
import com.example.sos.repository.MainRepository
import com.example.sos.utils.SharedPrefManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(val repo: MainRepository, val sharedPrefManager: SharedPrefManager) :
    ViewModel() {

    val isSmallPassword: MutableLiveData<Boolean> = MutableLiveData(false)
    lateinit var token: String

    val fName = MutableLiveData<String>()
    val lName = MutableLiveData<String>()
    val username = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val timestamp = MutableLiveData<String>()

    val registerEnabled: MediatorLiveData<Boolean> = MediatorLiveData()

    /** Dialog Box Variables */

    //contact variable
    val contactListNew = MutableLiveData<List<Contacts>>()
    val messageNew = MutableLiveData<Message>()

    /** Dialog Box Variables */

    val updatePassword = MutableLiveData<String>()

    val userDetailModelLiveData: LiveData<UserDetailModel>
        get() = repo.userDetailModelLiveData

    val listOfLatLongLiveData: MutableLiveData<MutableList<LocationModel>>
        get() = repo.listOfLatLongLiveData

    val userSavedLivedata: LiveData<Boolean>
        get() = repo.userSavedLivedata

    val isUserloggedInLive: LiveData<Boolean>
        get() = repo.isUserLoggedInLiveData

    val isPswdResetLiveData: LiveData<Boolean>
        get() = repo.isPswdresetLiveData

    val isPswdUpdatedLiveData: LiveData<Boolean>
        get() = repo.isPswdUpdatedLiveData

    val isUserSignOutLiveData: LiveData<Boolean>
        get() = repo.isUserSignOutLiveData

    val resultIdLiveData: MutableLiveData<List<Long>>
        get() = repo.resultId

    val listOfContact: LiveData<List<Contacts>>
        get() = repo.listOfContactLiveData

    val msgModel: LiveData<Message>
        get() = repo.messageModelLiveData

    val isNumberAddedLiveData : LiveData<Boolean>
    get() = repo.isNumberAddedLiveData

    var existInDatabase = false
    var existInDb = false

    lateinit var contact3: Contacts
    lateinit var contact2: Contacts
    lateinit var contact: Contacts

    lateinit var message: Message

    init {
        addDummyContacts()
        addDummyMessage()

        registerEnabled.value = false
        registerEnabled.addSource(fName) {
            registerEnabled.value = isRegisterButtonEnabled()
        }
        registerEnabled.addSource(lName) {
            registerEnabled.value = isRegisterButtonEnabled()
        }
        registerEnabled.addSource(username) {
            registerEnabled.value = isRegisterButtonEnabled()
        }
        registerEnabled.addSource(email) {
            registerEnabled.value = isRegisterButtonEnabled()
        }
        registerEnabled.addSource(password) {
            registerEnabled.value = isRegisterButtonEnabled()
        }
    }

    private fun isRegisterButtonEnabled(): Boolean {
        return fName.value.orEmpty().isNotEmpty() && lName.value.orEmpty()
            .isNotEmpty() && username.value.orEmpty()
            .isNotEmpty() && email.value.orEmpty()
            .isNotEmpty() && password.value.orEmpty()
            .isNotEmpty()
    }

    fun addDummyMessage() {
        message = Message(null, sharedPrefManager.getUserEmail(), "", "")
        messageNew.postValue(message)
    }

    fun addDummyContacts() {
        contact = Contacts(null, sharedPrefManager.getUserEmail(), "", "", "")
        contact2 = Contacts(null, sharedPrefManager.getUserEmail(), "", "", "")
        contact3 = Contacts(null, sharedPrefManager.getUserEmail(), "", "", "")
        val eList = listOf(contact, contact2, contact3)
        contactListNew.postValue(eList)
    }

    /** Room Method */
    fun createSmsContact() {
        viewModelScope.launch {
            contactListNew.value?.let {
                repo.createContact(it, existInDatabase)
            }
        }
    }


    fun getContacts(email: String, token: String) {
        viewModelScope.launch {
            repo.getContact(email, token)
        }
    }

    fun createMessage() {
        Log.d("TAG", "createMessage: Message function working ${messageNew}")
        viewModelScope.launch {
            messageNew.value.let {
                if (it != null) {
                    repo.createMessage(it, existInDb)
                }
            }
        }
    }


    fun getMessage(email: String, token: String) {
        viewModelScope.launch {
            repo.getMessage(email, token)
        }
    }

    fun insertNumber(number: UserDetail){
        viewModelScope.launch(Dispatchers.IO) {
            repo.insertNumber(number)
        }
    }

    fun createUserInFirebase() {
        val model = UserDetailModel(
            fName.value.toString(),
            lName.value.toString(),
            email.value.toString(),
            username.value.toString(),
            Timestamp.now()
        )
        val pswd = password.value.toString()
        repo.createUserInFirebase(model, pswd)
    }


    fun updatePassword() {
        val pswd = updatePassword.value.toString()
        repo.updatePassword(pswd)
    }


    fun getUserDataFromFirebase() {

        repo.getUserDetailFromFirestore()
    }

    fun fetchLatLongFromFirebase(timestamp1: Long, timestamp2: Long) {
        repo.fetchLatLongFromFirebase(timestamp1, timestamp2)
    }

    fun loginUserInFirebase() {
        val lMail = email.value.toString()
        val lPswd = password.value.toString()
        repo.loginUserInFirebase(lMail, lPswd)
    }

    fun signOutUser() {
        repo.signOutUser()
    }

    fun resetPassword() {
        val tMail = email.value.toString()
        repo.resetPassword(tMail)
    }

    fun checkPasswordLength(password: String) {
        if (password.length < 6 || password.length > 6) {
            isSmallPassword.postValue(true)
        } else {
            isSmallPassword.postValue(false)
        }
    }

}