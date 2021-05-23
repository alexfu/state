package com.alexfu.state.loading

import com.alexfu.state.Action
import com.alexfu.state.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadableStore<T : Any>(initialState: LoadState<T>) : Store<LoadState<T>>(initialState) {
    constructor(initialState: T) : this(LoadState.None(initialState))

    fun observeValue(): Flow<T> {
        return observeState()
            .map { it.value }
    }

    fun updateValue(action: Action<T>) {
        updateState { oldState ->
            val updatedValue = action(oldState.value)
            when (oldState) {
                is LoadState.Error -> oldState.copy(updatedValue, oldState.error)
                is LoadState.Loading -> oldState.copy(updatedValue)
                is LoadState.None -> oldState.copy(updatedValue)
                is LoadState.Success -> oldState.copy(updatedValue)
            }
        }
    }
}
