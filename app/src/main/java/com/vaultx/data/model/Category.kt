package com.vaultx.data.model

import androidx.annotation.DrawableRes
import com.vaultx.R

/**
 * Categories for organizing password entries.
 * Each category has a user-friendly [displayName].
 */
enum class Category(val displayName: String, @DrawableRes val icon: Int) {
    SOCIAL("Social", R.drawable.ic_social),
    BANKING("Banking", R.drawable.ic_banking),
    WORK("Work", R.drawable.ic_work),
    OTHERS("Others", R.drawable.ic_others);

    companion object {
        /**
         * Parses a string value into a [Category], case-insensitive.
         * Falls back to [OTHERS] if no match is found.
         */
        fun fromString(value: String): Category {
            return values().find {
                it.name.equals(value, ignoreCase = true)
            } ?: OTHERS
        }
    }
}
