package com.vaultx.ui.settings

import android.content.Intent
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        updateAutoLockStatus()

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

        binding.btnAutoLock.setOnClickListener {
            showAutoLockDialog()
        }

        binding.btnExport.setOnClickListener {
            lifecycleScope.launch {
                val data = viewModel.exportData()
                if (data != null) {
                    shareExportedData(data)
                } else {
                    Toast.makeText(requireContext(), "Failed to export data", Toast.LENGTH_SHORT).show()
                }
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

    private fun updateAutoLockStatus() {
        val minutes = viewModel.getAutoLockTimeout()
        binding.tvAutoLockStatus.text = when (minutes) {
            -1 -> "Never"
            0 -> "Immediate"
            1 -> "1 Minute"
            else -> "$minutes Minutes"
        }
    }

    private fun showAutoLockDialog() {
        val options = arrayOf("Immediate", "1 Minute", "5 Minutes", "10 Minutes", "Never")
        val values = intArrayOf(0, 1, 5, 10, -1)
        var checkedItem = values.indexOf(viewModel.getAutoLockTimeout())
        if (checkedItem == -1) checkedItem = 2 // Default to 5 min

        MaterialAlertDialogBuilder(requireContext(), R.style.VaultX_AlertDialog)
            .setTitle("Auto-Lock Timeout")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                viewModel.setAutoLockTimeout(values[which])
                updateAutoLockStatus()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun shareExportedData(data: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, data)
            putExtra(Intent.EXTRA_SUBJECT, "VaultX Backup")
        }
        startActivity(Intent.createChooser(intent, "Save VaultX Backup"))
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

