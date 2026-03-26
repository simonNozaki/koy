package io.github.simonnozaki.koy

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VariableEnvironmentTests {

    @Nested
    inner class `when adding duplicates` {
        val env = VariableEnvironment(mutableMapOf(), mutableMapOf(), null)

        @BeforeEach
        fun setUp() {
            env.setVal("name", Value.of("koy"))
        }

        @Test
        fun `should throw Exception if same definition exists`() {
            assertThrows<KoyLangRuntimeException> {
                env.setVal("name", Value.of("Ruby"))
            }
        }

        @Test
        fun `should throw Exception if same definition exists in vals`() {
            assertThrows<KoyLangRuntimeException> {
                env.setMutableVal("name", Value.of("Ruby"))
            }
        }
    }
}
