package com.alexfu.state

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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
