package mcen.gui.viewports

import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesMiniMapLocation
import imgui.flag.ImGuiMouseButton
import imgui.type.ImInt
import mcen.api.graph.Graph
import mcen.api.graph.node.Node
import mcen.api.graph.node.Nodes
import mcen.content.internal.WorldPos
import mcen.gui.Renderer
import kotlin.reflect.full.primaryConstructor

class NodeViewport() : Viewport( "Node Editor") {
    private val context = ImNodes.editorContextCreate()
    private val graph: Graph = Graph()
    private val LINK_A = ImInt()
    private val LINK_B = ImInt()
    private val nodes: Map<String, () -> Node> =
        Nodes::class.nestedClasses.filter { !it.isAbstract }.associate { it.simpleName!! to { it.primaryConstructor!!.call() as Node } }


    fun buildSource(): String {
        return ""
    }

    private fun renderNodes(): Boolean {
        graph.iterate {
            ImNodes.beginNode(it.id)
            ImNodes.beginNodeTitleBar()
            ImGui.dummy(50f, 0f)
            ImGui.sameLine()
            ImGui.text(it.name)
            ImGui.sameLine()
            ImGui.dummy(50f, 0f)
            ImNodes.endNodeTitleBar()

            for (input in it.inputs) {
                ImNodes.beginInputAttribute(input.id)
                ImGui.text(input.name)
                ImNodes.endInputAttribute()
            }
            for (output in it.outputs) {
                ImNodes.beginOutputAttribute(output.id)
                ImGui.text(output.name)
                ImNodes.endOutputAttribute()
            }

            ImNodes.endNode()
        }
        graph.links.forEach {
            ImNodes.link(it.key, it.value.first, it.value.second)
        }
        val isEditorHovered = ImNodes.isEditorHovered();

        ImNodes.miniMap(0.2f, ImNodesMiniMapLocation.BottomRight);
        ImNodes.endNodeEditor()
        return isEditorHovered
    }

    private fun renderLinks() {
        if (ImNodes.isLinkCreated(LINK_A, LINK_B)) {
            val source = graph.findByOutputID(LINK_A.get())
            val target = graph.findByInputID(LINK_B.get())
            if (source != null && target != null && source.parent.id != target.parent.id) {
                if (source.isValid(target))
                    graph.link(source, target)

            }
        }
    }


    private fun renderPopups(isEditorHovered: Boolean) {
        if (ImGui.isMouseClicked(ImGuiMouseButton.Right)) {
            val hoveredNode = ImNodes.getHoveredNode();
            if (hoveredNode != -1) {
                ImGui.openPopup("node_context");
                ImGui.getStateStorage().setInt(ImGui.getID("delete_node_id"), hoveredNode);
            } else if (isEditorHovered) {
                ImGui.openPopup("node_editor_context");
            }
        }
        if (ImGui.isPopupOpen("node_context")) {
            val targetNode = ImGui.getStateStorage().getInt(ImGui.getID("delete_node_id"))
            if (ImGui.beginPopup("node_context")) {
                if (ImGui.button("Delete " + graph.findByNodeID(targetNode)?.name)) {
                    graph.removeNode(targetNode)
                    ImGui.closeCurrentPopup()
                }
                ImGui.endPopup()
            }
        }
        if (ImGui.beginPopup("node_editor_context")) {
            if (ImGui.beginListBox("Nodes", 150f, 250f)) {
                nodes.forEach {
                    if (ImGui.selectable(it.key)) {
                        val node = graph.addNode(it.value())
                        ImNodes.setNodeScreenSpacePos(node.id, ImGui.getMousePosX(), ImGui.getMousePosY())
                        ImGui.closeCurrentPopup()
                    }
                }
                ImGui.endListBox()
            }
            ImGui.endPopup()
        }
    }

    override fun Renderer.dockspace() {
        ImNodes.editorContextSet(context)
        ImNodes.beginNodeEditor()
        val isHovered = renderNodes()
        renderLinks()
        renderPopups(isHovered)
    }

    override fun Renderer.createDock(parentId: ImInt) {
        imgui.internal.ImGui.dockBuilderDockWindow(name, parentId.get())
    }

    override fun onClose() {
        ImNodes.editorContextFree(context)
    }
}