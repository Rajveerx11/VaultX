package com.vaultx.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultx.di.AppModule
import com.vaultx.data.model.PasswordEntry
import com.vaultx.data.model.Resource
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val authRepository = AppModule.authRepository
    private val passwordRepository = AppModule.passwordRepository

    private val _passwordEntry = MutableLiveData<Resource<PasswordEntry>>()
    val passwordEntry: LiveData<Resource<PasswordEntry>> = _passwordEntry

    private val _deleteState = MutableLiveData<Resource<Boolean>>()
    val deleteState: LiveData<Resource<Boolean>> = _deleteState

    fun loadPassword(passwordId: String) {
        val uid = AppModule.firebaseAuth.currentUser?.uid ?: return
        _passwordEntry.value = Resource.Loading
        viewModelScope.launch {
            val result = passwordRepository.getPasswordEntryById(uid, passwordId)
            _passwordEntry.value = result
        }
    }

    fun deletePassword(passwordId: String) {
        val uid = AppModule.firebaseAuth.currentUser?.uid ?: return
        _deleteState.value = Resource.Loading
        viewModelScope.launch {
            val result = passwordRepository.deletePasswordEntry(uid, passwordId)
            _deleteState.value = result
        }
    }

    fun decryptPassword(encrypted: String): String {
        return try { AppModule.cryptoManager.decrypt(encrypted) } catch (e: Exception) { "" }
    }
}

