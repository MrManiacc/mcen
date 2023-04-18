package mcen.gui.viewports

import imgui.ImGui
import mcen.content.internal.WorldPos
import mcen.gui.Renderer

class MenubarViewport(worldPos: WorldPos) : Viewport(worldPos, "menubar") {
    override fun Renderer.dockspace() {
        val scripts = getViewport<ScriptViewport>() ?: return
        mainMenuBar {
            menu("File") {
                menuItem("Save", "Ctrl-S") {
                    val text = scripts.editor.text
                    println("SAVING TEXT: \n$text")
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
            menu("Help") {

            }
        }
    }
}