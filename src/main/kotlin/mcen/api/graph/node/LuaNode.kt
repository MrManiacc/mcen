package mcen.api.graph.node

import mcen.api.graph.Graph
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class LuaNode(private val graph: Graph) : OneArgFunction() {
    override fun call(arg: LuaValue): LuaValue {
        if (arg.isnil() || !arg.isstring()) return LuaValue.NIL
        val name = arg.tojstring()
        val node = Node(name)
        graph.addNode(node)
        return LuaValue.valueOf(node.id)
    }

}