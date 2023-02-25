package com.alexfu.state

sealed class LoadState {
    object Loading : LoadState()
    object Success : LoadState()
    data class Error(val error: Throwable) : LoadState()
}
