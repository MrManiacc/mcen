package mcen.api.graph

import mcen.Serial
import mcen.api.graph.node.Edge
import mcen.api.graph.node.Node
import mcen.getDeepList
import mcen.putDeepList
import net.minecraft.nbt.CompoundTag

/**
 * A graph is a wrapper around all the nodes. While nodes do share their connections directly
 * via edges called "connectors", we still need to keep track of all nodes because unlike a programming
 * language where everything must always be linked up, in a node editor nodes are valid while unlinked (at least for rendering)
 */
class Graph() : Serial {
    private val nodeMap: MutableMap<Int, Node> = HashMap()
    private val inputToNode: MutableMap<Int, Edge> = HashMap()
    private val outputToNode: MutableMap<Int, Edge> = HashMap()
    private val linkMap: MutableMap<Int, Pair<Int, Int>> = HashMap()
    private var nextNodeID = 1
    private var nextEdgeID = 1_000_000
    private var nextLinkId = 1
    internal val nodes: Collection<Node> get() = nodeMap.values
    internal val links: Map<Int, Pair<Int, Int>> get() = linkMap
    private var meta: String? = null

    internal fun setMeta(meta: String) {
        this.meta = meta
    }

    internal inline fun takeMeta(meta: (String) -> Unit) {
        this.meta?.apply(meta)
        this.meta = null
    }

    fun addEdge(edge: Edge) {
        if (edge.id == -1) edge.id = nextEdgeID++
        if (edge.connectorType == Edge.ConnectorType.Output) outputToNode[edge.id] = edge
        else inputToNode[edge.id] = edge
    }


    fun addNode(node: Node): Node {
        node.graph = this
        if (node.id == -1) node.id = nextNodeID++
        //TODO: remove this may cause side effects
        node.inputs.forEach(::addEdge)
        node.outputs.forEach(::addEdge)
        nodeMap[node.id] = node
        return node
    }

//    fun findTopLevelStatements() = nodeMap.filterValues { it is Nodes.StatementNode && it.inputs.isEmpty() }.values

    fun link(input: Edge, output: Edge, linkId: Int = ++nextLinkId) {
        if (!input.linkTo(output, linkId)) return
        linkMap[linkId] = input.id to output.id
        output.linkTo(input, linkId)
    }

    fun unlink(linkId: Int) {
        for (node in nodes) {
            for (input in node.inputs) {
                if (input.links.containsKey(linkId)) {
                    val other = input.links[linkId]!!
                    input.unlink(linkId)
                    val link = findByOutputID(other) ?: continue
                    link.unlink(linkId)
                    linkMap.remove(linkId)
                }
            }
        }
    }

    fun findByEdge(id: Int): Edge? = findByInputID(id) ?: findByOutputID(id)
    fun findByOutputID(id: Int): Edge? = outputToNode[id]
    fun findByInputID(id: Int): Edge? = inputToNode[id]
    fun findByNodeID(id: Int): Node? = nodeMap[id]

    /**
     * Removes a node from the graph, this will also remove all links to and from the node
     */
    fun removeNode(nodeId: Int) {
        val node = nodeMap.remove(nodeId) ?: return
        node.unAttach()
        node.inputs.forEach {
            inputToNode.remove(it.id)
        }
        node.outputs.forEach {
            outputToNode.remove(it.id)
        }
    }

    internal inline fun iterateLinks(link: (Int, Edge, Edge) -> Unit) {
        for (node in nodes) {
            for (input in node.inputs) {
                for (links in input.links) {
                    link(links.key, input, findByOutputID(links.value) ?: continue)
                }
            }
        }
    }

    internal inline fun iterate(node: (Node) -> Unit) {
        nodes.forEach(node)
    }

    override fun CompoundTag.serialize() {
        putBoolean("hasMeta", meta != null)
        if (meta != null) putString("meta", meta!!)
        putInt("nextNodeId", nextNodeID)
        putInt("nextPinId", nextEdgeID)
        putInt("nextLinkId", nextLinkId)
        putDeepList("nodes", nodeMap.values.toList())
        val links = CompoundTag()
        links.putIntArray("keys", this@Graph.linkMap.keys.toList())
        for ((i, value) in this@Graph.linkMap.values.withIndex()) {
            links.putInt("input_${i}", value.first)
            links.putInt("output_${i}", value.second)
        }
        put("links", links)
    }


    override fun CompoundTag.deserialize() {
        if (getBoolean("hasMeta")) meta = getString("meta")
        nextNodeID = getInt("nextNodeId")
        nextEdgeID = getInt("nextPinId")
        nextLinkId = getInt("nextLinkId")
        nodeMap.clear()
        outputToNode.clear()
        inputToNode.clear()
        linkMap.clear()
        val nodes = getDeepList<Node>("nodes")
        nodes.forEach(::addNode)
        val links = getCompound("links")
        val keys = links.getIntArray("keys")
        keys.forEachIndexed { index, linkId ->
            val input = findByInputID(links.getInt("input_$index")) ?: return@forEachIndexed
            val output = findByOutputID(links.getInt("output_$index")) ?: return@forEachIndexed
            link(input, output, linkId)
        }

    }

    fun removeEdge(id: Int) {
        outputToNode.remove(id)
        inputToNode.remove(id)
    }

}