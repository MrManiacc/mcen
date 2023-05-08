package mcen.gui.viewports

import imgui.ImGui
import imgui.flag.*
import imgui.type.ImInt
import imgui.type.ImString
import mcen.api.workspace.File
import mcen.api.workspace.Folder
import mcen.api.workspace.Workspace
import mcen.content.internal.Registry
import mcen.content.internal.WorldPos
import mcen.gui.Ico
import mcen.gui.Icons
import mcen.gui.Renderer
import mcen.registry.net.SyncWorkspacePacket

class ExplorerViewport(val workspace: Workspace, private val worldPos: WorldPos) : Viewport("Workspace") {
    private val saveAs = ImString()
     var newContext: Folder? = null

    var displayFullPaths: Boolean = false
    override fun Renderer.dockspace() {

        drawFolder(workspace)

    }

    override fun popup() {
        var openedNew = false
        if (newContext != null) {
            ImGui.openPopup("new_action")
            openedNew = true
        }
        val scripts = getViewport<ScriptViewport>() ?: return
        val center = ImGui.getMainViewport().center
        ImGui.setNextWindowPos(center.x, center.y, ImGuiCond.Appearing, 0.5f, 0.5f);
        if (ImGui.beginPopup("save_action")) {
            val workspace = getViewport<ExplorerViewport>()?.workspace ?: return
            ImGui.inputText("Name", saveAs, ImGuiInputTextFlags.CallbackAlways)
            if (ImGui.button("Save")) {
                if (saveAs.get().isNotEmpty()) {
                    scripts.setFile(workspace.writeFile(saveAs.get(), scripts.editor.text))
                    Registry.Net.sendToServer(SyncWorkspacePacket(workspace, worldPos.position, worldPos.world))
                    ImGui.closeCurrentPopup()
                }
            }
            ImGui.endPopup()
        }
        ImGui.setNextWindowPos(center.x, center.y, ImGuiCond.Appearing, 0.5f, 0.5f);
        if (ImGui.beginPopup("new_action")) {
            if (newContext != null) {
                if (!newContext!!.isRoot) saveAs.set("${newContext!!.path}/")
                else saveAs.set("/")
                newContext = null
            }
            val workspace = getViewport<ExplorerViewport>()?.workspace ?: return


            ImGui.inputText("Name", saveAs, ImGuiInputTextFlags.CallbackEdit)
            if (openedNew)
                ImGui.setKeyboardFocusHere(-1)
            if (!saveAs.get().startsWith("/")) saveAs.set("/${saveAs.get()}")


            if (ImGui.button("Create Folder")) {
                if (saveAs.get().isNotEmpty()) {
                    workspace.addFolder(saveAs.get())
                    Registry.Net.sendToServer(SyncWorkspacePacket(workspace, worldPos.position, worldPos.world))
                    ImGui.closeCurrentPopup()
                }
            }
            if (ImGui.isItemHovered())
                ImGui.setTooltip("Creates a new folder (don't include the / at the end)")
            ImGui.sameLine()
            if (ImGui.button("Create Script")) {
                if (saveAs.get().isNotEmpty()) {
                    scripts.setFile(workspace.writeFile(saveAs.get(), ""))
                    Registry.Net.sendToServer(SyncWorkspacePacket(workspace, worldPos.position, worldPos.world))
                    ImGui.closeCurrentPopup()
                }
            }
            if (ImGui.isItemHovered())
                ImGui.setTooltip("Creates a new lua script (don't include the .lua extension)")
            ImGui.endPopup()
        }
    }


    override fun Renderer.createDock(parentId: ImInt) {
        val dockLeft: Int =
            imgui.internal.ImGui.dockBuilderSplitNode(parentId.get(), ImGuiDir.Left, 0.23f, null, parentId)
        imgui.internal.ImGui.dockBuilderDockWindow(name, dockLeft)
    }

    private fun drawFolder(folder: Folder) {
        val scripts = getViewport<ScriptViewport>() ?: return

        val treeOpen = ImGui.treeNodeEx(
            if (displayFullPaths) folder.path else folder.name,
            ImGuiTreeNodeFlags.AllowItemOverlap or ImGuiTreeNodeFlags.SpanAvailWidth or ImGuiTreeNodeFlags.OpenOnArrow or ImGuiTreeNodeFlags.OpenOnDoubleClick or ImGuiTreeNodeFlags.DefaultOpen
        )
        val folderId = "folder_${folder.path}"
        if (ImGui.beginPopupContextItem("folder_context_menu_$folderId")) {
            folderContextMenu(folder)
            ImGui.endPopup()
        }
        ImGui.sameLine(ImGui.getWindowContentRegionMaxX() - 30)
        ImGui.setCursorPosY(ImGui.getCursorPosY() - 2)
        ImGui.pushStyleColor(ImGuiCol.Button, 45, 46, 46, 255)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 185, 183, 175, 255)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, 65, 66, 66, 255)
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 20f)
        if (ImGui.button("${Ico.ICON_FILE_DIRECTORY_CREATE}##${folder.path}")) {
            newContext = folder
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Create new file or folder")
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 2)
        ImGui.popStyleColor(3)
        ImGui.popStyleVar()

        if (treeOpen) {
            folder.files.forEach { child ->
                val fileId = "file_${child.value.path}"
                if (ImGui.selectable(
                        "${if (displayFullPaths) "${child.value.parent!!.path}/${child.key}" else child.key}.lua##$fileId",
                        false,
                        ImGuiSelectableFlags.SpanAllColumns
                    )
                ) {
                    scripts.setFile(child.value)
                }
                if (ImGui.beginPopupContextItem("file_context_menu_$fileId")) {
                    fileContextMenu(child.value)
                    ImGui.endPopup()
                }
            }
            folder.folders.forEach { child ->
                drawFolder(child.value)
            }
            ImGui.treePop()
        }


    }

    private fun folderContextMenu(folder: Folder) {
        if (ImGui.menuItem("Create New File")) {
            newContext = folder
        }
        if (ImGui.menuItem("Create New Folder")) {
            newContext = folder
        }
        if (ImGui.menuItem("Delete")) {
            deleteFolder(folder)
        }
        if (ImGui.menuItem("Rename")) {
            // Code to rename the folder (assuming you have a renaming function)
        }
    }

    private fun deleteFolder(folder: Folder) {
        folder.parent?.removeFolder(folder)
        // Send sync packet to the server
        Registry.Net.sendToServer(SyncWorkspacePacket(workspace, worldPos.position, worldPos.world))
    }

    private fun deleteFile(file: File) {
        file.parent?.removeFile(file)
        // Send sync packet to the server
        Registry.Net.sendToServer(SyncWorkspacePacket(workspace, worldPos.position, worldPos.world))
    }

    private fun fileContextMenu(file: File) {
        if (ImGui.menuItem("Delete")) {
            deleteFile(file)
        }
        if (ImGui.menuItem("Rename")) {
            // Code to rename the file (assuming you have a renaming function)
        }
    }
}