package mcen.gui.viewports

import imgui.ImGui
import imgui.flag.*
import imgui.type.ImInt
import imgui.type.ImString
import mcen.api.workspace.Folder
import mcen.api.workspace.Workspace
import mcen.content.internal.Registry
import mcen.content.internal.WorldPos
import mcen.gui.Renderer
import mcen.registry.net.SyncWorkspacePacket

class ExplorerViewport(val workspace: Workspace, private val worldPos: WorldPos) : Viewport("Workspace") {
    private val saveAs = ImString()

    override fun Renderer.dockspace() {
        drawFolder(workspace)
    }

    override fun Renderer.createDock(parentId: ImInt) {
        val dockLeft: Int =
            imgui.internal.ImGui.dockBuilderSplitNode(parentId.get(), ImGuiDir.Left, 0.23f, null, parentId)
        imgui.internal.ImGui.dockBuilderDockWindow(name, dockLeft)
    }


    private fun drawFolder(folder: Folder) {
        val scripts = getViewport<ScriptViewport>() ?: return
        if (ImGui.treeNodeEx(
                folder.name,
                ImGuiTreeNodeFlags.SpanFullWidth or ImGuiTreeNodeFlags.OpenOnArrow or ImGuiTreeNodeFlags.OpenOnDoubleClick
            )
        ) {
            folder.files.forEach { child ->
                if (ImGui.selectable("${child.key}.lua", false, ImGuiSelectableFlags.SpanAllColumns)) {
//                    scripts.activeFile.sourceCode = scripts.editor.text
                    scripts.setFile(child.value)
                }
            }
            folder.folders.forEach { child ->
                drawFolder(child.value)
            }
            ImGui.treePop()
        }
    }

    override fun popup() {
        val scripts = getViewport<ScriptViewport>() ?: return
        val center = ImGui.getMainViewport().center
        ImGui.setNextWindowPos(center.x, center.y, ImGuiCond.Appearing, 0.5f, 0.5f);
        if (ImGui.beginPopup("save_action")) {
            val workspace = getViewport<ExplorerViewport>()?.workspace ?: return
            ImGui.inputText("Name", saveAs, ImGuiInputTextFlags.CallbackAlways)
            if (ImGui.button("Save")) {
                if (saveAs.get().isNotEmpty()) {
                    workspace.writeFile(saveAs.get(), scripts.editor.text)
                    Registry.Net.sendToServer(SyncWorkspacePacket(workspace, worldPos.position, worldPos.world))
                    ImGui.closeCurrentPopup()
                }
            }
            ImGui.endPopup()
        }
        ImGui.setNextWindowPos(center.x, center.y, ImGuiCond.Appearing, 0.5f, 0.5f);
        if (ImGui.beginPopup("new_action")) {
            val workspace = getViewport<ExplorerViewport>()?.workspace ?: return
            ImGui.inputText("Name", saveAs, ImGuiInputTextFlags.CallbackAlways)
            if (ImGui.button("Create Folder")) {
                if (saveAs.get().isNotEmpty()) {
                    workspace.addFolder(saveAs.get())
                    Registry.Net.sendToServer(SyncWorkspacePacket(workspace, worldPos.position, worldPos.world))
                    ImGui.closeCurrentPopup()
                }
            }
            ImGui.sameLine()
            if (ImGui.button("Create File")) {
                if (saveAs.get().isNotEmpty()) {
                    scripts.setFile(workspace.writeFile(saveAs.get(), ""))
                    Registry.Net.sendToServer(SyncWorkspacePacket(workspace, worldPos.position, worldPos.world))
                    ImGui.closeCurrentPopup()
                }
            }
            ImGui.endPopup()
        }
    }
}

