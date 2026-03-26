package io.github.simonnozaki.koy

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FunctionEnvironmentTests {

    private fun definition(name: String) = TopLevel.FunctionDefinition(name, listOf(), Block(integer(0)))

    @Test
    fun `getDefinition returns val function`() {
        val env = FunctionEnvironment()
        env.setAsVal(definition("foo"))
        assertEquals("foo", env.getDefinition("foo").name)
    }

    @Test
    fun `getDefinition returns mutable val function`() {
        val env = FunctionEnvironment()
        env.setMutableVal(definition("bar"))
        assertEquals("bar", env.getDefinition("bar").name)
    }

    @Test
    fun `should throw on getDefinition for undefined function`() {
        assertThrows<KoyLangRuntimeException> {
            FunctionEnvironment().getDefinition("notDefined")
        }
    }

    @Test
    fun `should throw on setMutableVal with duplicate name`() {
        val env = FunctionEnvironment()
        env.setMutableVal(definition("dup"))
        assertThrows<KoyLangRuntimeException> {
            env.setMutableVal(definition("dup"))
        }
    }
}
