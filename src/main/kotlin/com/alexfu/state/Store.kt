package com.alexfu.state

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

typealias Action<T> = (T) -> T

open class Store<T : Any>(initialState: T) {
    private val stateFlow = MutableStateFlow(initialState)
    private val stateProcessor = Channel<Action<T>>()
    private val scope = CoroutineScope(CoroutineName("StoreCoroutine"))

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

    fun observeState(): StateFlow<T> {
        return stateFlow.asStateFlow()
    }

    fun updateState(action: Action<T>) {
        scope.launch {
            stateProcessor.send(action)
        }
    }
}
