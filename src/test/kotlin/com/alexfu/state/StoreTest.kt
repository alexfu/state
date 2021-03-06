package com.alexfu.state

import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.time.ExperimentalTime

@ExperimentalTime
class StoreTest {
    @Test
    @DisplayName("Emits initial state")
    fun emitsInitialState() {
        runBlocking {
            val store = Store(0)
            store.observeState().test {
                expectThat(expectItem()).isEqualTo(0)
                cancel()
            }
        }
    }

    @Test
    @DisplayName("Updates state")
    fun updatesState() {
        runBlocking {
            val store = Store(1)
            store.observeState().test {
                store.updateState { oldState -> oldState + 1 }
                expectThat(expectItem()).isEqualTo(1)
                expectThat(expectItem()).isEqualTo(2)
                cancel()
            }
        }
    }
}
