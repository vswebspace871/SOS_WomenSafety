package com.example.sos.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sos.repository.MainRepository
import com.example.sos.utils.SharedPrefManager
import com.example.sos.viewmodels.MainViewModel

class MainViewModelFactory(val repo : MainRepository, val sharedPrefManager: SharedPrefManager) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repo, sharedPrefManager) as T
    }

}