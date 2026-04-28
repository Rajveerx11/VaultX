package com.vaultx.ui.audit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vaultx.R
import com.vaultx.databinding.ItemPasswordCardBinding
import com.vaultx.data.model.Category
import com.vaultx.data.model.PasswordEntry
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class AuditAdapter(
    private val onItemClick: (PasswordEntry) -> Unit
) : RecyclerView.Adapter<AuditAdapter.AuditViewHolder>() {

    private val items = mutableListOf<AuditItem>()

    sealed class AuditItem {
        data class Header(val title: String, val description: String) : AuditItem()
        data class Entry(val passwordEntry: PasswordEntry, val issueType: String) : AuditItem()
    }

    fun setResults(weak: List<PasswordEntry>, reused: List<PasswordEntry>) {
        items.clear()
        if (weak.isNotEmpty()) {
            items.add(AuditItem.Header("Weak Passwords", "These passwords are easy to guess. Consider making them longer and more complex."))
            items.addAll(weak.map { AuditItem.Entry(it, "WEAK") })
        }
        if (reused.isNotEmpty()) {
            items.add(AuditItem.Header("Reused Passwords", "You are using these passwords on multiple accounts. This is a high security risk."))
            items.addAll(reused.map { AuditItem.Entry(it, "REUSED") })
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is AuditItem.Header -> 0
        is AuditItem.Entry -> 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            HeaderViewHolder(view)
        } else {
            val binding = ItemPasswordCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            EntryViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: AuditViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    abstract class AuditViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: AuditItem)
    }

    class HeaderViewHolder(view: android.view.View) : AuditViewHolder(view) {
        private val text1: android.widget.TextView = view.findViewById(android.R.id.text1)
        private val text2: android.widget.TextView = view.findViewById(android.R.id.text2)

        override fun bind(item: AuditItem) {
            val header = item as AuditItem.Header
            text1.text = header.title
            text1.setTextColor(android.graphics.Color.WHITE)
            text2.text = header.description
            text2.setTextColor(android.graphics.Color.GRAY)
            text2.textSize = 12f
            view.setPadding(0, 32, 0, 8)
        }
    }

    inner class EntryViewHolder(private val binding: ItemPasswordCardBinding) : AuditViewHolder(binding.root) {
        override fun bind(item: AuditItem) {
            val entryItem = item as AuditItem.Entry
            val entry = entryItem.passwordEntry
            val category = Category.fromString(entry.category)

            binding.tvTitle.text = entry.title
            binding.tvUsername.text = entry.username
            binding.ivCategoryIcon.setImageResource(category.icon)
            binding.tvSecurityBadge.text = entryItem.issueType
            binding.tvSecurityBadge.setTextColor(binding.root.resources.getColor(R.color.vx_red, null))

            if (entry.url.isNotEmpty()) {
                val faviconUrl = "https://www.google.com/s2/favicons?sz=64&domain=${entry.url}"
                Glide.with(binding.ivFavicon.context)
                    .load(faviconUrl)
                    .placeholder(R.drawable.ic_globe)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.ivFavicon)
            }

            binding.ivFavorite.setImageResource(
                if (entry.isFavorite) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )

            binding.root.setOnClickListener { onItemClick(entry) }
        }
    }
}
