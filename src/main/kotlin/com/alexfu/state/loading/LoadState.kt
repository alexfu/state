package com.alexfu.state.loading

sealed class LoadState<T>(open val value: T) {
    data class None<T>(override val value: T) : LoadState<T>(value)
    data class Loading<T>(override val value: T) : LoadState<T>(value)
    data class Success<T>(override val value: T) : LoadState<T>(value)
    data class Error<T>(override val value: T, val error: Throwable) : LoadState<T>(value)

    fun none(): LoadState<T> {
        return None(value)
    }

    fun loading(): LoadState<T> {
        return Loading(value)
    }

    fun success(): LoadState<T> {
        return Success(value)
    }

    fun error(error: Throwable): LoadState<T> {
        return Error(value, error)
    }

    fun withNewValue(newValue: T): LoadState<T> {
        return when (this) {
            is Error -> copy(newValue, error)
            is Loading -> copy(newValue)
            is None -> copy(newValue)
            is Success -> copy(newValue)
        }
    }
}
