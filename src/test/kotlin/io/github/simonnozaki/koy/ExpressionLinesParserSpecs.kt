package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressionLinesParserSpecs {

    private fun run(source: String): Interpreter {
        val interpreter = Interpreter()
        Parsers.lines().parse(Input.of(source.trimIndent())).result.forEach { interpreter.interpret(it) }
        return interpreter
    }

    // --- Control flow ---

    @Nested
    inner class `when parsing control flow statements` {

        @Test
        fun `should increment variable in while loop`() {
            val interpreter = run("""
                mutable val i = 0;
                while(i < 10) {
                  i = i + 1;
                }
            """)
            assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
        }

        @Test
        fun `should execute else clause when condition is false`() {
            val interpreter = run("""
                mutable val x = 5;
                if (x < 5) {
                  x = 1;
                } else {
                  x = 0;
                }
            """)
            assertEquals(0, interpreter.getValue("x")?.asInt()?.value)
        }

        @Test
        fun `should iterate with for-in`() {
            val source = """
                for (i in 0 to 9) {
                  println(i);
                  i = i + 1;
                }
            """
            val interpreter = Interpreter()
            Parsers.lines().parse(Input.of(source.trimIndent())).result.forEach { interpreter.interpret(it) }
            assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
        }

        @Test
        fun `should accumulate with lte in while condition`() {
            val interpreter = run("""
                mutable val i = 0;
                while (i <= 10) {
                  i = i + 1;
                }
            """)
            assertEquals(11, interpreter.getValue("i")?.asInt()?.value)
        }

        @Test
        fun `should increment in while using prefix increment`() {
            val interpreter = run("""
                mutable val i = 0;
                while (i < 10) {
                  ++i;
                }
                println(i);
            """)
            assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
        }
    }

    // --- Variable declarations ---

    @Nested
    inner class `when parsing variable declarations` {

        @Test
        fun `should parse true and false literals`() {
            val interpreter = run("""
                mutable val t = true;
                mutable val a = false;
            """)
            assertEquals(true, interpreter.getValue("t")?.asBool()?.value)
            assertEquals(false, interpreter.getValue("a")?.asBool()?.value)
        }

        @Test
        fun `should assign mutable variable`() {
            val interpreter = run("""
                mutable val n = 0;
                n = 1;
            """)
            assertEquals(1, interpreter.getValue("n")?.asInt()?.value)
        }

        @Test
        fun `should throw on reassignment to val`() {
            assertThrows<KoyLangRuntimeException>("Declaration [ n ] is already existed, so can not declare again.") {
                run("""
                    val n = 0;
                    n = 1;
                """)
            }
        }

        @Test
        fun `should throw on redeclaration of val`() {
            assertThrows<KoyLangRuntimeException>("Declaration [ n ] is already existed, so can not declare again.") {
                run("""
                    val n = 0;
                    val n = 1;
                """)
            }
        }

        @Test
        fun `should throw on redeclaration of mutable val`() {
            assertThrows<KoyLangRuntimeException>("Declaration [ n ] is already existed, so can not declare again.") {
                run("""
                    mutable val n = 0;
                    mutable val n = 1;
                """)
            }
        }

        @Test
        fun `should throw on mutable val redeclared as val`() {
            assertThrows<KoyLangRuntimeException>("Declaration [ n ] is already existed, so can not declare again.") {
                run("""
                    mutable val n = 0;
                    val n = 1;
                """)
            }
        }
    }

    // --- Literals and expressions ---

    @Nested
    inner class `when parsing literals and expressions` {

        @Test
        fun `should evaluate block expression`() {
            val expr = Parsers.blockExpression().parse(Input.of("{\n  true;\n}".trimIndent())).result
            Assertions.assertTrue(Interpreter().interpret(expr).asBool().value)
        }

        @Test
        fun `should assign array literal`() {
            val interpreter = run("mutable val odd = [1, 3, 5];")
            val arr = interpreter.getValue("odd")?.asArray()?.items
            assertEquals(3, arr?.size)
            assertEquals(true, arr?.containsAll(listOf(Value.of(1), Value.of(3), Value.of(5))))
        }

        @Test
        fun `should assign and evaluate set literal`() {
            val interpreter = run("""
                val domains = %{
                  "ezweb.ne.jp",
                  "gmail.com",
                  "yahoo.jp"
                };
                println(domains);
            """)
            val set = interpreter.getValue("domains")?.asSet()?.value
            assertEquals(3, set?.size)
            set?.containsAll(listOf(
                Value.String("ezweb.ne.jp"),
                Value.String("gmail.com"),
                Value.String("yahoo.jp")
            ))?.let { Assertions.assertTrue(it) }
        }

        @Test
        fun `should assign and call object literal`() {
            val interpreter = run("""
                val o = {
                  id: 1,
                  title: "Get ready to meeting"
                };
            """)
            assertEquals(1, interpreter.getValue("o")?.asObject()?.value?.get("id")?.asInt()?.value)
        }

        @Test
        fun `should emit and call function literal from function`() {
            val interpreter = run("""
                val Age = |v| {
                  mutable val _v = v;
                  {
                    value: _v,
                    getOld: |_| {
                      _v = ++_v;
                      _v;
                    }
                  };
                };
                val now = Age(21);
            """)
            val now = interpreter.getValue("now") ?: throw KoyLangRuntimeException("")
            Assertions.assertTrue(now.isObject())
            assertEquals(21, now.asObject().value["value"]?.asInt()?.value)
        }

        @Test
        fun `should assign function literal to variable`() {
            val interpreter = run("""
                val l = |x| {
                  "Hello, " + x;
                };
            """)
            assertEquals("l", interpreter.getFunctions().getDefinition("l").name)
            assertEquals("x", interpreter.getFunctions().getDefinition("l").args[0])
        }

        @Test
        fun `should increment and decrement`() {
            val interpreter = run("""
                mutable val n = 1;
                val n2 = ++n;
                mutable val m = 1;
                val m2 = --m;
            """)
            assertEquals(2, interpreter.getValue("n2")?.asInt()?.value)
            assertEquals(0, interpreter.getValue("m2")?.asInt()?.value)
        }
    }

    // --- Collection operations ---

    @Nested
    inner class `when parsing collection operations` {

        @Test
        fun `should get element in array by index`() {
            val interpreter = run("""
                mutable val i = 0;
                mutable val j = 0;
                val odd = [1, 3, 5];
                while (i < 3) {
                  println(odd->i);
                  j = odd->i;
                  ++i;
                }
            """)
            assertEquals(5, interpreter.getValue("j")?.asInt()?.value)
        }

        @Test
        fun `should conjoin element with array`() {
            val interpreter = run("""
                val odd = [1, 3, 5];
                val odd2 = odd<-7;
                println(odd);
                println(odd2);
            """)
            val odd = interpreter.getValue("odd")?.asArray()
            val odd2 = interpreter.getValue("odd2")?.asArray()
            odd?.let { assertEquals(3, it.items.size) }
            odd2?.let { assertEquals(4, it.items.size) }
            odd2?.let {
                assertTrue(it.items.containsAll(listOf(Value.of(1), Value.of(3), Value.of(5), Value.of(7))))
            }
        }

        @Test
        fun `should conjoin element with set`() {
            val interpreter = run("""
                val odd = %{1, 3, 5};
                val odd2 = odd<-7;
                println(odd);
                println(odd2);
            """)
            val odd = interpreter.getValue("odd")?.asSet()
            val odd2 = interpreter.getValue("odd2")?.asSet()
            odd?.let { assertEquals(3, it.value.size) }
            odd2?.let { assertEquals(4, it.value.size) }
            odd2?.let {
                assertTrue(it.value.containsAll(listOf(Value.of(1), Value.of(3), Value.of(5), Value.of(7))))
            }
        }

        @Test
        fun `should return nil for missing element in array`() {
            val interpreter = run("""
                mutable val j = 0;
                val odd = [1, 3, 5];
                val b = odd->3 == nil;
            """)
            interpreter.getValue("b")?.asBool()?.value?.let { Assertions.assertTrue(it) }
        }

        @Test
        fun `should return true for equal arrays`() {
            val interpreter = run("""
                val l = [0, 1, 2];
                val m = [0, 1, 2];
                val r = l == m;
            """)
            val r = interpreter.getValue("r")?.asBool()?.value ?: throw KoyLangRuntimeException("")
            Assertions.assertTrue(r)
        }

        @Test
        fun `should throw on equality check with incompatible collection types`() {
            assertThrows<KoyLangRuntimeException> {
                run("""
                    val l = [0, 1, 2];
                    val m = %{0, 1, 2};
                    val r = l == m;
                """)
            }
        }
    }
}
