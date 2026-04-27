package com.vaultx.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vaultx.R
import com.vaultx.di.AppModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash screen shown on app launch.
 * Displays app branding for 2 seconds, then navigates based on auth state.
 */
class SplashFragment : Fragment() {
    companion object {
        private const val SPLASH_DELAY_MS = 600L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)
            if (!isAdded) return@launch

            val navController = findNavController()
            if (navController.currentDestination?.id != R.id.splashFragment) {
                return@launch
            }

            if (!AppModule.authRepository.isUserLoggedIn()) {
                navController.navigate(R.id.action_splash_to_auth)
            } else if (AppModule.preferencesManager.isBiometricEnabled()) {
                AppModule.biometricHelper.showBiometricPrompt(
                    activity = requireActivity(),
                    onSuccess = {
                        if (isAdded && navController.currentDestination?.id == R.id.splashFragment) {
                            navController.navigate(R.id.action_splash_to_dashboard)
                        }
                    },
                    onError = {
                        activity?.finish()
                    }
                )
            } else {
                navController.navigate(R.id.action_splash_to_dashboard)
            }
        }
    }
}

