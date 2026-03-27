package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import kotlin.test.assertEquals

/**
 * Interpreter specs
 */
class InterpreterTests {
    // --- Arithmetic ---

    @Nested
    inner class `when evaluating arithmetic expressions` {
        @Test
        fun `should add two integers`() {
            val result = Interpreter().interpret(add(IntegerLiteral(10), IntegerLiteral(20))).asInt().value
            assertEquals(30, result)
        }

        @Test
        fun `should evaluate nested add and multiply`() {
            val result =
                Interpreter()
                    .interpret(
                        add(multiply(IntegerLiteral(3), IntegerLiteral(5)), IntegerLiteral(10)),
                    ).asInt()
                    .value
            assertEquals(25, result)
        }

        @Test
        fun `should divide two integers`() {
            val result = Interpreter().interpret(divide(integer(10), integer(2))).asInt().value
            assertEquals(5, result)
        }

        @Test
        fun `should compute remainder`() {
            val result = Interpreter().interpret(remain(integer(10), integer(3))).asInt().value
            assertEquals(1, result)
        }

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
        fun `should throw on add with incompatible types`() {
            assertThrows<KoyLangRuntimeException> {
                Interpreter().interpret(add(integer(1), bool(true)))
            }
        }
    }

    // --- Comparison ---

    @Nested
    inner class `when evaluating comparison expressions` {
        @Test
        fun `should return true for greater-than`() {
            val result = Interpreter().interpret(greaterThan(integer(5), integer(3)))
            assertTrue(result.isBool())
            assertTrue(result.asBool().value)
        }

        @Test
        fun `should return true for greater-than-or-equal`() {
            val result = Interpreter().interpret(greaterThanEqual(integer(5), integer(5)))
            assertTrue(result.isBool())
            assertTrue(result.asBool().value)
        }

        @Test
        fun `should return true for less-than-or-equal`() {
            val result = Interpreter().interpret(lessThanEqual(integer(3), integer(5)))
            assertTrue(result.isBool())
            assertTrue(result.asBool().value)
        }

        @Test
        fun `should return true for equal integers`() {
            assertTrue(Interpreter().interpret(equal(integer(5), integer(5))).asBool().value)
        }

        @Test
        fun `should return true for equal booleans`() {
            assertTrue(Interpreter().interpret(equal(bool(true), bool(true))).asBool().value)
        }

        @Test
        fun `should return true for equal strings`() {
            assertTrue(Interpreter().interpret(equal(str("koy"), str("koy"))).asBool().value)
        }

        @Test
        fun `should return true for equal nil`() {
            assertTrue(Interpreter().interpret(equal(Nil, Nil)).asBool().value)
        }

        @Test
        fun `should return false for nil compared to non-nil`() {
            assertEquals(false, Interpreter().interpret(equal(Nil, integer(1))).asBool().value)
        }

        @Test
        fun `should return true for not-equal integers`() {
            assertTrue(Interpreter().interpret(notEqual(integer(1), integer(2))).asBool().value)
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

        @Test
        fun `should throw on equal with incompatible types`() {
            assertThrows<KoyLangRuntimeException> {
                Interpreter().interpret(equal(integer(1), str("a")))
            }
        }
    }

    // --- Logical ---

    @Nested
    inner class `when evaluating logical expressions` {
        @Test
        fun `should return true for logical-and of two trues`() {
            val result =
                Interpreter().interpret(
                    BinaryExpression(Operator.LOGICAL_AND, BoolLiteral(true), BoolLiteral(true)),
                )
            assertTrue(result.isBool())
            assertTrue(result.asBool().value)
        }

        @Test
        fun `should return true for logical-or of true and false`() {
            val result =
                Interpreter().interpret(
                    BinaryExpression(Operator.LOGICAL_OR, BoolLiteral(true), BoolLiteral(false)),
                )
            assertTrue(result.isBool())
            assertTrue(result.asBool().value)
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
    }

    // --- Nil ---

    @Nested
    inner class `when evaluating nil literal` {
        @Test
        fun `should return nil value`() {
            assertTrue(Interpreter().interpret(Nil).isNil())
        }
    }

    // --- Control flow ---

    @Nested
    inner class `when evaluating control flow` {
        @Test
        fun `should return true when if-without-else and condition is false`() {
            val result = Interpreter().interpret(If(bool(false), integer(1), Optional.empty()))
            assertTrue(result.isBool())
        }

        @Test
        fun `should not execute while body when condition is initially false`() {
            val interpreter = Interpreter()
            interpreter.interpret(MutableValDeclaration("i", integer(0)))
            interpreter.interpret(While(bool(false), assign("i", integer(99))))
            assertEquals(0, interpreter.getValue("i")?.asInt()?.value)
        }

        @Test
        fun `should increment variable in while loop`() {
            val interpreter = Interpreter()
            interpreter.interpret(MutableValDeclaration("i", IntegerLiteral(0)))
            interpreter.interpret(
                While(
                    BinaryExpression(Operator.LESS_THAN, identifier("i"), IntegerLiteral(10)),
                    assign("i", BinaryExpression(Operator.ADD, identifier("i"), IntegerLiteral(1))),
                ),
            )
            assertEquals(10, interpreter.getValue("i")?.asInt()?.value)
        }
    }

    // --- Collections ---

    @Nested
    inner class `when evaluating collections` {
        @Test
        fun `should define array literal with correct size`() {
            val interpreter = Interpreter()
            interpreter.interpret(ValDeclaration("a", Array(IntegerLiteral(1), IntegerLiteral(3), IntegerLiteral(5))))
            assertEquals(
                3,
                interpreter
                    .getValue("a")
                    ?.asArray()
                    ?.items
                    ?.size,
            )
        }

        @Test
        fun `should initialize set literal`() {
            val result =
                Interpreter().interpret(
                    SetLiteral(setOf(StringLiteral("Java"), StringLiteral("Kotlin"), StringLiteral("Koy"))),
                )
            assertTrue(result.isSet())
            assertTrue(result.asSet().value.containsAll(listOf(Value.String("Java"), Value.String("Kotlin"), Value.String("Koy"))))
        }

        @Test
        fun `should access array element by index`() {
            val result =
                Interpreter().interpret(
                    IndexAccess(Array(integer(10), integer(20), integer(30)), integer(1)),
                )
            assertEquals(20, result.asInt().value)
        }

        @Test
        fun `should return nil on out-of-bounds index access`() {
            val result =
                Interpreter().interpret(
                    IndexAccess(Array(integer(1), integer(2)), integer(99)),
                )
            assertTrue(result.isNil())
        }

        @Test
        fun `should push element to array`() {
            val result =
                Interpreter()
                    .interpret(
                        PushElement(Array(integer(1), integer(2)), integer(3)),
                    ).asArray()
            assertEquals(3, result.items.size)
            assertEquals(Value.Int(3), result.items[2])
        }

        @Test
        fun `should push element to set`() {
            val result =
                Interpreter()
                    .interpret(
                        PushElement(SetLiteral(setOf(integer(1), integer(2))), integer(3)),
                    ).asSet()
            assertEquals(3, result.value.size)
        }

        @Test
        fun `should throw on push to non-collection`() {
            assertThrows<KoyLangRuntimeException> {
                Interpreter().interpret(PushElement(integer(1), integer(2)))
            }
        }
    }

    // --- Objects ---

    @Nested
    inner class `when evaluating objects` {
        @Test
        fun `should evaluate object literal properties`() {
            val interpreter = Interpreter()
            interpreter.interpret(
                ValDeclaration("o", Object(mapOf("a" to integer(1), "b" to str("1")))),
            )
            val o = interpreter.getValue("o")?.asObject()?.value
            assertEquals(1, o?.get("a")?.asInt()?.value)
            assertEquals("1", o?.get("b")?.asString()?.value)
        }

        @Test
        fun `should call method on object`() {
            val interpreter = Interpreter()
            listOf(
                ValDeclaration(
                    "object",
                    Object(
                        mapOf(
                            "print" to
                                FunctionLiteral(
                                    listOf("msg"),
                                    Block(BinaryExpression(Operator.ADD, StringLiteral("Hello, "), Identifier("msg"))),
                                ),
                        ),
                    ),
                ),
                ValDeclaration("r", MethodCall(Identifier("object"), Identifier("print"), listOf(StringLiteral("Koy")))),
            ).forEach { interpreter.interpret(it) }
            assertEquals("Hello, Koy", interpreter.getValue("r")?.asString()?.value)
        }

        @Test
        fun `should call method from inline object literal`() {
            val result =
                Interpreter().interpret(
                    MethodCall(
                        Object(
                            mapOf(
                                "greet" to
                                    FunctionLiteral(
                                        listOf("msg"),
                                        Block(
                                            add(str("Hello, "), identifier("msg")),
                                        ),
                                    ),
                            ),
                        ),
                        Identifier("greet"),
                        listOf(str("Koy")),
                    ),
                )
            assertEquals("Hello, Koy", result.asString().value)
        }

        @Test
        fun `should return non-function property value via method call`() {
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
    }

    // --- Functions ---

    @Nested
    inner class `when evaluating functions` {
        @Test
        fun `should evaluate main function calling another function`() {
            val topLevels =
                listOf(
                    defineFunction("main", listOf(), Block(Println(call("add", integer(10), integer(20))))),
                    defineFunction("add", listOf("v1", "v2"), Block(add(identifier("v1"), identifier("v2")))),
                )
            assertEquals(30, Interpreter().callMain(Program(topLevels)).asInt().value)
        }

        @Test
        fun `should evaluate recursive factorial`() {
            val topLevels =
                listOf(
                    defineFunction(
                        "factorial",
                        listOf("v"),
                        Block(
                            If(
                                lessThan(identifier("v"), integer(2)),
                                integer(1),
                                Optional.of(
                                    multiply(
                                        call("factorial", subtract(identifier("v"), integer(1))),
                                        identifier("v"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    defineFunction("main", listOf(), Block(Println(call("factorial", integer(5))))),
                )
            assertEquals(120, Interpreter().callMain(Program(topLevels)).asInt().value)
        }

        @Test
        fun `should define function literal`() {
            val interpreter = Interpreter()
            interpreter.interpret(
                ValDeclaration(
                    "f",
                    FunctionLiteral(
                        listOf("x", "y"),
                        BlockExpression(listOf(add(identifier("x"), identifier("y")))),
                    ),
                ),
            )
            val f = interpreter.getFunction("f")
            assertEquals("f", f.name)
            assertTrue(f.args.containsAll(listOf("x", "y")))
        }

        @Test
        fun `should increment and decrement`() {
            val interpreter = Interpreter()
            listOf(
                MutableValDeclaration("n", integer(1)),
                ValDeclaration("n2", increment("n")),
                MutableValDeclaration("m", integer(1)),
                ValDeclaration("m2", decrement("m")),
            ).forEach { interpreter.interpret(it) }
            assertEquals(2, interpreter.getValue("n2")?.asInt()?.value)
            assertEquals(0, interpreter.getValue("m2")?.asInt()?.value)
        }

        @Test
        fun `should call function with labeled parameters`() {
            val topLevels =
                listOf(
                    defineFunction("add", listOf("x", "y"), Block(add(identifier("x"), identifier("y")))),
                    defineFunction(
                        "main",
                        listOf(),
                        Block(
                            LabeledCall(
                                "add",
                                listOf(
                                    LabeledParameter("x", integer(3)),
                                    LabeledParameter("y", integer(4)),
                                ),
                            ),
                        ),
                    ),
                )
            assertEquals(7, Interpreter().callMain(Program(topLevels)).asInt().value)
        }

        @Test
        fun `should throw on function call with too few args`() {
            val topLevels =
                listOf(
                    defineFunction("add", listOf("x", "y"), Block(add(identifier("x"), identifier("y")))),
                    defineFunction("main", listOf(), Block(call("add", integer(1)))),
                )
            assertThrows<KoyLangRuntimeException> {
                Interpreter().callMain(Program(topLevels))
            }
        }

        @Test
        fun `should throw on function call with too many args`() {
            val topLevels =
                listOf(
                    defineFunction("add", listOf("x", "y"), Block(add(identifier("x"), identifier("y")))),
                    defineFunction("main", listOf(), Block(call("add", integer(1), integer(2), integer(3)))),
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
                    Object(mapOf("add" to FunctionLiteral(listOf("x", "y"), Block(add(identifier("x"), identifier("y")))))),
                ),
            )
            assertThrows<KoyLangRuntimeException> {
                interpreter.interpret(MethodCall(Identifier("obj"), Identifier("add"), listOf(integer(1))))
            }
        }

        @Test
        fun `should throw on method call with too many args`() {
            val interpreter = Interpreter()
            interpreter.interpret(
                ValDeclaration(
                    "obj",
                    Object(mapOf("add" to FunctionLiteral(listOf("x", "y"), Block(add(identifier("x"), identifier("y")))))),
                ),
            )
            assertThrows<KoyLangRuntimeException> {
                interpreter.interpret(MethodCall(Identifier("obj"), Identifier("add"), listOf(integer(1), integer(2), integer(3))))
            }
        }
    }

    // --- Variable declarations ---

    @Nested
    inner class `when evaluating variable declarations` {
        @Test
        fun `should declare mutable val as function literal`() {
            val interpreter = Interpreter()
            interpreter.interpret(
                MutableValDeclaration("f", FunctionLiteral(listOf("x"), Block(add(identifier("x"), integer(1))))),
            )
            assertEquals("f", interpreter.getFunction("f").name)
        }

        @Test
        fun `should declare mutable val as object`() {
            val interpreter = Interpreter()
            interpreter.interpret(MutableValDeclaration("obj", Object(mapOf("n" to integer(10)))))
            assertEquals(
                Value.Int(10),
                interpreter
                    .getValue("obj")
                    ?.asObject()
                    ?.value
                    ?.get("n"),
            )
        }

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

        @Test
        fun `should throw on undefined identifier`() {
            assertThrows<KoyLangRuntimeException> {
                Interpreter().interpret(identifier("notDefined"))
            }
        }
    }

    // --- callMain with top-level definitions ---

    @Nested
    inner class `when calling main with top-level definitions` {
        @Test
        fun `should support top-level mutable val definition`() {
            val topLevels =
                listOf(
                    TopLevel.MutableValDefinition("count", integer(0)),
                    defineFunction("main", listOf(), Block(identifier("count"))),
                )
            assertEquals(0, Interpreter().callMain(Program(topLevels)).asInt().value)
        }

        @Test
        fun `should support top-level mutable val as function literal`() {
            val topLevels =
                listOf(
                    TopLevel.MutableValDefinition("double", FunctionLiteral(listOf("x"), Block(multiply(identifier("x"), integer(2))))),
                    defineFunction("main", listOf(), Block(call("double", integer(5)))),
                )
            assertEquals(10, Interpreter().callMain(Program(topLevels)).asInt().value)
        }

        @Test
        fun `should support top-level mutable val as object`() {
            val topLevels =
                listOf(
                    TopLevel.MutableValDefinition("cfg", Object(mapOf("value" to integer(99)))),
                    defineFunction("main", listOf(), Block(identifier("cfg"))),
                )
            assertTrue(Interpreter().callMain(Program(topLevels)).isObject())
        }

        @Test
        fun `should support top-level val as plain value`() {
            val topLevels =
                listOf(
                    TopLevel.ValDefinition("n", integer(42)),
                    defineFunction("main", listOf(), Block(identifier("n"))),
                )
            assertEquals(42, Interpreter().callMain(Program(topLevels)).asInt().value)
        }

        @Test
        fun `should support top-level val as object`() {
            val topLevels =
                listOf(
                    TopLevel.ValDefinition("cfg", Object(mapOf("key" to str("val")))),
                    defineFunction("main", listOf(), Block(identifier("cfg"))),
                )
            assertTrue(Interpreter().callMain(Program(topLevels)).isObject())
        }
    }

    // --- Debug ---

    @Nested
    inner class `when debug mode is enabled` {
        @Test
        fun `should not change evaluation result`() {
            val result = Interpreter()
                .withDebug()
                .interpret(add(integer(1), integer(2)))
                .asInt()
                .value
            assertEquals(3, result)
        }
    }
}
