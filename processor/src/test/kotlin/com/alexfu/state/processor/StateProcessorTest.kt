package com.alexfu.state.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class StateProcessorTest {
    @Test
    fun `simple data class`() {
        val source = SourceFile.kotlin("TestState.kt", """
            import com.alexfu.state.State

            @State
            data class TestState(
                val name: String
            )
        """.trimIndent())

        val result = compile(source)

        expectThat(result) {
            isEqualTo("""
                import com.alexfu.state.Action
                import kotlin.String
                
                public object TestStateActions {
                  public fun setName(name: String): Action<TestState> = { it.copy(name = name) }
                }
            """.trimIndent())
        }
    }

    private fun compile(source: SourceFile): String {
        val compilation = KotlinCompilation()
            .apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(StateProcessorProvider())
                inheritClassPath = true
            }

        compilation.compile()

        return compilation.kspSourcesDir.walkTopDown().filter { it.isFile }.firstOrNull()?.readText()?.trim() ?: ""
    }
}