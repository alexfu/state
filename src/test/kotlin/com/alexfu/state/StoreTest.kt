package com.alexfu.state

import app.cash.turbine.test
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.time.ExperimentalTime

@ExperimentalTime
class StoreTest {
    @Test
    fun `observeState emits initial state`() {
        runBlocking {
            val store = Store(0)
            store.observeState().test {
                expectThat(expectItem()).isEqualTo(0)
                cancel()
            }
        }
    }

    @Test
    fun `updateState updates state correctly`() {
        runBlocking {
            val store = Store(1)
            store.observeState().test {
                expectThat(expectItem()).isEqualTo(1)
                store.updateState { oldState -> oldState + 1 }
                expectThat(expectItem()).isEqualTo(2)
                cancel()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `updateState when multiple threads update state it updates state in correct order`() {
        runBlocking {
            val store = Store(initialState = "")
            store.observeState()
                .test {
                    expectThat(expectItem()).isEqualTo("")

                    GlobalScope.launch {
                        store.updateState {
                            Thread.sleep(500)
                            "A"
                        }
                    }

                    GlobalScope.launch {
                        delay(50)
                        store.updateState { "B" }
                    }

                    expectThat(expectItem()).isEqualTo("A")
                    expectThat(expectItem()).isEqualTo("B")
                    cancel()
                }
        }
    }
}
