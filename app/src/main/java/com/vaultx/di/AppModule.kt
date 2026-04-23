package com.vaultx.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vaultx.data.repository.AuthRepository
import com.vaultx.data.repository.PasswordRepository
import com.vaultx.utils.BiometricHelper
import com.vaultx.utils.CryptoManager
import com.vaultx.utils.PreferencesManager

/**
 * Manual dependency injection module for VaultX.
 * Provides singleton instances of all backend services.
 * Initialized by [com.vaultx.VaultXApplication] on app start.
 */
object AppModule {
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var cryptoManager: CryptoManager
    lateinit var preferencesManager: PreferencesManager
    lateinit var biometricHelper: BiometricHelper
    lateinit var authRepository: AuthRepository
    lateinit var passwordRepository: PasswordRepository

    fun initialize(context: Context) {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        cryptoManager = CryptoManager()
        preferencesManager = PreferencesManager(context.applicationContext)
        biometricHelper = BiometricHelper(preferencesManager)
        authRepository = AuthRepository(firebaseAuth)
        passwordRepository = PasswordRepository(firebaseFirestore, cryptoManager)
    }
}
