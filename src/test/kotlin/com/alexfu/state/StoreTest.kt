package com.alexfu.state

import app.cash.turbine.test
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
                expectThat(awaitItem()).isEqualTo(0)
                cancel()
            }
        }
    }

    @Test
    fun `updateState updates state correctly`() {
        runBlocking {
            val store = Store(1)
            store.observeState().test {
                expectThat(awaitItem()).isEqualTo(1)
                store.updateState { oldState -> oldState + 1 }
                expectThat(awaitItem()).isEqualTo(2)
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
                    expectThat(awaitItem()).isEqualTo("")

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

                    expectThat(awaitItem()).isEqualTo("A")
                    expectThat(awaitItem()).isEqualTo("B")
                    cancel()
                }
        }
    }
}
