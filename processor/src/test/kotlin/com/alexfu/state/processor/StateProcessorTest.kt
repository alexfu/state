package com.alexfu.state.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.Test
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.io.File

class StateProcessorTest {
    @Test
    fun `when source is a data class it processes successfully`() {
        val source = SourceFile.kotlin("TestState.kt", """
            import com.alexfu.state.State

            @State
            data class TestState(
                val name: String
            )
        """.trimIndent())

        val file = compile(source)

        expectThat(file)
            .isNotNull()
            .contentEquals(
                """
                import com.alexfu.state.Action
                import kotlin.String

                public object TestStateActions {
                  public fun setName(name: String): Action<TestState> = { it.copy(name = name) }
                }
                """.trimIndent()
            )
    }

    @Test
    fun `when source is not a data class it skips processing`() {
        val source = SourceFile.kotlin("TestClass.kt", """
            import com.alexfu.state.State

            @State
            class TestClass(val name: String) {
              fun copy() {
                // Fake copy
              }
            }
        """.trimIndent())

        val result = compile(source)

        expectThat(result).isNull()
    }

    private fun compile(source: SourceFile): File? {
        val compilation = KotlinCompilation()
            .apply {
                sources = listOf(source)
                symbolProcessorProviders = listOf(StateProcessorProvider())
                inheritClassPath = true
            }

        compilation.compile()

        return compilation.kspSourcesDir.walkTopDown().filter { it.isFile }.firstOrNull()
    }
}

private fun Assertion.Builder<File>.contentEquals(expected: String) {
    assert("content equals %s", expected) { file ->
        val actual = file.readText().trim()
        if (actual == expected) {
            pass()
        } else {
            fail(actual = actual)
        }
    }
}