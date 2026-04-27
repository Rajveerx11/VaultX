package com.vaultx.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vaultx.R
import com.vaultx.databinding.FragmentDashboardBinding
import com.vaultx.data.model.PasswordEntry
import com.vaultx.data.model.Resource
import com.vaultx.utils.ClipboardSecurity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Dashboard displaying all password entries in a RecyclerView.
 * Features: real-time updates, search filtering, FAB to add, copy to clipboard.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: PasswordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWelcome()
        setupRecyclerView()
        setupSearch()
        setupFAB()
        setupLogout()
        observePasswords()

        viewModel.loadPasswords()
    }

    private fun setupWelcome() {
        val name = viewModel.getUserName().uppercase().ifEmpty { "USER" }
        binding.tvWelcome.text = getString(R.string.welcome_back, name)
    }

    private fun setupRecyclerView() {
        adapter = PasswordAdapter(
            onItemClick = { entry -> navigateToDetail(entry) },
            onCopyClick = { entry -> copyPasswordToClipboard(entry) }
        )
        binding.rvPasswords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPasswords.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.searchPasswords(text.toString())
        }
    }

    private fun setupFAB() {
        binding.fabAdd.setOnClickListener {
            val bundle = Bundle().apply { putString("passwordId", "") }
            findNavController().navigate(R.id.action_dashboard_to_addEdit, bundle)
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            viewLifecycleOwner.lifecycleScope.launch {
                clearCredentialState()
                if (isAdded) {
                    findNavController().navigate(R.id.action_dashboard_to_auth)
                }
            }
        }
    }

    private fun observePasswords() {
        viewModel.passwords.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPasswords.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (resource.data.isEmpty()) {
                        binding.rvPasswords.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvPasswords.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        adapter.submitList(resource.data)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToDetail(entry: PasswordEntry) {
        val bundle = Bundle().apply { putString("passwordId", entry.id) }
        findNavController().navigate(R.id.action_dashboard_to_detail, bundle)
    }

    private fun copyPasswordToClipboard(entry: PasswordEntry) {
        val decrypted = viewModel.decryptPassword(entry.encryptedPassword)
        ClipboardSecurity.copySensitiveText(requireContext(), "VaultX password", decrypted)
        Snackbar.make(binding.root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.vx_surface_container_highest, null))
            .setTextColor(resources.getColor(R.color.vx_cyan, null))
            .show()
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


