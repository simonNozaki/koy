package io.github.simonnozaki.koy

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ObjectRuntimeEnvironmentTests {

    private fun props(vararg pairs: Pair<String, Value>) = mapOf(*pairs)

    // --- findBindings ---

    @Nested
    inner class `when finding object bindings` {

        @Test
        fun `should return bindings for mutable val object`() {
            val env = ObjectRuntimeEnvironment()
            env.setMutableVal("obj", props("x" to Value.Int(1)))
            assertNotNull(env.findBindings("obj"))
        }

        @Test
        fun `should return bindings for val object`() {
            val env = ObjectRuntimeEnvironment()
            env.setVal("obj", props("x" to Value.Int(1)))
            assertNotNull(env.findBindings("obj"))
        }

        @Test
        fun `should return null for unknown key`() {
            val env = ObjectRuntimeEnvironment()
            assertNull(env.findBindings("notDefined"))
        }
    }

    // --- setVal / setMutableVal duplicates ---

    @Nested
    inner class `when setting a duplicate object key` {

        @Test
        fun `should throw on setVal with duplicate key`() {
            val env = ObjectRuntimeEnvironment()
            env.setVal("obj", props("x" to Value.Int(1)))
            assertThrows<KoyLangRuntimeException> {
                env.setVal("obj", props("x" to Value.Int(2)))
            }
        }

        @Test
        fun `should throw on setMutableVal with duplicate key`() {
            val env = ObjectRuntimeEnvironment()
            env.setMutableVal("obj", props("x" to Value.Int(1)))
            assertThrows<KoyLangRuntimeException> {
                env.setMutableVal("obj", props("x" to Value.Int(2)))
            }
        }
    }
}
