package mcen.api.api.graph.node


import mcen.Serial
import mcen.api.api.graph.type.Type
import mcen.getEnum
import mcen.putEnum
import net.minecraft.nbt.CompoundTag

class Edge(
    /**
     * A unique name for the pin
     */
    var name: String,
    /**
     * The type of the edge
     */
    val type: Type,
    /**
     * Checks to see if the given link is valid for this connector
     */
    private val validateLink: (other: Edge) -> Boolean = { true }
) : Serial {
    private var linksMap: MutableMap<Int, Int> = HashMap()
    val links: Map<Int, Int> get() = linksMap
    lateinit var parent: Node
        internal set
    var id: Int = -1
        internal set
    lateinit var connectorType: ConnectorType
        internal set

    /**
     * Validates the link between the nodes
     */
    fun isValid(other: Edge) = validateLink(other)

    /**
     * True if we are linked to another node
     */
    fun isLinked(): Boolean = linksMap.isNotEmpty()

    inline fun <reified T : Any> links(): List<T> = links.values.mapNotNull { parent.graph.findByPin(it) }.filterIsInstance<T>()

    /**
     * This will validate the connection between the nodes
     */
    fun linkTo(other: Edge, linkId: Int): Boolean {
        if (isValid(other)) {
            linksMap[linkId] = other.id
            return true
        }
        return false
    }

    /**
     * Removes our link regardless of whether or not it's present
     */
    fun unlink() {
        linksMap.values.map(parent.graph::findByPin).forEach {
            if (it != null) linksMap.keys.forEach(it::unlink)
        }
        linksMap.clear()
    }

    /**
     * Removes our link regardless of whether or not it's present
     */
    fun unlink(linkId: Int) {
        linksMap.remove(linkId)
    }


    override fun CompoundTag.serialize() {
        putString("name", name)
        putInt("connector_id", this@Edge.id)
        putEnum("connector_type", connectorType)
        val links = CompoundTag()
        links.putIntArray("linkIds", linksMap.keys.toList())
        links.putIntArray("pinIds", linksMap.values.toList())
        put("links", links)
    }

    override fun CompoundTag.deserialize() {
        name = getString("name")
        this@Edge.id = getInt("connector_id")
        connectorType = getEnum("connector_type")
        linksMap.clear()
        val links = getCompound("links")
        val keys = links.getIntArray("linkIds")
        val values = links.getIntArray("pinIds")
        assert(keys.size == values.size)
        keys.forEachIndexed { index, linkId ->
            val connector = values[index]
            linksMap[linkId] = connector
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Edge) return false

        if (name != other.name) return false
        if (parent != other.parent) return false
        if (id != other.id) return false
        if (connectorType != other.connectorType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + parent.hashCode()
        result = 31 * result + id
        result = 31 * result + connectorType.hashCode()
        return result
    }


    enum class ConnectorType {
        Input, Output
    }

}