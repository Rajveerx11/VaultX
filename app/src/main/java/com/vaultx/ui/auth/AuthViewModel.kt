package com.vaultx.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaultx.data.model.Resource
import com.vaultx.data.model.User
import com.vaultx.di.AppModule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * ViewModel for the Auth screen (Login + Register).
 */
class AuthViewModel : ViewModel() {
    companion object {
        private const val TAG = "VaultXGoogleAuth"
    }

    private val authRepository = AppModule.authRepository
    private val preferencesManager = AppModule.preferencesManager

    private val _authResult = MutableLiveData<Resource<User>>()
    val authResult: LiveData<Resource<User>> = _authResult

    /**
     * Email/password login.
     */
    fun login(email: String, password: String) {
        _authResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.loginUser(email, password)
                if (result is Resource.Success) {
                    preferencesManager.saveUserLoggedIn(true)
                    result.data.let { user ->
                        preferencesManager.saveUserProfile(
                            user.name,
                            user.email
                        )
                    }
                }
                _authResult.value = result
            } catch (e: CancellationException) {
                _authResult.value = Resource.Error(e.message ?: "Login timed out. Please try again.")
            }
        }
    }

    /**
     * New user registration.
     */
    fun register(name: String, email: String, password: String) {
        _authResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.registerUser(name, email, password)
                if (result is Resource.Success) {
                    preferencesManager.saveUserLoggedIn(true)
                    preferencesManager.saveUserProfile(name, email)
                }
                _authResult.value = result
            } catch (e: CancellationException) {
                _authResult.value = Resource.Error(e.message ?: "Registration timed out. Please try again.")
            }
        }
    }

    /**
     * Google Sign-In with ID token from Google Sign-In flow.
     */
    fun signInWithGoogle(idToken: String) {
        _authResult.value = Resource.Loading
        viewModelScope.launch {
            try {
                Log.d(TAG, "ViewModel signInWithGoogle started")
                val result = authRepository.loginWithGoogle(idToken)
                Log.d(TAG, "ViewModel signInWithGoogle result=${result::class.java.simpleName}")
                if (result is Resource.Success) {
                    preferencesManager.saveUserLoggedIn(true)
                    result.data.let { user ->
                        preferencesManager.saveUserProfile(
                            user.name,
                            user.email
                        )
                    }
                }
                _authResult.value = result
            } catch (e: CancellationException) {
                Log.e(TAG, "ViewModel signInWithGoogle cancelled", e)
                _authResult.value = Resource.Error(
                    e.message ?: "Google sign-in timed out. Please check your connection and try again."
                )
            }
        }
    }
}


