package io.github.simonnozaki.koy

import io.github.simonnozaki.koy.Expression.*
import org.javafp.data.Unit
import org.javafp.parsecj.Combinators
import org.javafp.parsecj.Parser
import org.javafp.parsecj.Text.intr
import org.javafp.parsecj.Text.regex
import org.javafp.parsecj.Text.string
import org.javafp.parsecj.Text.wspace
import java.util.function.BinaryOperator

// TODO property call by dot operator as method call
// TODO comment out
// TODO Lambda is compatible with higer kind function?
object Parsers {
    /**
     * 1st: alphabet + _
     * after 2nd: alphabet + number + _
     */
    private const val PATTERN_IDENTIFIER = "[a-zA-Z_][a-zA-Z0-9_]*"
    private const val PATTERN_STRING_LITERAL = "[a-zA-Z0-9][a-zA-Z0-9-_, ]*"

    private val SPACING: Parser<Char, Unit> = wspace.map { Unit.unit }.or(regex("(?m)//.*$").map { Unit.unit })
    private val SPACINGS: Parser<Char, Unit> = SPACING.many().map { Unit.unit }
    private val PLUS: Parser<Char, Unit> = string("+").then(SPACINGS)
    private val MINUS: Parser<Char, Unit> = string("-").then(SPACINGS)
    private val ASTER: Parser<Char, Unit> = string("*").then(SPACINGS)
    private val SLUSH: Parser<Char, Unit> = string("/").then(SPACINGS)
    private val EQEQ: Parser<Char, Unit> = string("==").then(SPACINGS)
    private val NE: Parser<Char, Unit> = string("!=").then(SPACINGS)
    private val LT: Parser<Char, Unit> = string("<").then(SPACINGS)
    private val LT_EQ: Parser<Char, Unit> = string("<=").then(SPACINGS)
    private val GT: Parser<Char, Unit> = string(">").then(SPACINGS)
    private val GT_EQ: Parser<Char, Unit> = string("=>").then(SPACINGS)
    private val GLOBAL: Parser<Char, Unit> = string("global").then(SPACINGS)
    private val FN: Parser<Char, Unit> = string("fn").then(SPACINGS)
    private val PRINTLN: Parser<Char, Unit> = string("println").then(SPACINGS)
    private val IF: Parser<Char, Unit> = string("if").then(SPACINGS)
    private val ELSE: Parser<Char, Unit> = string("else").then(SPACINGS)
    private val WHILE: Parser<Char, Unit> = string("while").then(SPACINGS)
    private val FOR: Parser<Char, Unit> = string("for").then(SPACINGS)
    private val IN: Parser<Char, Unit> = string("in").then(SPACINGS)
    private val TO: Parser<Char, Unit> = string("to").then(SPACINGS)
    private val TRUE: Parser<Char, Unit> = string("true").then(SPACINGS)
    private val FALSE: Parser<Char, Unit> = string("false").then(SPACINGS)
    private val COMMA: Parser<Char, Unit> = string(",").then(SPACINGS)
    private val COLON: Parser<Char, Unit> = string(":").then(SPACINGS)
    private val SEMI_COLON: Parser<Char, Unit> = string(";").then(SPACINGS)
    private val EQ: Parser<Char, Unit> = string("=").then(SPACINGS)
    private val LPAREN: Parser<Char, Unit> = string("(").then(SPACINGS)
    private val RPAREN: Parser<Char, Unit> = string(")").then(SPACINGS)
    private val LBRACE: Parser<Char, Unit> = string("{").then(SPACINGS)
    private val RBRACE: Parser<Char, Unit> = string("}").then(SPACINGS)
    private val LBRACKET: Parser<Char, Unit> = string("[").then(SPACINGS)
    private val RBRACKET: Parser<Char, Unit> = string("]").then(SPACINGS)
    private val D_QUOTE: Parser<Char, Unit> = string("\"").then(SPACINGS)
    private val PIPE: Parser<Char, Unit> = string("|").then(SPACINGS)
    private val IDENT: Parser<Char, String> = regex(PATTERN_IDENTIFIER).bind { name -> SPACINGS.map { name } }

    private val integer: Parser<Char, IntegerLiteral> = intr.map { integer(it) }.bind { v -> SPACINGS.map { v } }
    private val bool: Parser<Char, BoolLiteral> = TRUE.map { bool(true) }
        .or(FALSE.map { bool(false) })
        .bind { v -> SPACINGS.map { v } }
    private val string: Parser<Char, StringLiteral> = D_QUOTE.bind {
        regex(PATTERN_STRING_LITERAL).bind { v ->
            D_QUOTE.map { str(v) }
        }
    }.bind { v -> SPACINGS.map { v } }

    /**
     * # Array literal
     * ## PEG
     * ```
     * arrayLiteral <- '[' (expression(, expression)*)? ']'
     * ```
     * ## Sample syntax
     * ```
     * odd = [1, 3, 5];
     * ```
     */
    private fun arrayLiteral(): Parser<Char, ArrayLiteral> {
        return LBRACKET.bind {
            expression().sepBy(COMMA).bind { params ->
                RBRACKET.map { ArrayLiteral(params.toList()) }
            }
        }
    }

    /**
     * # Object literal
     * ## PEG
     * ```
     * objectLiteral <- '{' (identifier ':' expression (,identifier ':' expression*)? '}'
     * ```
     *
     * ## Sample syntax
     * ```
     * {
     *   name: "Koy",
     *   buildAt: "2022-09-25"
     * }
     * ```
     */
    // TODO check prop names duplication
    private fun objectLiteral(): Parser<Char, ObjectLiteral> {
        return IDENT.bind { propName ->
            COLON.then(expression()).map { e -> propName to e }
        }
            .sepBy(COMMA)
            .between(LBRACE, RBRACE)
            .map {
                val properties = it.toList().associate { p -> p.first to p.second }
                ObjectLiteral(properties)
            }
    }

    /**
     * # Function literal
     *
     * ## PEG
     * ```
     * functionLiteral <- '|' (identifier(, identifier)*)? '|' blockExpression
     * ```
     * ## Sample syntax
     * ```ky
     * |x,y| {
     *   x + y;
     * }
     * ```
     */
    fun functionLiteral(): Parser<Char, FunctionLiteral> {
        return IDENT.sepBy(COMMA).between(PIPE, PIPE).bind { params ->
            blockExpression().map { block ->
                FunctionLiteral(params.toList(), block)
            }
        }
    }

    /**
     * ```
     * println <- println '(' expression ')' ';'
     * ```
     */
    private fun println(): Parser<Char, Expression> {
        return PRINTLN.bind {
            expression().between(LPAREN, RPAREN).bind { param ->
                SEMI_COLON.map { PrintLn(param) as Expression }
            }
        }.attempt()
    }

    /**
     * # Line definition
     * The unit that is equal to one line. Expressions are all as one line.
     * ## PEG
     * ```
     * line <- println
     *   / ifExpression
     *   / whileExpression
     *   / forInExpression
     *   / blockExpression
     *   / assignment
     *   / expressionLine
     * ```
     */
    fun line(): Parser<Char, Expression> {
        return println()
            .or(assignment())
            .or(blockExpression())
            .or(ifExpression())
            .or(forInExpression())
            .or(whileExpression())
            .or(expressionLine())
    }

    /**
     * ```
     * lines <- line+;
     * ```
     */
    fun lines(): Parser<Char, List<Expression>> {
        return line().many1().bind { s ->
            Combinators.eof<Char>().map {
                s.toList()
            }
        }
    }

    /**
     * ```
     * assignment <- identifier '=' expression ';'
     * ```
     */
    private fun assignment(): Parser<Char, Assignment> {
        return IDENT.bind { name ->
            EQ.then(expression()).bind { expr ->
                SEMI_COLON.map { Assignment(name, expr) }
            }
        }.attempt()
    }

    /**
     * `expressionLine` can accept semicolon and new line as symbol for one line.
     * ```
     * expressionLine <- expression ';' / '\n'
     * ```
     */
    private fun expressionLine(): Parser<Char, Expression> {
        return expression().bind { e -> SEMI_COLON.map { e } }.attempt()
    }

    /**
     * # Function call
     * ## PEG
     * ```
     * functionCall <- identifier '(' (expression (',' expression)*)? ')'
     * ```
     *
     * ## Sample syntax
     * ```
     * factorial(5)
     * ```
     */
    private fun functionCall(): Parser<Char, FunctionCall> {
        return IDENT.bind { identifier ->
            expression().sepBy(COMMA).between(LPAREN, RPAREN).map {
                FunctionCall(identifier, it.toList())
            }
        }.attempt()
    }

    /**
     * ```
     * labeledParameter <- identifier '=' expression;
     * labeledCall <- identifier '[' (labeledParameter(',' labeledParameter)*)? ']'
     * ```
     */
    private fun labeledCall(): Parser<Char, LabeledCall> {
        return IDENT.bind { name ->
            IDENT.bind { label ->
                EQ.then(expression()).map { param ->
                    LabeledParameter(label, param)
                }
            }
                .sepBy(COMMA)
                .between(LBRACKET, RBRACKET)
                .map { LabeledCall(name, it.toList()) }
        }.attempt()
    }

    /**
     * ```
     * topLevelDefinition <- globalVariableDefinition / functionDefinition
     * ```
     */
    private fun topLevelDefinition(): Parser<Char, TopLevel> {
        return globalVariableDefinition().map { it as TopLevel }.or(functionDefinition().map { it as TopLevel })
    }

    /**
     * ```
     * functionDefinition <-
     *   'fn' identifier '(' (identifier (',' identifier)*)? ')' blockExpression;
     * ```
     */
    private fun functionDefinition(): Parser<Char, FunctionDefinition> {
        val defName = FN.then(IDENT)
        val defArgs = IDENT.sepBy(COMMA).between(LPAREN, RPAREN)

        return defName.bind { name ->
            defArgs.bind { args ->
                blockExpression().map { body -> FunctionDefinition(name, args.toList(), body) }
            }
        }
    }

    /**
     * ```
     * globalVariableDefinition <-
     *   'global' identifier '=' expression;
     * ```
     */
    private fun globalVariableDefinition(): Parser<Char, GlobalVariableDefinition> {
        val defGlobal = GLOBAL.then(IDENT)
        val defInitializer = EQ.then(expression())
        return defGlobal.bind { name ->
            defInitializer.bind { expression ->
                SEMI_COLON.map { GlobalVariableDefinition(name, expression) }
            }
        }
    }

    /**
     * ```
     * program <- topLevelDefinition
     * ```
     */
    fun program(): Parser<Char, Program> {
        return SPACINGS.bind {
            topLevelDefinition()
                .many()
                .map { it.toList() }
                .map { Program(it) }
        }
    }

    /**
     * ```
     * expression <- comparative
     * ```
     */
    fun expression(): Parser<Char, Expression> = comparative()

    /**
     * comparative <- addictive (
     *   ('==') / (!=') / ('<') / ('<=') / ('>') / ('=>') addictive
     * )*;
     */
    private fun comparative(): Parser<Char, Expression> {
        val eqeq: Parser<Char, BinaryOperator<Expression>> = EQEQ.attempt().map { BinaryOperator { l, r -> equal(l, r) } }
        val ne: Parser<Char, BinaryOperator<Expression>> = NE.attempt().map { BinaryOperator { l, r -> notEqual(l, r) } }
        val gt: Parser<Char, BinaryOperator<Expression>> = GT.attempt().map { BinaryOperator { l, r -> greaterThan(l, r) } }
        val gtEq: Parser<Char, BinaryOperator<Expression>> = GT_EQ.attempt().map { BinaryOperator { l, r -> greaterThanEqual(l, r) } }
        val lt: Parser<Char, BinaryOperator<Expression>> = LT.attempt().map { BinaryOperator { l, r -> lessThan(l, r) } }
        val ltEq: Parser<Char, BinaryOperator<Expression>> = LT_EQ.attempt().map { BinaryOperator { l, r -> lessThanEqual(l, r) } }

        return addictive().chainl1(
            lt.or(ltEq).or(gt).or(gtEq).or(eqeq).or(ne)
        )
    }

    /**
     * ```
     * addictive <- multitive
     *   '(+' multitive / '-' multitive)*;
     * ```
     */
    private fun addictive(): Parser<Char, Expression> {
        val addition: Parser<Char, BinaryOperator<Expression>> = PLUS.map {
            BinaryOperator { left: Expression, right: Expression -> add(left, right) }
        }
        val subtract: Parser<Char, BinaryOperator<Expression>> = MINUS.map {
            BinaryOperator { left: Expression, right: Expression -> subtract(left, right) }
        }
        return multitive().chainl1(addition.or(subtract))
    }

    /**
     * ```
     * multitive <- primary
     *   '(+' primary / '-' primary)*;
     * ```
     */
    private fun multitive(): Parser<Char, Expression> {
        val multiply: Parser<Char, BinaryOperator<Expression>> = ASTER.map {
            BinaryOperator { l, r -> multiply(l, r) }
        }
        val divide: Parser<Char, BinaryOperator<Expression>> = SLUSH.map {
            BinaryOperator { l, r -> divide(l, r) }
        }
        return primary().chainl1(multiply.or(divide))
    }

    /**
     * ```
     * primary <- '(' expression ')'
     *   / integer
     *   / bool
     *   / string
     *   / functionCall
     *   / labeledCall
     *   / identifier
     *   / arrayLiteral
     *   / objectLiteral
     *   / functionLiteral
     * ```
     */
    private fun primary(): Parser<Char, Expression> {
        return LPAREN.bind {
            expression().bind { v ->
                RPAREN.map { v }
            }
        }
            .or(integer)
            .or(bool)
            .or(string)
            .or(functionCall())
            .or(labeledCall())
            .or(identifier())
            .or(arrayLiteral())
            .or(functionLiteral())
            .or(objectLiteral())
    }

    /**
     * ```
     * blockExpression <- '{' expression '}'
     * ```
     */
    fun blockExpression(): Parser<Char, BlockExpression> {
        return LBRACE.bind { line().many().bind { expressions -> RBRACE.map { BlockExpression(expressions.toList()) } } }
    }

    /**
     * ```
     * ifExpression <- 'if' '(' expression ')' line ('else' line)?;
     * ```
     */
    private fun ifExpression(): Parser<Char, IfExpression> {
        val condition = IF.then(expression().between(LPAREN, RPAREN))
        return condition.bind { c ->
            line().bind { thenClause ->
                ELSE.then(line()).optionalOpt().map { elseClause -> IfExpression(c, thenClause, elseClause.get()) }
            }
        }.attempt()
    }

    /**
     * ```
     * forInExpression <- "for" "(" identifier "in" integer "to" integer ")" line
     * ```
     */
    private fun forInExpression(): Parser<Char, Expression> {
        return FOR.then(
            LPAREN.then(IDENT).bind { name ->
                IN.then(expression()).bind { from ->
                    TO.then(expression()).bind { to ->
                        RPAREN.then(line()).map {
                            Block(
                                assign(name, from),
                                While(
                                    lessThan(identifier(name), to),
                                    Block(
                                        it,
                                        assign(name, add(identifier(name), integer(1)))
                                    )
                                )
                            )
                        }
                    }
                }
            }
        )
    }

    /**
     * ```
     * whileExpression <- 'while' '(' expression ')' line
     * ```
     */
    private fun whileExpression(): Parser<Char, WhileExpression> {
        val condition = WHILE.then(expression().between(LPAREN, RPAREN))
        return condition.bind { c ->
            line().map { body -> WhileExpression(c, body) }
        }.attempt()
    }

    /**
     * ```
     * identifier <- Identifier
     * ```
     */
    private fun identifier(): Parser<Char, Identifier> {
        return IDENT.map { Identifier(it) }
    }
}
