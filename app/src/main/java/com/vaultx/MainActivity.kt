package com.vaultx

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.vaultx.di.AppModule

/**
 * Single-activity architecture. Hosts the NavHostFragment that manages
 * all screens. Handles biometric prompt on app resume if enabled.
 */
class MainActivity : AppCompatActivity() {

    private var isResuming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Handle system bar insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        
        val prefs = AppModule.preferencesManager
        val auth = AppModule.authRepository
        
        if (isResuming && auth.isUserLoggedIn()) {
            val lastActive = prefs.getLastActiveTime()
            val timeoutMinutes = prefs.getAutoLockTimeout()
            val currentTime = System.currentTimeMillis()
            
            val isTimeout = when (timeoutMinutes) {
                -1 -> false // Never
                0 -> true  // Immediate
                else -> (currentTime - lastActive) > (timeoutMinutes * 60 * 1000)
            }

            if (isTimeout) {
                if (prefs.isBiometricEnabled()) {
                    AppModule.biometricHelper.showBiometricPrompt(
                        activity = this,
                        onSuccess = { /* Access granted */ },
                        onError = { 
                            // If biometric fails/cancelled, we could either finish the app
                            // or navigate to Auth screen. For security, finish is safer.
                            finish()
                        }
                    )
                } else {
                    // If biometric is disabled but timeout occurred, 
                    // we could force a logout or just let them in for now.
                    // A better way would be a PIN, but for this PR we'll focus on Biometrics.
                }
            }
        }
        isResuming = true
    }

    override fun onPause() {
        super.onPause()
        AppModule.preferencesManager.saveLastActiveTime(System.currentTimeMillis())
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
