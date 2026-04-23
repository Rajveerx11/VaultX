package com.vaultx.data.model

/**
 * A generic sealed class that wraps data with a loading/success/error state.
 * Used as the standard response wrapper for all repository operations
 * so the UI layer can handle states uniformly.
 *
 * Usage example in ViewModel:
 * ```
 * when (result) {
 *     is Resource.Loading -> showLoading()
 *     is Resource.Success -> showData(result.data)
 *     is Resource.Error -> showError(result.message)
 * }
 * ```
 */
sealed class Resource<out T> {

    /** Operation completed successfully with [data]. */
    data class Success<out T>(val data: T) : Resource<T>()

    /** Operation failed with an error [message]. */
    data class Error(val message: String) : Resource<Nothing>()

    /** Operation is in progress. */
    data object Loading : Resource<Nothing>()
}
