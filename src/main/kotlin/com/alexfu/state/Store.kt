package com.alexfu.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

typealias Action<T> = (T) -> T

open class Store<T : Any>(initialState: T) {
    private val stateFlow = MutableStateFlow(initialState)

    val state: T
        get() = stateFlow.value

    fun observeState(): Flow<T> {
        return stateFlow
    }

    @Synchronized
    fun updateState(action: Action<T>) {
        stateFlow.value = action(stateFlow.value)
    }
}
