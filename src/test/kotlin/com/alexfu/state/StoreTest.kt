package com.alexfu.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly

@ExperimentalCoroutinesApi
class StoreTest {
    @Test
    @DisplayName("Emits initial state")
    fun emitsInitialState() {
        runBlockingTest {
            val store = Store("Hello")

            val output = mutableListOf<String>()
            store.observeState().take(1).toList(output)

            expectThat(output).containsExactly("Hello")
        }
    }

    @Test
    @DisplayName("Updates state")
    fun updatesState() {
        runBlockingTest {
            val store = Store(1)
            val output = mutableListOf<Int>()
            launch {
                store.observeState().take(2).toList(output)
            }

            store.updateState { oldState -> oldState + 1 }

            expectThat(output).containsExactly(1, 2)
        }
    }
}
