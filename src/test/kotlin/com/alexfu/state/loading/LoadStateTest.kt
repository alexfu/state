package com.alexfu.state.loading

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class LoadStateTest {
    @Test
    @DisplayName("Transitions to loading")
    fun transitionsToLoading() {
        val state = LoadState.None(0)
        expectThat(state.loading()).isEqualTo(LoadState.Loading(0))
    }

    @Test
    @DisplayName("Transitions to success")
    fun transitionsToSuccess() {
        val state = LoadState.None(0)
        expectThat(state.success()).isEqualTo(LoadState.Success(0))
    }

    @Test
    @DisplayName("Transitions to error")
    fun transitionsToError() {
        val error = RuntimeException()
        val state = LoadState.Error(0, error)
        expectThat(state.error(error)).isEqualTo(LoadState.Error(0, error))
    }

    @Test
    @DisplayName("Transitions to none")
    fun transitionsToNone() {
        val state = LoadState.Success(0)
        expectThat(state.none()).isEqualTo(LoadState.None(0))
    }
}
