package com.vaultx.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vaultx.databinding.ItemPasswordCardBinding
import com.vaultx.data.model.Category
import com.vaultx.data.model.PasswordEntry

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * RecyclerView adapter for password entry cards on the dashboard.
 * Uses ListAdapter with DiffUtil for efficient updates.
 */
class PasswordAdapter(
    private val onItemClick: (PasswordEntry) -> Unit,
    private val onCopyClick: (PasswordEntry) -> Unit,
    private val onFavoriteClick: (PasswordEntry) -> Unit
) : ListAdapter<PasswordEntry, PasswordAdapter.PasswordViewHolder>(PasswordDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val binding = ItemPasswordCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PasswordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PasswordViewHolder(
        private val binding: ItemPasswordCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: PasswordEntry) {
            val category = Category.fromString(entry.category)

            binding.tvTitle.text = entry.title
            binding.tvUsername.text = entry.username
            binding.tvMaskedPassword.text = "••••••"
            binding.ivCategoryIcon.setImageResource(category.icon)

            // Load Favicon using Google's favicon service
            if (entry.url.isNotEmpty()) {
                val faviconUrl = "https://www.google.com/s2/favicons?sz=64&domain=${entry.url}"
                Glide.with(binding.ivFavicon.context)
                    .load(faviconUrl)
                    .placeholder(R.drawable.ic_globe)
                    .error(R.drawable.ic_globe)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivFavicon)
            } else {
                binding.ivFavicon.setImageResource(R.drawable.ic_globe)
            }

            // Favorite Status
            binding.ivFavorite.setImageResource(
                if (entry.isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            // Security badge based on category
            binding.tvSecurityBadge.text = when (category) {
                Category.BANKING -> "HIGH SECURITY"
                Category.WORK -> "SECURED"
                Category.SOCIAL -> "STANDARD"
                Category.OTHERS -> "STANDARD"
            }

            // Click handlers
            binding.root.setOnClickListener { onItemClick(entry) }
            binding.ivCopy.setOnClickListener { onCopyClick(entry) }
            binding.ivFavorite.setOnClickListener { onFavoriteClick(entry) }
        }
    }

    companion object PasswordDiffCallback : DiffUtil.ItemCallback<PasswordEntry>() {
        override fun areItemsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: PasswordEntry, newItem: PasswordEntry) =
            oldItem == newItem
    }
}



