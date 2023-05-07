package mcen.gui.viewports

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiInputTextFlags
import imgui.type.ImString
import mcen.api.workspace.File
import mcen.api.workspace.Folder
import mcen.api.workspace.Workspace
import mcen.content.internal.Registry
import mcen.content.internal.WorldPos
import mcen.gui.Icons
import mcen.gui.Renderer
import mcen.registry.net.CompileSource
import mcen.registry.net.SyncWorkspacePacket
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
            ImGui.sameLine(ImGui.getWindowWidth() - 180);
            if (ImGui.button("Compile ${Icons.Sun}")) {
                Registry.Net.sendToServer(
                    SyncWorkspacePacket(
                        explorer.workspace,
                        worldPos.position,
                        worldPos.world
                    )
                )
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
            ImGui.sameLine()
            if (ImGui.button("Clear ${Icons.HandPointDown}")) {
                scripts.messages.clear()
                scripts.errors.clear()

            }
        }

        if (action.isNotBlank()) {
            ImGui.openPopup("${action}_action")
            action = ""
        }


    }


}