package com.vaultx.ui.addedit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultx.di.AppModule
import com.vaultx.data.model.PasswordEntry
import com.vaultx.data.model.Resource
import kotlinx.coroutines.launch

class AddEditViewModel : ViewModel() {

    private val authRepository = AppModule.authRepository
    private val passwordRepository = AppModule.passwordRepository

    private val _saveState = MutableLiveData<Resource<Boolean>>()
    val saveState: LiveData<Resource<Boolean>> = _saveState

    private val _passwordEntry = MutableLiveData<Resource<PasswordEntry>>()
    val passwordEntry: LiveData<Resource<PasswordEntry>> = _passwordEntry

    fun loadPassword(passwordId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        _passwordEntry.value = Resource.Loading
        viewModelScope.launch {
            val result = passwordRepository.getPasswordById(uid, passwordId)
            _passwordEntry.value = result
        }
    }

    fun savePassword(
        passwordId: String?,
        title: String,
        username: String,
        rawPassword: String,
        url: String,
        category: String,
        notes: String
    ) {
        val uid = authRepository.currentUser?.uid ?: return
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            val result = if (passwordId.isNullOrEmpty()) {
                passwordRepository.addPassword(uid, title, username, rawPassword, url, category, notes)
            } else {
                passwordRepository.updatePassword(uid, passwordId, title, username, rawPassword, url, category, notes)
            }
            _saveState.value = result
        }
    }

    fun decryptPassword(encrypted: String): String {
        return passwordRepository.decryptPassword(encrypted)
    }
}


