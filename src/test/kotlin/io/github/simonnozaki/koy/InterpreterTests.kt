package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import kotlin.test.assertEquals

/**
 * Interpreter specs
 */
class InterpreterTests {
    private val interpreter = Interpreter()

    @Test
    fun can_add_10_to_20() {
        val expression = add(
            IntegerLiteral(10), IntegerLiteral(20)
        )
        val result = interpreter.interpret(expression).asInt().value
        assertEquals(30, result)
    }

    @Test
    fun can_walk_tree_nodes() {
        val expression = add(
            multiply(
                IntegerLiteral(3), IntegerLiteral(5)
            ),
            IntegerLiteral(10)
        )

        val result = interpreter.interpret(expression).asInt().value

        assertEquals(25, result)
    }

    @Test
    fun can_evaluate_main_function() {
        val topLevels = listOf(
            // fn main() {
            //   add(10, 20)
            // }
            defineFunction(
                "main",
                listOf(),
                Block(
                    Println(call("add", integer(10), integer(20)))
                )
            ),
            // fn add(v1, v2) {
            //   v1 + v2
            // }
            defineFunction(
                "add",
                listOf("v1", "v2"),
                Block(
                    add(identifier("v1"), identifier("v2"))
                )
            )
        )

        val result = interpreter.callMain(Program(topLevels))

        assertEquals(30, result.asInt().value)
    }

    @Test
    fun can_evaluate_factorial() {
        val topLevels = listOf(
            // fn factorial(v) {
            //   if (v < 2) {
            //     1
            //   }
            //   factorial(v - 1) * v
            // }
            defineFunction(
                "factorial", listOf("v"),
                Block(
                    If(
                        lessThan(identifier("v"), integer(2)),
                        integer(1),
                        Optional.of(
                            multiply(
                                call("factorial", subtract(identifier("v"), integer(1))),
                                identifier("v")
                            )
                        )
                    )
                )
            ),
            // fn main() {
            //   factorial(5)
            // }
            defineFunction(
                "main", listOf(),
                Block(
                    Println(call("factorial", integer(5)))
                )
            )
        )

        val result = interpreter.callMain(Program(topLevels))

        assertEquals(120, result.asInt().value)
    }

    @Test
    fun can_increment_in_while() {
        val statements = listOf(
            // mutable val i = 0;
            MutableValDeclaration("i", IntegerLiteral(0)),
            // while(x < 10) {
            //   i = i + 1;
            // }
            While(
                BinaryExpression(Operator.LESS_THAN, identifier("i"), IntegerLiteral(10)),
                assign("i", BinaryExpression(Operator.ADD, identifier("i"), IntegerLiteral(1)))
            )
        )
        for (statement in statements) {
            interpreter.interpret(statement)
        }
        assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
    }

    @Test
    fun can_define_array_literal() {
        // val a = [1, 3, 5]
        val statement = ValDeclaration("a", Array(IntegerLiteral(1), IntegerLiteral(3), IntegerLiteral(5)))
        interpreter.interpret(statement)

        val arrayItems = interpreter.getValue("a")?.asArray()
        assertEquals(3, arrayItems?.items?.size)
    }

    @Test
    fun can_evaluate_object() {
        val interpreter = Interpreter()
        // val o = { a: 1, b: "1" }
        val statement = ValDeclaration(
            "o",
            Object(
                mapOf(
                    "a" to integer(1),
                    "b" to str("1")
                )
            )
        )
        interpreter.interpret(statement)
        val o = interpreter.getValue("o")?.asObject()?.value

        assertEquals(1, o?.get("a")?.asInt()?.value)
        assertEquals("1", o?.get("b")?.asString()?.value)
    }

    @Test
    fun `can define function literal`() {
        val interpreter = Interpreter()
        // val f = { x, y ->
        //   x+y;
        // };
        val statement = ValDeclaration(
            "f",
            FunctionLiteral(
                listOf("x", "y"),
                BlockExpression(
                    listOf(
                        add(identifier("x"), identifier("y"))
                    )
                )
            )
        )
        interpreter.interpret(statement)
        val f = interpreter.getFunction("f")
        println(f)

        assertEquals("f", f.name)
        assertEquals(
            true,
            f.args.containsAll(
                listOf(
                    "x",
                    "y"
                )
            )
        )
    }

    @Test
    fun `can increment and decrement`() {
        val interpreter = Interpreter()
        listOf(
            // val n = 1;
            // val n2 = n++;
            // val m = 1;
            // val m2 = m--;
            MutableValDeclaration("n", integer(1)),
            ValDeclaration("n2", increment("n")),
            MutableValDeclaration("m", integer(1)),
            ValDeclaration("m2", decrement("m"),)
        ).forEach { interpreter.interpret(it) }
        println(interpreter.getVariables())

        assertEquals(2, interpreter.getValue("n2")?.asInt()?.value)
        assertEquals(0, interpreter.getValue("m2")?.asInt()?.value)
    }

    @Test
    fun `should be true of logical and`() {
        // true and true
        val interpreter = Interpreter()
        val statement = BinaryExpression(Operator.LOGICAL_AND, BoolLiteral(true), BoolLiteral(true))
        val result = interpreter.interpret(statement)

        assertTrue(result.isBool())
        assertTrue(result.asBool().value)
    }

    @Test
    fun `should be true of logical or`() {
        // true and true
        val interpreter = Interpreter()
        val statement = BinaryExpression(Operator.LOGICAL_OR, BoolLiteral(true), BoolLiteral(false))
        val result = interpreter.interpret(statement)

        assertTrue(result.isBool())
        assertTrue(result.asBool().value)
    }

    @Test
    fun `can initialize set literal`() {
        val interpreter = Interpreter()
        val statement = SetLiteral(setOf(StringLiteral("Java"), StringLiteral("Kotlin"), StringLiteral("Koy")))
        val result = interpreter.interpret(statement)

        assertTrue(result.isSet())
        assertTrue(result.asSet().value.containsAll(listOf(Value.String("Java"), Value.String("Kotlin"), Value.String("Koy"))))
    }

    @Test
    fun `can get and call method related to object`() {
        val interpreter = Interpreter()
        listOf(
            // val object = {
            //   print: |msg| {
            //     "hello, " + msg;
            //   }
            // }
            ValDeclaration(
                "object",
                Object(
                    mapOf(
                        "print" to FunctionLiteral(
                            listOf("msg"),
                            Block(
                                BinaryExpression(Operator.ADD, StringLiteral("Hello, "), Identifier("msg"))
                            )
                        )
                    )
                )
            ),
            // val r = object->print("koy");
            ValDeclaration("r", MethodCall(Identifier("object"), Identifier("print"), listOf(StringLiteral("Koy"))))
        )
            .forEach { interpreter.interpret(it) }
        println(interpreter.getValue("r"))
    }

    // --- Type mismatch in arithmetic/comparison operations ---

    @Test
    fun `should throw on subtract with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(subtract(str("hello"), integer(1)))
        }
    }

    @Test
    fun `should throw on multiply with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(multiply(str("hello"), integer(2)))
        }
    }

    @Test
    fun `should throw on divide with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(divide(str("hello"), integer(2)))
        }
    }

    @Test
    fun `should throw on remainder with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(remain(str("hello"), integer(2)))
        }
    }

    @Test
    fun `should throw on divide by zero`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(divide(integer(10), integer(0)))
        }
    }

    @Test
    fun `should throw on remainder by zero`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(remain(integer(10), integer(0)))
        }
    }

    @Test
    fun `should throw on less-than with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(lessThan(str("hello"), integer(1)))
        }
    }

    @Test
    fun `should throw on less-than-or-equal with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(lessThanEqual(str("hello"), integer(1)))
        }
    }

    @Test
    fun `should throw on greater-than with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(greaterThan(str("hello"), integer(1)))
        }
    }

    @Test
    fun `should throw on greater-than-or-equal with non-integer`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(greaterThanEqual(str("hello"), integer(1)))
        }
    }

    // --- Parameter count mismatch ---

    @Test
    fun `should throw on function call with too few args`() {
        val topLevels = listOf(
            defineFunction("add", listOf("x", "y"), Block(add(identifier("x"), identifier("y")))),
            defineFunction("main", listOf(), Block(call("add", integer(1))))
        )
        assertThrows<KoyLangRuntimeException> {
            Interpreter().callMain(Program(topLevels))
        }
    }

    @Test
    fun `should throw on function call with too many args`() {
        val topLevels = listOf(
            defineFunction("add", listOf("x", "y"), Block(add(identifier("x"), identifier("y")))),
            defineFunction("main", listOf(), Block(call("add", integer(1), integer(2), integer(3))))
        )
        assertThrows<KoyLangRuntimeException> {
            Interpreter().callMain(Program(topLevels))
        }
    }

    @Test
    fun `should throw on method call with too few args`() {
        val interpreter = Interpreter()
        interpreter.interpret(
            ValDeclaration(
                "obj",
                Object(
                    mapOf(
                        "add" to FunctionLiteral(
                            listOf("x", "y"),
                            Block(add(identifier("x"), identifier("y")))
                        )
                    )
                )
            )
        )
        assertThrows<KoyLangRuntimeException> {
            interpreter.interpret(
                MethodCall(Identifier("obj"), Identifier("add"), listOf(integer(1)))
            )
        }
    }

    @Test
    fun `should throw on method call with too many args`() {
        val interpreter = Interpreter()
        interpreter.interpret(
            ValDeclaration(
                "obj",
                Object(
                    mapOf(
                        "add" to FunctionLiteral(
                            listOf("x", "y"),
                            Block(add(identifier("x"), identifier("y")))
                        )
                    )
                )
            )
        )
        assertThrows<KoyLangRuntimeException> {
            interpreter.interpret(
                MethodCall(Identifier("obj"), Identifier("add"), listOf(integer(1), integer(2), integer(3)))
            )
        }
    }

    // --- Arithmetic operators (happy path) ---

    @Test
    fun `can divide integers`() {
        val result = Interpreter().interpret(divide(integer(10), integer(2))).asInt().value
        assertEquals(5, result)
    }

    @Test
    fun `can compute remainder`() {
        val result = Interpreter().interpret(remain(integer(10), integer(3))).asInt().value
        assertEquals(1, result)
    }

    // --- Comparison operators (happy path) ---

    @Test
    fun `can evaluate greater-than`() {
        val result = Interpreter().interpret(greaterThan(integer(5), integer(3)))
        assertTrue(result.isBool())
        assertTrue(result.asBool().value)
    }

    @Test
    fun `can evaluate greater-than-or-equal`() {
        val result = Interpreter().interpret(greaterThanEqual(integer(5), integer(5)))
        assertTrue(result.isBool())
        assertTrue(result.asBool().value)
    }

    @Test
    fun `can evaluate less-than-or-equal`() {
        val result = Interpreter().interpret(lessThanEqual(integer(3), integer(5)))
        assertTrue(result.isBool())
        assertTrue(result.asBool().value)
    }

    @Test
    fun `can evaluate equal for integers`() {
        val result = Interpreter().interpret(equal(integer(5), integer(5)))
        assertTrue(result.asBool().value)
    }

    @Test
    fun `can evaluate equal for booleans`() {
        val result = Interpreter().interpret(equal(bool(true), bool(true)))
        assertTrue(result.asBool().value)
    }

    @Test
    fun `can evaluate equal for strings`() {
        val result = Interpreter().interpret(equal(str("koy"), str("koy")))
        assertTrue(result.asBool().value)
    }

    @Test
    fun `can evaluate equal for nil`() {
        val result = Interpreter().interpret(equal(Nil, Nil))
        assertTrue(result.asBool().value)
    }

    @Test
    fun `can evaluate nil not equal to non-nil`() {
        val result = Interpreter().interpret(equal(Nil, integer(1)))
        assertEquals(false, result.asBool().value)
    }

    @Test
    fun `can evaluate not-equal for integers`() {
        val result = Interpreter().interpret(notEqual(integer(1), integer(2)))
        assertTrue(result.asBool().value)
    }

    @Test
    fun `should throw on equal with incompatible types`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(equal(integer(1), str("a")))
        }
    }

    @Test
    fun `should throw on add with incompatible types`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(add(integer(1), bool(true)))
        }
    }

    @Test
    fun `should throw on logical-and with non-boolean`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(logicalAnd(integer(1), integer(2)))
        }
    }

    @Test
    fun `should throw on logical-or with non-boolean`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(logicalOr(integer(1), integer(2)))
        }
    }

    // --- Nil literal ---

    @Test
    fun `can evaluate nil literal`() {
        val result = Interpreter().interpret(Nil)
        assertTrue(result.isNil())
    }

    // --- Control flow ---

    @Test
    fun `if without else returns true when condition is false`() {
        // Current behavior: elseClause.orElse(Value.of(true)) returns Bool(true)
        val result = Interpreter().interpret(If(bool(false), integer(1), Optional.empty()))
        assertTrue(result.isBool())
    }

    @Test
    fun `while body is never executed when condition is false from start`() {
        val interpreter = Interpreter()
        interpreter.interpret(MutableValDeclaration("i", integer(0)))
        interpreter.interpret(While(bool(false), assign("i", integer(99))))
        assertEquals(0, interpreter.getValue("i")?.asInt()?.value)
    }

    // --- IndexAccess ---

    @Test
    fun `can access array element by index`() {
        val result = Interpreter().interpret(
            IndexAccess(Array(integer(10), integer(20), integer(30)), integer(1))
        )
        assertEquals(20, result.asInt().value)
    }

    @Test
    fun `index access out of bounds returns nil`() {
        val result = Interpreter().interpret(
            IndexAccess(Array(integer(1), integer(2)), integer(99))
        )
        assertTrue(result.isNil())
    }

    // --- PushElement ---

    @Test
    fun `can push element to array`() {
        val result = Interpreter().interpret(
            PushElement(Array(integer(1), integer(2)), integer(3))
        ).asArray()
        assertEquals(3, result.items.size)
        assertEquals(Value.Int(3), result.items[2])
    }

    @Test
    fun `can push element to set`() {
        val result = Interpreter().interpret(
            PushElement(SetLiteral(setOf(integer(1), integer(2))), integer(3))
        ).asSet()
        assertEquals(3, result.value.size)
    }

    @Test
    fun `should throw on push to non-collection`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(PushElement(integer(1), integer(2)))
        }
    }

    // --- LabeledCall ---

    @Test
    fun `can call function with labeled parameters`() {
        val interpreter = Interpreter()
        val topLevels = listOf(
            // fn add(x, y) { x + y; }
            defineFunction("add", listOf("x", "y"), Block(add(identifier("x"), identifier("y")))),
            defineFunction("main", listOf(), Block(
                LabeledCall("add", listOf(
                    LabeledParameter("x", integer(3)),
                    LabeledParameter("y", integer(4))
                ))
            ))
        )
        val result = interpreter.callMain(Program(topLevels))
        assertEquals(7, result.asInt().value)
    }

    // --- MethodCall from inline ObjectLiteral ---

    @Test
    fun `can call method from inline object literal`() {
        val result = Interpreter().interpret(
            MethodCall(
                Object(mapOf("greet" to FunctionLiteral(listOf("msg"), Block(
                    add(str("Hello, "), identifier("msg"))
                )))),
                Identifier("greet"),
                listOf(str("Koy"))
            )
        )
        assertEquals("Hello, Koy", result.asString().value)
    }

    @Test
    fun `method call returns non-function property value`() {
        val interpreter = Interpreter()
        interpreter.interpret(ValDeclaration("obj", Object(mapOf("x" to integer(42)))))
        val result = interpreter.interpret(MethodCall(Identifier("obj"), Identifier("x"), listOf()))
        assertEquals(42, result.asInt().value)
    }

    @Test
    fun `should throw on method call for undefined object`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(MethodCall(Identifier("noSuchObj"), Identifier("method"), listOf()))
        }
    }

    @Test
    fun `should throw on method call for undefined method`() {
        val interpreter = Interpreter()
        interpreter.interpret(ValDeclaration("obj", Object(mapOf("x" to integer(1)))))
        assertThrows<KoyLangRuntimeException> {
            interpreter.interpret(MethodCall(Identifier("obj"), Identifier("noSuchMethod"), listOf()))
        }
    }

    // --- Assignment error cases ---

    @Test
    fun `should throw on assignment to undefined variable`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(assign("undeclared", integer(1)))
        }
    }

    @Test
    fun `should throw on reassignment to val`() {
        val interpreter = Interpreter()
        interpreter.interpret(ValDeclaration("x", integer(1)))
        assertThrows<KoyLangRuntimeException> {
            interpreter.interpret(assign("x", integer(2)))
        }
    }

    // --- MutableValDeclaration with function and object ---

    @Test
    fun `can declare mutable val as function literal`() {
        val interpreter = Interpreter()
        interpreter.interpret(
            MutableValDeclaration("f", FunctionLiteral(listOf("x"), Block(add(identifier("x"), integer(1)))))
        )
        assertEquals("f", interpreter.getFunction("f").name)
    }

    @Test
    fun `can declare mutable val as object`() {
        val interpreter = Interpreter()
        interpreter.interpret(
            MutableValDeclaration("obj", Object(mapOf("n" to integer(10))))
        )
        val value = interpreter.getValue("obj")?.asObject()?.value
        assertEquals(Value.Int(10), value?.get("n"))
    }

    // --- callMain with MutableValDefinition and ValDefinition ---

    @Test
    fun `callMain supports top-level mutable val definition`() {
        val topLevels = listOf(
            TopLevel.MutableValDefinition("count", integer(0)),
            defineFunction("main", listOf(), Block(identifier("count")))
        )
        val result = Interpreter().callMain(Program(topLevels))
        assertEquals(0, result.asInt().value)
    }

    @Test
    fun `callMain supports top-level mutable val as function literal`() {
        val topLevels = listOf(
            TopLevel.MutableValDefinition("double", FunctionLiteral(listOf("x"), Block(multiply(identifier("x"), integer(2))))),
            defineFunction("main", listOf(), Block(call("double", integer(5))))
        )
        val result = Interpreter().callMain(Program(topLevels))
        assertEquals(10, result.asInt().value)
    }

    @Test
    fun `callMain supports top-level mutable val as object`() {
        val topLevels = listOf(
            TopLevel.MutableValDefinition("cfg", Object(mapOf("value" to integer(99)))),
            defineFunction("main", listOf(), Block(identifier("cfg")))
        )
        val result = Interpreter().callMain(Program(topLevels))
        assertTrue(result.isObject())
    }

    @Test
    fun `callMain supports top-level val as plain value`() {
        val topLevels = listOf(
            TopLevel.ValDefinition("n", integer(42)),
            defineFunction("main", listOf(), Block(identifier("n")))
        )
        val result = Interpreter().callMain(Program(topLevels))
        assertEquals(42, result.asInt().value)
    }

    @Test
    fun `callMain supports top-level val as object`() {
        val topLevels = listOf(
            TopLevel.ValDefinition("cfg", Object(mapOf("key" to str("val")))),
            defineFunction("main", listOf(), Block(identifier("cfg")))
        )
        val result = Interpreter().callMain(Program(topLevels))
        assertTrue(result.isObject())
    }

    // --- Identifier not found ---

    @Test
    fun `should throw on undefined identifier`() {
        assertThrows<KoyLangRuntimeException> {
            Interpreter().interpret(identifier("notDefined"))
        }
    }

    // --- withDebug ---

    @Test
    fun `withDebug does not change evaluation result`() {
        val result = Interpreter().withDebug().interpret(add(integer(1), integer(2))).asInt().value
        assertEquals(3, result)
    }
}
