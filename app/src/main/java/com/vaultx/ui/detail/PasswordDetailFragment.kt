package com.vaultx.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vaultx.R
import com.vaultx.databinding.FragmentPasswordDetailBinding
import com.vaultx.data.model.Category
import com.vaultx.data.model.Resource
import com.vaultx.utils.ClipboardSecurity
import com.google.android.material.snackbar.Snackbar

class PasswordDetailFragment : Fragment() {

    private var _binding: FragmentPasswordDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()

    private lateinit var passwordId: String
    private var isPasswordVisible = false
    private var decryptedPassword = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passwordId = arguments?.getString("passwordId") ?: return

        setupListeners()
        observeViewModel()

        viewModel.loadPassword(passwordId)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        binding.fabEdit.setOnClickListener {
            val bundle = Bundle().apply { putString("passwordId", passwordId) }
            findNavController().navigate(R.id.action_detail_to_addEdit, bundle)
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deletePassword(passwordId)
        }

        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            updatePasswordVisibility()
        }

        binding.btnCopyPassword.setOnClickListener {
            copyToClipboard("Password", decryptedPassword)
        }

        binding.btnCopyUsername.setOnClickListener {
            copyToClipboard("Username", binding.tvUsername.text.toString())
        }
    }

    private fun observeViewModel() {
        viewModel.passwordEntry.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    bindData(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                    findNavController().navigateUp() // exit if not found
                }
            }
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                findNavController().navigateUp()
            } else if (resource is Resource.Error) {
                Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun bindData(entry: com.vaultx.data.model.PasswordEntry) {
        val category = Category.fromString(entry.category)
        binding.ivCategoryIcon.setImageResource(category.icon)
        binding.tvTitle.text = entry.title
        binding.tvCategory.text = category.displayName.uppercase()
        binding.tvUsername.text = entry.username

        decryptedPassword = viewModel.decryptPassword(entry.encryptedPassword)
        updatePasswordVisibility()

        if (entry.url.isNotEmpty()) {
            binding.layoutUrl.visibility = View.VISIBLE
            binding.tvUrl.text = entry.url
            binding.btnOpenUrl.setOnClickListener {
                var validUrl = entry.url
                if (!validUrl.startsWith("http://") && !validUrl.startsWith("https://")) {
                    validUrl = "https://$validUrl"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
                startActivity(intent)
            }
        } else {
            binding.layoutUrl.visibility = View.GONE
        }

        if (entry.notes.isNotEmpty()) {
            binding.layoutNotes.visibility = View.VISIBLE
            binding.tvNotes.text = entry.notes
        } else {
            binding.layoutNotes.visibility = View.GONE
        }
    }

    private fun updatePasswordVisibility() {
        if (isPasswordVisible) {
            binding.tvPassword.text = decryptedPassword
            binding.btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
        } else {
            binding.tvPassword.text = "••••••••••"
            binding.btnTogglePassword.setImageResource(R.drawable.ic_eye)
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        ClipboardSecurity.copySensitiveText(requireContext(), "VaultX $label", text)
        Snackbar.make(binding.root, "$label copied", Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.vx_surface_container_highest, null))
            .setTextColor(resources.getColor(R.color.vx_cyan, null))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decryptedPassword = "" // Clear mem
        _binding = null
    }
}



