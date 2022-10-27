package io.github.simonnozaki.koy

import org.javafp.parsecj.input.Input
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressionLinesParserSpecs {
    @Test
    fun `should add in while`() {
        val interpreter = Interpreter()
        // Need to trim indents since `lines` does not ignore spaces
        val program = """
            mutable val i = 0;
            while(i < 10) {
              i = i + 1;
            }
            """.trimIndent()
        val statements = Parsers.lines().parse(Input.of(program)).result
        for (statement in statements) {
            interpreter.interpret(statement)
        }
        println(statements)

        assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
    }

    @Test
    fun `should be else clause`() {
        val interpreter = Interpreter()
        val source = """
                mutable val x = 5;
                if (x < 5) {
                  x = 1;
                } else {
                  x = 0;
                }
            """.trimIndent()
        val statements = Parsers.lines().parse(Input.of(source)).result
        for (statement in statements) {
            interpreter.interpret(statement)
        }
        assertEquals(0, interpreter.getValue("x")?.asInt()?.value)
    }

    @Test
    fun `can loop for in`() {
        val interpreter = Interpreter()
        val source = """
                for (i in 0 to 9) {
                  println(i);
                  i = i + 1;
                }
            """.trimIndent()
        val statements = Parsers.lines()
            .parse(Input.of(source))
            .result
        statements.forEach { interpreter.interpret(it) }

        assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
    }

    @Test
    fun `can evaluate block expression`() {
        val source = """
                {
                  true;
                }
            """.trimIndent()
        val expr = Parsers.blockExpression().parse(Input.of(source)).result
        val result = Interpreter().interpret(expr)

        Assertions.assertTrue(result.asBool().value)
    }

    @Test
    fun `can assign array literal`() {
        val interpreter = Interpreter()
        val source = """
            mutable val odd = [1, 3, 5]; 
            """.trimIndent()
        println(source)
        val statements = Parsers.lines().parse(Input.of(source)).result
        statements.forEach { interpreter.interpret(it) }
        val arr = interpreter.getValue("odd")?.asArray()?.items

        assertEquals(3, arr?.size)
        assertEquals(
            true,
            arr?.containsAll(
                listOf(
                    Value.of(1),
                    Value.of(3),
                    Value.of(5)
                )
            )
        )
    }

    @Test
    fun `can parse true and false`() {
        val interpreter = Interpreter()
        val source = """
            mutable val t = true;
            mutable val a = false;
            """.trimIndent()
        val statements = Parsers.lines().parse(Input.of(source)).result
        statements.forEach { interpreter.interpret(it) }
        val t = interpreter.getValue("t")?.asBool()?.value
        val a = interpreter.getValue("a")?.asBool()?.value

        assertEquals(true, t)
        assertEquals(false, a)
    }

    @Test
    fun `should emit and call function literal from function`() {
        val interpreter = Interpreter()
        val source = """
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
            """.trimIndent()
        Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }
        val now = interpreter.getValue("now") ?: throw KoyLangRuntimeException("")

        Assertions.assertTrue(now.isObject())
        assertEquals(21, now.asObject().value["value"]?.asInt()?.value)
    }

    @Test
    fun `should be equal arrays`() {
        val interpreter = Interpreter()
        val source = """
            val l = [0, 1, 2];
            val m = [0, 1, 2];
            val r = l == m;
            """.trimIndent()
        Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }
        val r = interpreter.getValue("r")?.asBool()?.value ?: throw KoyLangRuntimeException("")

        Assertions.assertTrue(r)
    }

    @Test
    fun `should throw Exception with not compatible types`() {
        assertThrows<KoyLangRuntimeException> {
            val interpreter = Interpreter()
            val source = """
                val l = [0, 1, 2];
                val m = %{0, 1, 2};
                val r = l == m;
                """.trimIndent()
            Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }
        }
    }

    @Test
    fun `can assign and call object literal`() {
        val source = """
            val o = {
              id: 1,
              title: "Get ready to meeting"
            };
            """.trimIndent()
        val statements = Parsers.lines().parse(Input.of(source)).result
        val interpreter = Interpreter()
        statements.forEach { interpreter.interpret(it) }

        println(interpreter.getVariables())
        assertEquals(1, interpreter.getValue("o")?.asObject()?.value?.get("id")?.asInt()?.value)
    }

    @Test
    fun `can assign function literal with no parameter to variable`() {
        val interpreter = Interpreter()
        // Lambda unused parameter is shortcuttable
        val source = """
            val l = |x| {
              "Hello, " + x; 
            };
            """.trimIndent()
        val statements = Parsers.lines().parse(Input.of(source)).result
        statements.forEach { interpreter.interpret(it) }
        val functions = interpreter.getFunctions()

        assertEquals("l", functions.getDefinition("l").name)
        assertEquals("x", functions.getDefinition("l").args[0])
    }

    @Test
    fun `can increment and decrement`() {
        val interpreter = Interpreter()
        val source = """
            mutable val n = 1;
            val n2 = ++n;
            mutable val m = 1;
            val m2 = --m;
            """.trimIndent()
        Parsers.lines()
            .parse(Input.of(source))
            .result
            .forEach { interpreter.interpret(it) }

        assertEquals(2, interpreter.getValue("n2")?.asInt()?.value)
        assertEquals(0, interpreter.getValue("m2")?.asInt()?.value)
    }

    @Test
    fun `can not reassign val declaration`() {
        assertThrows<KoyLangRuntimeException> ("Declaration [ n ] is already existed, so can not declare again.") {
            val interpreter = Interpreter()
            val source = """
                val n = 0;
                n = 1;
                """.trimIndent()
            Parsers.lines()
                .parse(Input.of(source))
                .result
                .forEach { interpreter.interpret(it) }
        }
    }

    @Test
    fun `can not redeclare val variable`() {
        assertThrows<KoyLangRuntimeException> ("Declaration [ n ] is already existed, so can not declare again.") {
            val interpreter = Interpreter()
            val source = """
                val n = 0;
                val n = 1;
                """.trimIndent()
            Parsers.lines()
                .parse(Input.of(source))
                .result
                .forEach { interpreter.interpret(it) }
        }
    }

    @Test
    fun `can not redeclare mutable val variable`() {
        assertThrows<KoyLangRuntimeException>("Declaration [ n ] is already existed, so can not declare again.") {
            val interpreter = Interpreter()
            val source = """
                mutable val n = 0;
                mutable val n = 1;
                """.trimIndent()
            Parsers.lines()
                .parse(Input.of(source))
                .result
                .forEach { interpreter.interpret(it) }
        }
    }

    @Test
    fun `can not redeclare mutable val and val at once`() {
        assertThrows<KoyLangRuntimeException>("Declaration [ n ] is already existed, so can not declare again.") {
            val interpreter = Interpreter()
            val source = """
                mutable val n = 0;
                val n = 1;
                """.trimIndent()
            Parsers.lines()
                .parse(Input.of(source))
                .result
                .forEach { interpreter.interpret(it) }
        }
    }

    @Test
    fun `can assign mutable variable`() {
        val interpreter = Interpreter()
        val source = """
            mutable val n = 0;
            n = 1;
            """.trimIndent()
        Parsers.lines()
            .parse(Input.of(source))
            .result
            .forEach { interpreter.interpret(it) }

        println(interpreter.getVariables())
        assertEquals(1, interpreter.getValue("n")?.asInt()?.value)
    }

    @Test
    fun `can assign and evaluate set literal`() {
        val interpreter = Interpreter()
        val source = """
            val domains = %{
              "ezweb.ne.jp",
              "gmail.com",
              "yahoo.jp"
            };
            println(domains);
            """.trimIndent()
        Parsers.lines()
            .parse(Input.of(source))
            .result
            .forEach { interpreter.interpret(it) }

        println(interpreter.getVariables())
        val set = interpreter.getValue("domains")?.asSet()?.value
        assertEquals(3, set?.size)
        set?.containsAll(
            listOf(
                Value.String("ezweb.ne.jp"),
                Value.String("gmail.com"),
                Value.String("yahoo.jp")
            )
        )?.let { Assertions.assertTrue(it) }
    }

    @Test
    fun `can increment in while`() {
        val interpreter = Interpreter()
        val source = """
            mutable val i = 0;
            while (i < 10) {
              ++i;
            }
            println(i);
            """.trimIndent()
        Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }

        assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
    }

    @Test
    fun `can get element in array`() {
        val interpreter = Interpreter()
        val source = """
            mutable val i = 0;
            mutable val j = 0;
            val odd = [1, 3, 5];
            while (i < 3) {
              println(odd->i);
              j = odd->i;
              ++i;
            }
            """.trimIndent()

        Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }

        assertEquals(5, interpreter.getValue("j")?.asInt()?.value)
    }

    @Test
    fun `should conjoin element with array`() {
        val interpreter = Interpreter()
        val source = """
        val odd = [1, 3, 5];
        val odd2 = odd<-7;
        println(odd);
        println(odd2);
        """.trimIndent()
        Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }
        val odd = interpreter.getValue("odd")?.asArray()
        val odd2 = interpreter.getValue("odd2")?.asArray()

        odd?.let { assertEquals(3, it.items.size) }
        odd2?.let { assertEquals(4, it.items.size) }
        odd2?.let { assertTrue(it.items.containsAll(listOf(
            Value.of(1),
            Value.of(3),
            Value.of(5),
            Value.of(7)
        ))) }
    }

    @Test
    fun `should conjoin element with set`() {
        val interpreter = Interpreter()
        val source = """
        val odd = %{1, 3, 5};
        val odd2 = odd<-7;
        println(odd);
        println(odd2);
        """.trimIndent()
        Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }
        val odd = interpreter.getValue("odd")?.asSet()
        val odd2 = interpreter.getValue("odd2")?.asSet()

        odd?.let { assertEquals(3, it.value.size) }
        odd2?.let { assertEquals(4, it.value.size) }
        odd2?.let { assertTrue(it.value.containsAll(listOf(
            Value.of(1),
            Value.of(3),
            Value.of(5),
            Value.of(7)
        ))) }
    }

    @Test
    fun `should be nil with not existing elm in array`() {
        val interpreter = Interpreter()
        val source = """
            mutable val j = 0;
            val odd = [1, 3, 5];
            val b = odd->3 == nil;
            """.trimIndent()

        Parsers.lines().parse(Input.of(source)).result.forEach { interpreter.interpret(it) }

        interpreter.getValue("b")?.asBool()?.value?.let { Assertions.assertTrue(it) }
    }
}