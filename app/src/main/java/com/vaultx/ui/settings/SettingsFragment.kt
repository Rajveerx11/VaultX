package com.vaultx.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vaultx.R
import com.vaultx.databinding.FragmentSettingsBinding
import com.vaultx.di.AppModule
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvUserName.text = viewModel.getUserName()
        binding.tvUserEmail.text = viewModel.getUserEmail()

        binding.swBiometric.isChecked = viewModel.isBiometricEnabled()

        binding.swBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Verify hardware capability
                if (AppModule.biometricHelper.isBiometricAvailable(requireActivity())) {
                    viewModel.setBiometricEnabled(true)
                } else {
                    binding.swBiometric.isChecked = false
                    Toast.makeText(requireContext(), "Biometric hardware unavailable", Toast.LENGTH_SHORT).show()
                }
            } else {
                viewModel.setBiometricEnabled(false)
            }
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            viewLifecycleOwner.lifecycleScope.launch {
                clearCredentialState()
                if (isAdded) {
                    findNavController().navigate(R.id.action_settings_to_auth)
                }
            }
        }
    }

    private suspend fun clearCredentialState() {
        try {
            CredentialManager.create(requireContext())
                .clearCredentialState(ClearCredentialStateRequest())
        } catch (_: ClearCredentialException) {
            // Firebase sign-out already succeeded; ignore provider cleanup failures.
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

