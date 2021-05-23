package com.alexfu.state.loading

import app.cash.turbine.test
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LoadableStoreTest {
    @Test
    @DisplayName("Maintains LoadState when updating value")
    fun maintainsLoadStateWhenUpdatingValue() {
        runBlocking {
            val store = LoadableStore(LoadState.Success(0))
            store.observeValue().test {
                store.updateValue { oldState -> oldState + 1 }
                expectThat(expectItem()).isEqualTo(0)
                expectThat(expectItem()).isEqualTo(1)
                expectThat(store.state).isEqualTo(LoadState.Success(1))
                cancel()
            }
        }
    }
}
