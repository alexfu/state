package com.alexfu.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

typealias Action<T> = (T) -> T

open class Store<T : Any>(initialState: T) {
    private val stateFlow = MutableStateFlow(initialState)
    private val stateProcessor = Channel<Action<T>>()
    private val scope = CoroutineScope(Dispatchers.Default)

    val state: T
        get() = stateFlow.value

    init {
        scope.launch {
            stateProcessor.receiveAsFlow()
                .map { action -> action(state) }
                .collect { newState ->
                    stateFlow.value = newState
                }
        }
    }

    fun observeState(): Flow<T> {
        return stateFlow
    }

    fun updateState(action: Action<T>) {
        scope.launch {
            stateProcessor.send(action)
        }
    }
}
