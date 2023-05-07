package mcen.api.graph.node

import mcen.api.graph.type.Type
import net.minecraft.core.BlockPos

object Nodes {

//
//    class TextNode : PrimitiveNode("Text Node") {
//        val Value: Edge by output(Edge("Text", Type.STRING, "text") { it.isValid(Value) })
//        override fun toString(): String {
//            return "local ${Value.name.lowercase()}_${Value.id} = ${Value.userData}"
//        }
//    }
//
//    class FloatNode : PrimitiveNode("Float Node") {
//        val Value: Edge by output(Edge("Number", Type.FLOAT, 0.0f) { it.isValid(Value) })
//        override fun toString(): String {
//            return "local ${Value.name.lowercase()}_${Value.id} = ${Value.userData}"
//        }
//    }
//
//    class IntNode : PrimitiveNode("Int Node") {
//        val Value: Edge by output(Edge("Int", Type.INT, 0) { it.isValid(Value) })
//        override fun toString(): String {
//            return "local ${Value.name.lowercase()}_${Value.id} = ${Value.userData}"
//        }
//    }
//
//    class BooleanNode : PrimitiveNode("Boolean Node") {
//        val Value: Edge by output(Edge("Boolean", Type.BOOLEAN, true) { it.isValid(Value) })
//        override fun toString(): String {
//            return "local ${Value.name.lowercase()}_${Value.id} = ${Value.userData}"
//        }
//    }
//
//    class BlockPosNode : PrimitiveNode("BlockPos Node") {
//        val Value: Edge by output(Edge("BlockPos", Type.BLOCK_POS, BlockPos.ZERO) { it.isValid(Value) })
//        override fun toString(): String {
//            val pos = Value.userData as BlockPos
//            return "local ${Value.name.lowercase()}_${Value.id} = {${pos.x}, ${pos.y}, ${pos.z}}})"
//        }
//    }
//
//    class SelfNode : PrimitiveNode("Self Node") {
//        val Value: Edge by output(Edge("Self", Type.ANY) { it.isValid(Value) })
//    }
//
//    class ConsoleNode : StatementNode("Console Node") {
//        val Info: Edge by input(Edge("Info", Type.ANY) { it.parent is ExpressionNode || it.type == Type.STRING && !Info.isLinked() })
//        val Debug: Edge by input(Edge("Debug", Type.ANY) { it.parent is ExpressionNode || it.type == Type.STRING && !Info.isLinked() })
//        val Warn: Edge by input(Edge("Warn", Type.ANY) { it.parent is ExpressionNode || it.type == Type.STRING && !Info.isLinked() })
//        val Error: Edge by input(Edge("Error", Type.ANY) { it.parent is ExpressionNode || it.type == Type.STRING && !Info.isLinked() })
//
//        override fun toString(): String {
//            val sb = StringBuilder()
//            if (Info.isLinked()) sb.appendLine("Console.info(${Info.links().first().parent})")
//            if (Debug.isLinked()) sb.appendLine("Console.debug(${Debug.links().first().parent})")
//            if (Warn.isLinked()) sb.appendLine("Console.warn(${Warn.links().first().parent})")
//            if (Error.isLinked()) sb.appendLine("Console.error(${Error.links().first().parent})")
//            return sb.toString()
//        }
//    }
//
//    abstract class PrimitiveNode(name: String) : ExpressionNode(name) {
//
//
//    }
//
//    abstract class ExpressionNode(name: String) : StatementNode(name) {}
//
//    abstract class StatementNode(name: String) : Node(name) {}
//
//    class AddNode : ExpressionNode("Add") {
//        val Number1: Edge by input(Edge("Number A", Type.FLOAT) { it.type == Type.FLOAT || it.type == Type.INT })
//        val Number2: Edge by input(Edge("Number B", Type.FLOAT) { it.type == Type.FLOAT || it.type == Type.INT })
//        val Result: Edge by output(Edge("Number", Type.FLOAT) { it.isValid(Result) })
//    }
//
//    class PrintNode : StatementNode("Print") {
//        val PrintValue: Edge by input(Edge("Text", Type.ANY) { it.parent is ExpressionNode })
//    }
//
//    class IfNode : ExpressionNode("If") {
//        val Condition: Edge by input(Edge("Condition", Type.BOOLEAN) { it.type == Type.BOOLEAN })
//        val True: Edge by input(Edge("OnTrue", Type.ANY) { true })
//        val False: Edge by input(Edge("OnFalse", Type.ANY) { true })
//        val Result: Edge by output(Edge("Result", Type.ANY) { true })
//    }
//
//    class BranchNode : ExpressionNode("Branch") {
//        val Condition: Edge by input(Edge("Boolean", Type.BOOLEAN) { it.type == Type.BOOLEAN })
//        val True: Edge by output(Edge("True", Type.ANY) { it.type == Type.ANY })
//        val False: Edge by output(Edge("False", Type.ANY) { it.type == Type.ANY })
//    }
//
//    class AndNode : ExpressionNode("And") {
//        val Boolean1: Edge by input(Edge("Boolean A", Type.BOOLEAN) { it.type == Type.BOOLEAN })
//        val Boolean2: Edge by input(Edge("Boolean B", Type.BOOLEAN) { it.type == Type.BOOLEAN })
//        val Result: Edge by output(Edge("Boolean", Type.BOOLEAN) { it.isValid(Result) })
//    }
//
//    class OrNode : ExpressionNode("Or") {
//        val Boolean1: Edge by input(Edge("Boolean A", Type.BOOLEAN) { it.type == Type.BOOLEAN })
//        val Boolean2: Edge by input(Edge("Boolean B", Type.BOOLEAN) { it.type == Type.BOOLEAN })
//        val Result: Edge by output(Edge("Boolean", Type.BOOLEAN) { it.isValid(Result) })
//    }
//
//    class NotNode : ExpressionNode("Not") {
//        val Value: Edge by input(Edge("Not", Type.BOOLEAN) { it.type == Type.BOOLEAN })
//        val Result: Edge by output(Edge("Boolean", Type.BOOLEAN) { it.isValid(Result) })
//    }
//
//    class FunctionNode : StatementNode("Function") {
//        val FunctionName: Edge by input(Edge("FunctionName", Type.STRING) { it.type == Type.STRING })
//        val FunctionBody: Edge by output(Edge("FunctionBody", Type.ANY) { it.type == Type.ANY })
//    }
//

}