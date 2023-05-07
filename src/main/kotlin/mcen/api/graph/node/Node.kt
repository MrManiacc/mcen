package mcen.api.graph.node

import mcen.Serial
import mcen.api.graph.Graph
import net.minecraft.nbt.CompoundTag
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Node(val name: String) : Serial {
    private val inputEdges: MutableList<Edge> = ArrayList()
    private val outputEdges: MutableList<Edge> = ArrayList()

    //exposes a read only list of the inputs
    val inputs: List<Edge> get() = inputEdges

    //exposes a read only list of the outputs
    val outputs: List<Edge> get() = outputEdges

    //exposes a read only reference to the graph this node is a part of
    lateinit var graph: Graph
        internal set

    //the id of this node in the graph
    var id: Int = -1
        internal set


    @Suppress("UNCHECKED_CAST")
    protected fun output(
        connector: Edge
    ): ReadOnlyProperty<Any?, Edge> {
        connector.parent = this
        connector.connectorType = Edge.ConnectorType.Output
        outputEdges.add(connector)
        return object : ReadOnlyProperty<Any?, Edge>, Supplier<Edge> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = connector
            override fun get() = connector
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun input(
        connector: Edge
    ): ReadOnlyProperty<Any?, Edge> {
        connector.parent = this
        connector.connectorType = Edge.ConnectorType.Input
        inputEdges.add(connector)
        return object : ReadOnlyProperty<Any?, Edge>, Supplier<Edge> {
            override fun getValue(thisRef: Any?, property: KProperty<*>) = connector
            override fun get() = connector
        }
    }

    fun isLinked(): Boolean {
        for (input in inputs) if (input.isLinked()) return true
        for (output in outputs) if (output.isLinked()) return true
        return false
    }

    fun unAttach() {
        inputs.forEach(Edge::unlink)
        outputs.forEach(Edge::unlink)
    }

    override fun serializeNBT(): CompoundTag =
        with(super.serializeNBT()) {
            inputs.forEach {
                put("input_${it.id}", it.serializeNBT())
            }
            outputs.forEach {
                put("output_${it.id}", it.serializeNBT())
            }
            putInt("nodeId", this@Node.id)
            this
        }

    override fun deserializeNBT(nbt: CompoundTag) {
        super.deserializeNBT(nbt)
        with(nbt) {
            inputs.forEach {
                it.deserializeNBT(getCompound("input_${it.id}"))
            }
            outputs.forEach {
                it.deserializeNBT(getCompound("output_${it.id}"))
            }
            this@Node.id = getInt("nodeId")
        }
    }

    /**
     * This is called from the child connector,
     */
    open fun onLink(from: Edge, to: Edge) = Unit

    /**
     * This is called from the child connector,
     */
    open fun render() = Unit
}