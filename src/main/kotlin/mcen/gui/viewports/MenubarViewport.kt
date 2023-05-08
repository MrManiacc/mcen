package mcen.gui.viewports

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.*
import imgui.type.ImString
import mcen.api.workspace.File
import mcen.api.workspace.Folder
import mcen.api.workspace.Workspace
import mcen.content.internal.Registry
import mcen.content.internal.WorldPos
import mcen.gui.Ico
import mcen.gui.Icons
import mcen.gui.Renderer
import mcen.registry.net.CompileSource
import mcen.registry.net.SyncWorkspacePacket
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW.*
import kotlin.random.Random

class MenubarViewport(private val worldPos: WorldPos) : Viewport("menubar") {
    override fun Renderer.dockspace() {
        val scripts = getViewport<ScriptViewport>() ?: return
        val explorer = getViewport<ExplorerViewport>() ?: return
        var action = ""
        mainMenuBar {

            menu("File") {
                menuItem("New", "Ctrl-J") {
                    action = "new"
                }
                menuItem("Save", "Ctrl-S") {
                    if (scripts.activeFile == File.InvalidFile) {
                        action = "save"
                    } else {
                        scripts.activeFile.sourceCode = scripts.editor.text
                        Registry.Net.sendToServer(SyncWorkspacePacket(explorer.workspace, worldPos.position, worldPos.world))
                    }

                }
                menuItem("Show Paths", "", explorer.displayFullPaths) {
                    explorer.displayFullPaths = !explorer.displayFullPaths
                }
                menuItem("ReadOnly", "", scripts.editor.isReadOnly) {
                    scripts.editor.isReadOnly = !scripts.editor.isReadOnly
                }

            }
            menu("Edit") {
                menuItem("Undo", "Ctrl-Z", !scripts.editor.isReadOnly && scripts.editor.canUndo()) {
                    scripts.editor.undo(1)
                }
                menuItem("Redo", "Ctrl-Y", !scripts.editor.isReadOnly && scripts.editor.canRedo()) {
                    scripts.editor.redo(1)
                }
                menuItem("Copy", "Ctrl-C", !scripts.editor.isReadOnly && scripts.editor.hasSelection()) {
                    scripts.editor.copy()
                }
                menuItem("Paste", "Ctrl-V", !scripts.editor.isReadOnly && ImGui.getClipboardText() != null) {
                    scripts.editor.paste()
                }
                menuItem("Cut", "Ctrl-X", !scripts.editor.isReadOnly && scripts.editor.hasSelection()) {
                    scripts.editor.cut()
                }
                menuItem("Delete", "Del", !scripts.editor.isReadOnly && scripts.editor.hasSelection()) {
                    scripts.editor.delete()
                }

            }

            ImGui.sameLine((ImGui.getWindowWidth() / 2) - ImGui.calcTextSize("editing: ${scripts.activeFile.name}.lua").x / 2);
            ImGui.text("editing: ${scripts.activeFile.name}.lua")
            ImGui.sameLine(ImGui.getWindowWidth() - 270);
            ImGui.pushStyleColor(ImGuiCol.Button, 45, 46, 46, 255)

            if (ImGui.button("Save All ${Ico.ICON_SAVE_ALL}")) {
                scripts.activeFile.sourceCode = scripts.editor.text
                Registry.Net.sendToServer(
                    SyncWorkspacePacket(
                        explorer.workspace,
                        worldPos.position,
                        worldPos.world
                    )
                )
            }
            if(ImGui.isItemHovered())
                 ImGui.setTooltip("Saves current workspace on the server")


            if (ImGui.button("Clear ${Ico.ICON_CLEAR_ALL}")) {
                scripts.messages.clear()
                scripts.errors.clear()
            }
            if(ImGui.isItemHovered())
                ImGui.setTooltip("Clear all messages and errors")
            ImGui.sameLine()

            if (ImGui.button("Run ${Ico.ICON_DEBUG_START}")) {
                scripts.activeFile.sourceCode = scripts.editor.text
                Registry.Net.sendToServer(
                    CompileSource(
                        worldPos.position,
                        worldPos.world,
                        scripts.activeFile.sourceCode
                    )
                )
                scripts.errors.clear()
            }
            if(ImGui.isItemHovered())
                ImGui.setTooltip("Sends script to be compiled/ran server-side")
            ImGui.popStyleColor()

        }
        glfwGetKey(Minecraft.getInstance().window.window, GLFW_KEY_J).let { sKey ->
            if(sKey == GLFW_PRESS && glfwGetKey(Minecraft.getInstance().window.window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS){
                action = "new"
                explorer.newContext = scripts.activeFile.parent
            }
        }

        glfwGetKey(Minecraft.getInstance().window.window, GLFW_KEY_S).let {sKey->
            if(sKey == GLFW_PRESS && glfwGetKey(Minecraft.getInstance().window.window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS){
                if (scripts.activeFile == File.InvalidFile) {
                    action = "save"
                } else {
                    scripts.activeFile.sourceCode = scripts.editor.text
                    Registry.Net.sendToServer(SyncWorkspacePacket(explorer.workspace, worldPos.position, worldPos.world))
                }
            }
        }

        if (action.isNotBlank()) {
            ImGui.openPopup("${action}_action")
            action = ""
        }

    }


}