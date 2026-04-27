package com.vaultx.ui.generator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vaultx.R
import com.vaultx.databinding.FragmentPasswordGeneratorBinding
import com.vaultx.ui.addedit.AddEditPasswordFragment
import com.vaultx.utils.ClipboardSecurity
import com.google.android.material.snackbar.Snackbar

class PasswordGeneratorFragment : Fragment() {

    private var _binding: FragmentPasswordGeneratorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GeneratorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        binding.sliderLength.addOnChangeListener { _, value, _ ->
            viewModel.length = value.toInt()
            binding.tvLengthValue.text = viewModel.length.toString()
            viewModel.generate()
        }

        binding.swUppercase.setOnCheckedChangeListener { _, isChecked ->
            viewModel.useUpper = isChecked
            validateSwitches()
        }
        binding.swLowercase.setOnCheckedChangeListener { _, isChecked ->
            viewModel.useLower = isChecked
            validateSwitches()
        }
        binding.swNumbers.setOnCheckedChangeListener { _, isChecked ->
            viewModel.useNumbers = isChecked
            validateSwitches()
        }
        binding.swSymbols.setOnCheckedChangeListener { _, isChecked ->
            viewModel.useSymbols = isChecked
            validateSwitches()
        }

        binding.btnRegenerate.setOnClickListener { viewModel.generate() }
        
        binding.btnCopy.setOnClickListener {
            val pass = binding.tvGeneratedPassword.text.toString()
            if (pass.isNotEmpty()) {
                ClipboardSecurity.copySensitiveText(requireContext(), "VaultX generated password", pass)
                Snackbar.make(binding.root, R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(resources.getColor(R.color.vx_surface_container_highest, null))
                    .setTextColor(resources.getColor(R.color.vx_cyan, null))
                    .show()
            }
        }

        binding.btnUsePassword.setOnClickListener {
            val generated = binding.tvGeneratedPassword.text.toString()
            if (generated.isNotEmpty()) {
                // Return result to previous fragment
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    AddEditPasswordFragment.KEY_GENERATED_PW, generated
                )
                findNavController().navigateUp()
            }
        }
    }

    private fun validateSwitches() {
        if (!viewModel.useUpper && !viewModel.useLower && !viewModel.useNumbers && !viewModel.useSymbols) {
            // Force at least one to be true
            binding.swLowercase.isChecked = true
            viewModel.useLower = true
        }
        viewModel.generate()
    }

    private fun observeViewModel() {
        viewModel.generatedPassword.observe(viewLifecycleOwner) { pass ->
            binding.tvGeneratedPassword.text = pass
        }
        viewModel.strength.observe(viewLifecycleOwner) { strength ->
            binding.tvStrength.text = "STRENGTH: $strength"
            val colorRes = when (strength) {
                "WEAK" -> R.color.vx_red
                "MEDIUM" -> R.color.vx_purple
                else -> R.color.vx_green
            }
            binding.tvStrength.setTextColor(resources.getColor(colorRes, null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

