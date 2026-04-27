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
        // Show biometric prompt if enabled and user is logged in
        if (isResuming && AppModule.preferencesManager.isBiometricEnabled()
            && AppModule.authRepository.isUserLoggedIn()
        ) {
            AppModule.biometricHelper.showBiometricPrompt(
                activity = this,
                onSuccess = { /* Access granted — continue normally */ },
                onError = { /* User cancelled or failed — finish the activity */ 
                    finish()
                }
            )
        }
        isResuming = true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp() || super.onSupportNavigateUp()
    }
}
