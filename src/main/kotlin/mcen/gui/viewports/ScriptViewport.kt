package mcen.gui.viewports

import imgui.extension.texteditor.TextEditor
import imgui.extension.texteditor.TextEditorLanguageDefinition
import imgui.type.ImInt
import mcen.content.ControllerScreen
import mcen.content.internal.Registry
import mcen.content.internal.WorldPos
import mcen.gui.Renderer

class ScriptViewport(worldPos: WorldPos) : Viewport(worldPos, "scripts") {
    val messages = Registry.Console.getMessages(worldPos)
    val errors = Registry.Console.getErrors(worldPos)
    val editor = TextEditor().apply {
        val lang = TextEditorLanguageDefinition.lua()
        setLanguageDefinition(lang)
        setShowWhitespaces(false)
        setHandleKeyboardInputs(false)
    }

    override fun Renderer.dockspace() {
        codeFont {
            editor.render("TextEditor");
        }
        if (!editor.isHandleKeyboardInputsEnabled)
            editor.setHandleKeyboardInputs(true)
    }

    override fun Renderer.createDock(parentId: ImInt) {
        imgui.internal.ImGui.dockBuilderDockWindow(name, parentId.get())
    }

    override fun onClose() {
        editor.setHandleKeyboardInputs(false)
    }
}