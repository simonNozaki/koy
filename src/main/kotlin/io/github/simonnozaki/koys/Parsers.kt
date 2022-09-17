package io.github.simonnozaki.koys

import org.javafp.data.Unit
import org.javafp.parsecj.Parser
import org.javafp.parsecj.Text.*
import java.util.function.BinaryOperator
import io.github.simonnozaki.koys.Expression.*


object Parsers {
    val SPACING: Parser<Char, Unit> = wspace.map { Unit.unit }.or(regex("(?m)//.*$").map { Unit.unit })
    val SPACINGS: Parser<Char, Unit> = SPACING.many().map { Unit.unit }
    val PLUS: Parser<Char, Unit> = string("+").then(SPACINGS)
    val MINUS: Parser<Char, Unit> = string("-").then(SPACINGS)
    val ASTER: Parser<Char, Unit> = string("*").then(SPACINGS)
    val SLUSH: Parser<Char, Unit> = string("/").then(SPACINGS)
    val EQEQ: Parser<Char, Unit> = string("==").then(SPACINGS)
    val NE: Parser<Char, Unit> = string("!=").then(SPACINGS)
    val LT: Parser<Char, Unit> = string("<").then(SPACINGS)
    val LT_EQ: Parser<Char, Unit> = string("<=").then(SPACINGS)
    val GT: Parser<Char, Unit> = string(">").then(SPACINGS)
    val GT_EQ: Parser<Char, Unit> = string("=>").then(SPACINGS)
    val GLOBAL: Parser<Char, Unit> = string("global").then(SPACINGS)
    val DEFINE: Parser<Char, Unit> = string("define").then(SPACINGS)
    val PRINTLN: Parser<Char, Unit> = string("println").then(SPACINGS)
    val IF: Parser<Char, Unit> = string("if").then(SPACINGS)
    val ELSE: Parser<Char, Unit> = string("else").then(SPACINGS)
    val WHILE: Parser<Char, Unit> = string("while").then(SPACINGS)
    val COMMA: Parser<Char, Unit> = string(",").then(SPACINGS)
    val SEMI_COLON: Parser<Char, Unit> = string(";").then(SPACINGS)
    val EQ: Parser<Char, Unit> = string("=").then(SPACINGS)
    val LPAREN = string("(").then(SPACINGS)
    val RPAREN = string(")").then(SPACINGS)
    val LBRACE = string("{").then(SPACINGS)
    val RBRACE = string("}").then(SPACINGS)
    val IDENT: Parser<Char, String> = regex("[a-zA-Z_][a-zA-Z0-9_]*").bind { name -> SPACINGS.map { name } }

    val integer = intr.map { integer(it) }.bind { v ->  SPACINGS.map { v } }

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
     * line <- println / ifExpression / whileExpression / blockExpression / assignment
     * ```
     */
    fun line(): Parser<Char, Expression> {
        return println().or(blockExpression())
    }

    /**
     * ```
     * assignment <- identifier '=' expression ';'
     * ```
     */
    fun assignment(): Parser<Char, Assignment> {
        return IDENT.bind { name ->
            EQ.then(expression()).bind { expr ->
                SEMI_COLON.map { Assignment(name, expr) }
            }
        }.attempt()
    }

    /**
     * expressionLine <- expression ';'
     */
    fun expressionLine(): Parser<Char, Expression> {
        return expression().bind { e -> SEMI_COLON.map { e } }.attempt()
    }

    /**
     * ```
     * topLevelDefinition <- globalVariableDefinition / functionDefinition
     * ```
     */
    fun topLevelDefinition(): Parser<Char, TopLevel> {
        return globalVariableDefinition().map { it as TopLevel }.or(functionDefinition().map { it as TopLevel })
    }

    /**
     * functionDefinition <-
     *   'define' identifier '(' (identifier (',' identifier)*)? ')' blockExpression;
     */
    fun functionDefinition(): Parser<Char, FunctionDefinition> {
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
    fun globalVariableDefinition(): Parser<Char, GlobalVariableDefinition> {
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
    fun expression(): Parser<Char, Expression> = addictive()

    /**
     * comparative <- addictive (
     *   ('==') / (!=') / ('<') / ('<=') / ('>') / ('=>') addictive
     * )*;
     */
    fun comparative(): Parser<Char, Expression> {
        val eqeq: Parser<Char, BinaryOperator<Expression>> = LT.attempt().map { BinaryOperator {l, r -> equal(l, r) } }
        val ne: Parser<Char, BinaryOperator<Expression>> = LT.attempt().map { BinaryOperator {l, r -> notEqual(l, r) } }
        val gt: Parser<Char, BinaryOperator<Expression>> = LT.attempt().map { BinaryOperator {l, r -> greaterThan(l, r) } }
        val gtEq: Parser<Char, BinaryOperator<Expression>> = LT.attempt().map { BinaryOperator {l, r -> greaterThanEqual(l, r) } }
        val lt: Parser<Char, BinaryOperator<Expression>> = LT.attempt().map { BinaryOperator {l, r -> lessThan(l, r) } }
        val ltEq: Parser<Char, BinaryOperator<Expression>> = LT.attempt().map { BinaryOperator {l, r -> lessThanEqual(l, r) } }

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
    fun addictive(): Parser<Char, Expression> {
        val func2 = BinaryOperator { left: Expression, right: Expression -> add(left, right) }
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
    fun multitive(): Parser<Char, Expression> {
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
     * ```
     */
    fun primary(): Parser<Char, Expression> {
        return LPAREN.bind {
                expression().bind { v->  RPAREN.map { v }
            }
        }.or(integer)
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
    fun ifExpression(): Parser<Char, IfExpression> {
        val condition = IF.then(expression()).between(LPAREN, RPAREN)
        return condition.bind { c ->
            line().bind { thenClause ->
                ELSE.then(line()).optionalOpt().map { elseClause -> IfExpression(c, thenClause, elseClause.get()) }
            }
        }.attempt()
    }

    /**
     * ```
     * identifier <- Identifier
     * ```
     */
    fun identifier(): Parser<Char, Identifier> {
        return IDENT.map { Identifier(it) }
    }
}