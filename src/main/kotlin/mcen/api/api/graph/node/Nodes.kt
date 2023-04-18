package mcen.api.api.graph.node

import mcen.api.api.graph.type.Type

object Nodes {


    class TextNode : PrimitiveNode("Text") {
        val TextValue: Edge by output(Edge("Text", Type.STRING) { it.isValid(TextValue) })
    }

    class NumberNode : PrimitiveNode("Number") {
        val NumberValue: Edge by output(Edge("Number", Type.FLOAT) { it.isValid(NumberValue) })
    }

    class BooleanNode : PrimitiveNode("Boolean") {
        val BooleanValue: Edge by output(Edge("Boolean", Type.BOOLEAN) { it.isValid(BooleanValue) })
    }

    abstract class PrimitiveNode(name: String) : ExpressionNode(name) {}


    abstract class ExpressionNode(name: String) : StatementNode(name) {}

    abstract class StatementNode(name: String) : Node(name) {}

    class AddNode : ExpressionNode("Add") {
        val Number1: Edge by input(Edge("Number", Type.FLOAT) { it.name == "Number" })
        val Number2: Edge by input(Edge("Number", Type.FLOAT) { it.name == "Number" })
        val Result: Edge by output(Edge("Number", Type.FLOAT) { it.isValid(Result) })
    }

    class PrintNode : StatementNode("Print") {
        val PrintValue: Edge by input(Edge("Text", Type.ANY) { it.parent is ExpressionNode })
    }

    class IfNode : ExpressionNode("If") {
        val Condition: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.type == Type.BOOLEAN })
        val True: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.type == Type.BOOLEAN })
        val False: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.type == Type.BOOLEAN })
        val Result: Edge by output(Edge("Boolean", Type.BOOLEAN) { it.type == Type.BOOLEAN })
    }

    class BranchNode : ExpressionNode("Branch") {
        val Condition: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.type == Type.BOOLEAN })
        val True: Edge by output(Edge("True", Type.ANY) { it.type == Type.ANY })
        val False: Edge by output(Edge("False", Type.ANY) { it.type == Type.ANY })
    }

    class AndNode : ExpressionNode("And") {
        val Boolean1: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.name == "Boolean" })
        val Boolean2: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.name == "Boolean" })
        val Result: Edge by output(Edge("Boolean", Type.BOOLEAN) { it.isValid(Result) })
    }

    class OrNode : ExpressionNode("Or") {
        val Boolean1: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.name == "Boolean" })
        val Boolean2: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.name == "Boolean" })
        val Result: Edge by output(Edge("Boolean", Type.BOOLEAN) { it.isValid(Result) })
    }

    class NotNode : ExpressionNode("Not") {
        val Boolean1: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.name == "Boolean" })
        val Result: Edge by output(Edge("Boolean", Type.BOOLEAN) { it.isValid(Result) })
    }

    class FunctionNode : StatementNode("Function") {
        val FunctionName: Edge by input(Edge("FunctionName", Type.STRING) { it.type == Type.STRING })
        val FunctionBody: Edge by output(Edge("FunctionBody", Type.ANY) { it.type == Type.ANY })
    }



}