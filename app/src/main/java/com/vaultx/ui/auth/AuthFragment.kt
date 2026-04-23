package com.vaultx.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.tabs.TabLayout
import com.vaultx.R
import com.vaultx.data.model.Resource
import com.vaultx.databinding.FragmentAuthBinding

/**
 * Authentication screen with Login/Register tabs and Google Sign-In.
 * Handles form validation and navigation on success.
 */
class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    private var googleSignInConfigured = false
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            binding.progressBar.visibility = View.GONE
            showError(getString(R.string.google_signin_cancelled))
            return@registerForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken.isNullOrBlank()) {
                binding.progressBar.visibility = View.GONE
                showError(getString(R.string.google_signin_failed))
                return@registerForActivityResult
            }
            viewModel.signInWithGoogle(idToken)
        } catch (e: ApiException) {
            binding.progressBar.visibility = View.GONE
            showError(
                getString(
                    R.string.google_signin_failed_message,
                    e.localizedMessage ?: getString(R.string.error_generic)
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleSignIn()
        setupTabs()
        setupButtons()
        observeAuthResult()
    }

    private fun setupGoogleSignIn() {
        val webClientId = resolveWebClientId()
        googleSignInConfigured = webClientId != null

        if (googleSignInConfigured) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId!!)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        }

        binding.btnGoogleSignIn.isEnabled = googleSignInConfigured
        binding.btnGoogleSignIn.isClickable = googleSignInConfigured
        binding.btnGoogleSignIn.isFocusable = googleSignInConfigured
        binding.btnGoogleSignIn.alpha = if (googleSignInConfigured) 1f else 0.6f
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_login))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_register))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.loginForm.visibility = View.VISIBLE
                        binding.registerForm.visibility = View.GONE
                    }

                    1 -> {
                        binding.loginForm.visibility = View.GONE
                        binding.registerForm.visibility = View.VISIBLE
                    }
                }
                binding.tvError.visibility = View.GONE
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })
    }

    private fun setupButtons() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString().trim()
            val password = binding.etLoginPassword.text.toString().trim()

            if (validateLogin(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etRegName.text.toString().trim()
            val email = binding.etRegEmail.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()
            val confirmPassword = binding.etRegConfirmPassword.text.toString().trim()

            if (validateRegister(name, email, password, confirmPassword)) {
                viewModel.register(name, email, password)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            if (!googleSignInConfigured) {
                showError(getString(R.string.google_signin_setup_incomplete))
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.tvError.visibility = View.GONE
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    }

    private fun resolveWebClientId(): String? {
        val resourceId = resources.getIdentifier(
            "default_web_client_id",
            "string",
            requireContext().packageName
        )
        if (resourceId == 0) {
            return null
        }

        return getString(resourceId).trim().takeIf { it.isNotEmpty() }
    }

    private fun validateLogin(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilLoginEmail.error = "Email required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilLoginEmail.error = "Invalid email format"
            return false
        }
        if (password.isEmpty()) {
            binding.tilLoginPassword.error = "Password required"
            return false
        }
        binding.tilLoginEmail.error = null
        binding.tilLoginPassword.error = null
        return true
    }

    private fun validateRegister(
        name: String, email: String, password: String, confirmPassword: String
    ): Boolean {
        if (name.isEmpty()) {
            binding.tilRegName.error = "Name required"
            return false
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilRegEmail.error = "Valid email required"
            return false
        }
        if (password.length < 6) {
            binding.tilRegPassword.error = "At least 6 characters"
            return false
        }
        if (password != confirmPassword) {
            binding.tilRegConfirmPassword.error = "Passwords do not match"
            return false
        }
        binding.tilRegName.error = null
        binding.tilRegEmail.error = null
        binding.tilRegPassword.error = null
        binding.tilRegConfirmPassword.error = null
        return true
    }

    private fun observeAuthResult() {
        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    findNavController().navigate(R.id.action_auth_to_dashboard)
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showError(result.message)
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
