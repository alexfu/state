package com.alexfu.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

typealias Action<T> = (T) -> T

open class Store<T : Any>(initialState: T) {
    private val stateFlow = MutableStateFlow(initialState)
    private val scope = CoroutineScope(Dispatchers.Default)

    val state: T
        get() = stateFlow.value

    fun observeState(): Flow<T> {
        return stateFlow
    }

    fun updateState(action: Action<T>) {
        scope.launch {
            stateFlow.value = action(stateFlow.value)
        }
    }
}
