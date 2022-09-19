package io.github.simonnozaki.koy

import org.javafp.data.Unit
import org.javafp.parsecj.Parser
import org.javafp.parsecj.Text.*
import java.util.function.BinaryOperator
import io.github.simonnozaki.koy.Expression.*
import org.javafp.parsecj.Combinators

/**
 * Syntax Parser
 */
object Parsers {
    private val PATTERN_IDENTIFIER = "[a-zA-Z_][a-zA-Z0-9_]*"

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
    private val DEFINE: Parser<Char, Unit> = string("fn").then(SPACINGS)
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
    private val SEMI_COLON: Parser<Char, Unit> = string(";").then(SPACINGS)
    private val EQ: Parser<Char, Unit> = string("=").then(SPACINGS)
    private val LPAREN: Parser<Char, Unit> = string("(").then(SPACINGS)
    private val RPAREN: Parser<Char, Unit> = string(")").then(SPACINGS)
    private val LBRACE: Parser<Char, Unit> = string("{").then(SPACINGS)
    private val RBRACE: Parser<Char, Unit> = string("}").then(SPACINGS)
    private val LBRACKET: Parser<Char, Unit> = string("[").then(SPACINGS)
    private val RBRACKET: Parser<Char, Unit> = string("]").then(SPACINGS)
    private val IDENT: Parser<Char, String> = regex(PATTERN_IDENTIFIER).bind { name -> SPACINGS.map { name } }

    private val integer: Parser<Char, IntegerLiteral> = intr.map { integer(it) }.bind { v ->  SPACINGS.map { v } }
    private val bool: Parser<Char, BoolLiteral> = TRUE.map { boolLiteral(true) }
        .or(FALSE.map { boolLiteral(false) })

    /**
     * arrayLiteral <- '[' (expression(, expression)*)? ']'
     */
    private fun arrayLiteral(): Parser<Char, ArrayLiteral> {
        return LBRACKET.bind {
            expression().sepBy(COMMA).bind { params ->
                RBRACKET.map { ArrayLiteral(params.toList()) }
            }
        }
    }

    /**
     * ```
     * println <- println '(' expression ')'
     * ```
     */
    fun println(): Parser<Char, Expression> {
        return PRINTLN.bind {
            expression().between(LPAREN, RPAREN).bind { param ->
                SEMI_COLON.map { PrintLn(param) as Expression }
            }
        }.attempt()
    }

    /**
     * 行の定義、1行とカウントされる式の単位
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
    private fun line(): Parser<Char, Expression> {
        return println()
            .or(blockExpression())
            .or(ifExpression())
            .or(forInExpression())
            .or(whileExpression())
            .or(expressionLine())
            .or(assignment())
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
     * expressionLine <- expression ';'
     */
    private fun expressionLine(): Parser<Char, Expression> {
        return expression().bind { e -> SEMI_COLON.map { e } }.attempt()
    }

    /**
     * 関数呼び出し
     * `(expression (',' expression)*)?` で複数の式が引数としてマッチする
     * ```
     * functionCall <- identifier '(' (expression (',' expression)*)? ')'
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
     * functionDefinition <-
     *   'define' identifier '(' (identifier (',' identifier)*)? ')' blockExpression;
     */
    private fun functionDefinition(): Parser<Char, FunctionDefinition> {
        val defName = DEFINE.then(IDENT)
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
        val eqeq: Parser<Char, BinaryOperator<Expression>> = EQEQ.attempt().map { BinaryOperator {l, r -> equal(l, r) } }
        val ne: Parser<Char, BinaryOperator<Expression>> = NE.attempt().map { BinaryOperator {l, r -> notEqual(l, r) } }
        val gt: Parser<Char, BinaryOperator<Expression>> = GT.attempt().map { BinaryOperator {l, r -> greaterThan(l, r) } }
        val gtEq: Parser<Char, BinaryOperator<Expression>> = GT_EQ.attempt().map { BinaryOperator {l, r -> greaterThanEqual(l, r) } }
        val lt: Parser<Char, BinaryOperator<Expression>> = LT.attempt().map { BinaryOperator {l, r -> lessThan(l, r) } }
        val ltEq: Parser<Char, BinaryOperator<Expression>> = LT_EQ.attempt().map { BinaryOperator {l, r -> lessThanEqual(l, r) } }

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
     *   / functionCall
     *   / labeledCall
     *   / identifier
     * ```
     */
    private fun primary(): Parser<Char, Expression> {
        return LPAREN.bind {
                expression().bind { v->  RPAREN.map { v }
            }
        }
            .or(integer)
            .or(bool)
            .or(functionCall())
            .or(labeledCall())
            .or(identifier())
            .or(arrayLiteral())
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