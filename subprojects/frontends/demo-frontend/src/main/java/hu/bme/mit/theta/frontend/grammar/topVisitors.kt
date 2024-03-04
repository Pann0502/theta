package hu.bme.mit.theta.frontend.grammar

import hu.bme.mit.theta.demo.frontend.dsl.gen.DemoBaseVisitor
import hu.bme.mit.theta.demo.frontend.dsl.gen.DemoLexer
import hu.bme.mit.theta.demo.frontend.dsl.gen.DemoParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

// TODO: write the rest of the visitors and build a model
// feel free to use chatgpt and the existing code (e.g., C, btor2, Promela frontend)
// you can also utilize that intellij can convert Java code to Kotlin (just paste java code in a kotlin file)
// https://github.com/ftsrg/theta/tree/btor2-frontend/subprojects/frontends/btor2-frontend
// https://github.com/ftsrg/theta/tree/trace-generation/subprojects/frontends/promela-frontend
class ModelVisitor : DemoBaseVisitor<String>() {
    override fun visitModel(ctx: DemoParser.ModelContext?): String {
        val assignments =
            ctx!!.assignment().map { it.accept(this) }
        val assertion = ctx.assertion().accept(this)
        val builder = StringBuilder()
        for (assignment in assignments) {
            builder.append(assignment).append("\n")
        }
        builder.append(assertion).append("\n")
        return builder.toString()
    }

    override fun visitAssignment(ctx: DemoParser.AssignmentContext?): String {
        if (ctx!!.expression()!=null) {
            return ctx.VarName().text + " := " + ctx.expression().text
        } else {
            return ctx.VarName().text + " := " + ctx.value().text
        }

        override fun visitVariableDeclaration(ctx: DemoParser.VariableDeclarationContext?): String {
            val variableName = ctx!!.VarName().text
            val variableType = ctx.type().accept(this)
            return "val $variableName: $variableType"
        }

        override fun visitArithmeticExpression(ctx: DemoParser.ArithmeticExpressionContext?): String {
            val leftOperand = ctx!!.leftOperand.accept(this)
            val operator = ctx.operator.text
            val rightOperand = ctx.rightOperand.accept(this)
            return "($leftOperand $operator $rightOperand)"
        }

        override fun visitIfStatement(ctx: DemoParser.IfStatementContext?): String {
            val condition = ctx!!.condition().accept(this)
            val thenBranch = ctx.thenBranch().accept(this)
            val elseBranch = ctx.elseBranch()?.accept(this) ?: ""
            return "if ($condition) {\n$thenBranch\n} else {\n$elseBranch\n}"
        }

        override fun visitLogicalExpression(ctx: DemoParser.LogicalExpressionContext?): String {
            val leftOperand = ctx!!.leftOperand.accept(this)
            val operator = ctx.operator.text
            val rightOperand = ctx.rightOperand.accept(this)
            return "($leftOperand $operator $rightOperand)"
        }

        override fun visitLoop(ctx: DemoParser.LoopContext?): String {
            val condition = ctx!!.condition().accept(this)
            val body = ctx.body().accept(this)
            return "while ($condition) {\n$body\n}"
        }

        override fun visitBreakStatement(ctx: DemoParser.BreakStatementContext?): String {
            return "break"
        }

        override fun visitContinueStatement(ctx: DemoParser.ContinueStatementContext?): String {
            return "continue"
        }

    }

    override fun visitVariableDeclaration(ctx: DemoParser.VariableDeclarationContext?): String {
        val variableName = ctx!!.VarName().text
        val variableType = ctx.type().accept(this)
        return "val $variableName: $variableType"
    }

    override fun visitAssertion(ctx: DemoParser.AssertionContext?): String {
        return "assert " + ctx!!.comparison().text
    }

    override fun visitArithmeticExpression(ctx: DemoParser.ArithmeticExpressionContext?): String {
        val leftOperand = ctx!!.leftOperand.accept(this)
        val operator = ctx.operator.text
        val rightOperand = ctx.rightOperand.accept(this)
        return "($leftOperand $operator $rightOperand)"
    }

    override fun visitIfStatement(ctx: DemoParser.IfStatementContext?): String {
        val condition = ctx!!.condition().accept(this)
        val thenBranch = ctx.thenBranch().accept(this)
        val elseBranch = ctx.elseBranch()?.accept(this) ?: ""
        return "if ($condition) {\n$thenBranch\n} else {\n$elseBranch\n}"
    }

    override fun visitLogicalExpression(ctx: DemoParser.LogicalExpressionContext?): String {
        val leftOperand = ctx!!.leftOperand.accept(this)
        val operator = ctx.operator.text
        val rightOperand = ctx.rightOperand.accept(this)
        return "($leftOperand $operator $rightOperand)"
    }

    override fun visitLoop(ctx: DemoParser.LoopContext?): String {
        val condition = ctx!!.condition().accept(this)
        val body = ctx.body().accept(this)
        return "while ($condition) {\n$body\n}"
    }

    override fun visitBreakStatement(ctx: DemoParser.BreakStatementContext?): String {
        return "break"
    }

    override fun visitContinueStatement(ctx: DemoParser.ContinueStatementContext?): String {
        return "continue"
    }




}

fun main() {
    // Create a CharStream that reads from standard input
    val demoCode = """
        var1 := 2
        var2 := input
        var3 := var1 + var2
        var4 := 3
        assert var3 < 10
    """.trimIndent()

    val input = CharStreams.fromString(demoCode)

    // Create a lexer that feeds off of input CharStream
    val lexer = DemoLexer(input)

    // Create a buffer of tokens pulled from the lexer
    val tokens = CommonTokenStream(lexer)

    // Create a parser that feeds off the tokens buffer
    val parser = DemoParser(tokens)

    // Start parsing from the starting rule
    val tree = parser.model() // Replace 'startingRule' with the name of your starting rule

    // Create a visitor
    val visitor = ModelVisitor()

    // Traverse the parse tree using the visitor
    print(visitor.visit(tree))
}