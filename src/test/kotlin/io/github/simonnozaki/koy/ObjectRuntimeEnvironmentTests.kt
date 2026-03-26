package io.github.simonnozaki.koy

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ObjectRuntimeEnvironmentTests {

    private fun props(vararg pairs: Pair<String, Value>) = mapOf(*pairs)

    @Test
    fun `findBindings returns mutable val object`() {
        val env = ObjectRuntimeEnvironment()
        env.setMutableVal("obj", props("x" to Value.Int(1)))
        assertNotNull(env.findBindings("obj"))
    }

    @Test
    fun `findBindings returns val object`() {
        val env = ObjectRuntimeEnvironment()
        env.setVal("obj", props("x" to Value.Int(1)))
        assertNotNull(env.findBindings("obj"))
    }

    @Test
    fun `findBindings returns null for unknown key`() {
        val env = ObjectRuntimeEnvironment()
        assertNull(env.findBindings("notDefined"))
    }

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
