package io.github.simonnozaki.koy

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FunctionEnvironmentTests {
    private fun definition(name: String) = TopLevel.FunctionDefinition(name, listOf(), block(integer(0)))

    // --- getDefinition ---

    @Nested
    inner class `when getting a function definition` {
        @Test
        fun `should return val function by name`() {
            val env = FunctionEnvironment()
            env.setAsVal(definition("foo"))
            assertEquals("foo", env.getDefinition("foo").name)
        }

        @Test
        fun `should return mutable val function by name`() {
            val env = FunctionEnvironment()
            env.setMutableVal(definition("bar"))
            assertEquals("bar", env.getDefinition("bar").name)
        }

        @Test
        fun `should throw on undefined function`() {
            assertThrows<KoyLangRuntimeException> {
                FunctionEnvironment().getDefinition("notDefined")
            }
        }
    }

    // --- setMutableVal ---

    @Nested
    inner class `when setting a mutable val function` {
        @Test
        fun `should throw on duplicate name`() {
            val env = FunctionEnvironment()
            env.setMutableVal(definition("dup"))
            assertThrows<KoyLangRuntimeException> {
                env.setMutableVal(definition("dup"))
            }
        }
    }
}
