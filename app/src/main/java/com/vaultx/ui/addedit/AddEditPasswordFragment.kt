package com.vaultx.ui.addedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vaultx.R
import com.vaultx.databinding.FragmentAddEditPasswordBinding
import com.vaultx.data.model.Category
import com.vaultx.data.model.Resource
import com.google.android.material.snackbar.Snackbar

class AddEditPasswordFragment : Fragment() {

    private var _binding: FragmentAddEditPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddEditViewModel by viewModels()

    private var passwordId: String? = null

    // For receiving generated password from Generator
    companion object {
        const val KEY_GENERATED_PW = "generated_password"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passwordId = arguments?.getString("passwordId")
        val isEditMode = !passwordId.isNullOrEmpty()

        binding.tvHeaderTitle.text = if (isEditMode) "EDIT PASSWORD" else "ADD PASSWORD"

        setupDropdown()
        setupListeners()
        observeViewModel()

        if (isEditMode) {
            viewModel.loadPassword(passwordId!!)
        }

        // Check for generated password
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(KEY_GENERATED_PW)
            ?.observe(viewLifecycleOwner) { generated ->
                binding.etPassword.setText(generated)
                // Consume
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>(KEY_GENERATED_PW)
            }
    }

    private fun setupDropdown() {
        val categories = Category.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        binding.actCategory.setAdapter(adapter)
        binding.actCategory.setText(Category.OTHERS.displayName, false)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        binding.btnGenerate.setOnClickListener {
            findNavController().navigate(R.id.action_addEdit_to_generator)
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString()
            val url = binding.etUrl.text.toString().trim()
            val category = Category.fromString(binding.actCategory.text.toString()).name
            val notes = binding.etNotes.text.toString().trim()

            if (title.isEmpty()) {
                binding.tilTitle.error = "Title required"
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                binding.tilPassword.error = "Password required"
                return@setOnClickListener
            }

            viewModel.savePassword(passwordId, title, username, pass, url, category, notes)
        }
    }

    private fun observeViewModel() {
        viewModel.passwordEntry.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val entry = resource.data
                binding.etTitle.setText(entry.title)
                binding.etUsername.setText(entry.username)
                binding.etPassword.setText(viewModel.decryptPassword(entry.encryptedPassword))
                binding.etUrl.setText(entry.url)
                binding.actCategory.setText(Category.fromString(entry.category).displayName, false)
                binding.etNotes.setText(entry.notes)
            }
        }

        viewModel.saveState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



