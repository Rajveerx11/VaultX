package com.vaultx

import android.app.Application
import com.vaultx.di.AppModule

/**
 * Custom Application class that initializes the DI module.
 * Registered in AndroidManifest.xml via android:name=".VaultXApplication"
 */
class VaultXApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.initialize(this)
    }
}

