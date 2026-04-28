package com.vaultx.ui.audit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vaultx.R
import com.vaultx.databinding.FragmentAuditBinding
import com.vaultx.data.model.Resource
import com.google.android.material.snackbar.Snackbar

class AuditFragment : Fragment() {

    private var _binding: FragmentAuditBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuditViewModel by viewModels()
    private lateinit var auditAdapter: AuditAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.runAudit()
    }

    private fun setupRecyclerView() {
        auditAdapter = AuditAdapter { entry ->
            val bundle = Bundle().apply { putString("passwordId", entry.id) }
            findNavController().navigate(R.id.action_audit_to_detail, bundle)
        }
        binding.rvAuditIssues.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAuditIssues.adapter = auditAdapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
    }

    private fun observeViewModel() {
        viewModel.auditResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Could show a progress bar
                }
                is Resource.Success -> {
                    val result = resource.data
                    updateUI(result)
                }
                is Resource.Error -> {
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateUI(result: AuditResult) {
        binding.tvHealthScoreLarge.text = "${result.score}%"
        
        val scoreColor = when {
            result.score >= 80 -> R.color.vx_green
            result.score >= 50 -> R.color.vx_purple
            else -> R.color.vx_red
        }
        binding.tvHealthScoreLarge.setTextColor(ContextCompat.getColor(requireContext(), scoreColor))

        if (result.weakPasswords.isEmpty() && result.reusedPasswords.isEmpty()) {
            binding.rvAuditIssues.visibility = View.GONE
            binding.tvNoIssues.visibility = View.VISIBLE
        } else {
            binding.rvAuditIssues.visibility = View.VISIBLE
            binding.tvNoIssues.visibility = View.GONE
            auditAdapter.setResults(result.weakPasswords, result.reusedPasswords)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
