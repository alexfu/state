package com.alexfu.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

typealias Action<T> = (T) -> T

open class Store<T : Any>(initialState: T) {
    private val stateFlow = MutableStateFlow(initialState)

    val state: T
        get() = stateFlow.value

    fun observeState(): StateFlow<T> {
        return stateFlow
    }

    @Synchronized
    fun updateState(action: Action<T>) {
        stateFlow.value = action(stateFlow.value)
    }
}
