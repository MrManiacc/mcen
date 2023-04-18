package mcen.gui.viewports

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImInt
import mcen.content.ControllerScreen
import mcen.content.internal.Registry
import mcen.content.internal.WorldPos
import mcen.gui.Icons
import mcen.gui.Renderer
import mcen.registry.net.CompileSource
import mcen.scripting.console.LogLevel
import org.luaj.vm2.ast.Exp
import org.luaj.vm2.ast.Visitor
import org.luaj.vm2.parser.LuaParser
import org.luaj.vm2.parser.ParseException
import org.luaj.vm2.parser.TokenMgrError

class ConsoleViewport(worldPos: WorldPos) : Viewport(worldPos, "console") {
    private var updated = false

    init {
        Registry.Console.addCallback(worldPos) {
            updated = true
        }
    }

    private fun parseScript(script: String): Boolean {
        val scripts = getViewport<ScriptViewport>() ?: return false
        try {
            val parser = LuaParser(script.byteInputStream())
            val chunk = parser.Chunk()
            chunk.accept(object : Visitor() {
                override fun visit(exp: Exp.NameExp) {
                }
            })
            scripts.editor.setErrorMarkers(emptyMap())
        } catch (e: ParseException) {
            println(e.message)
            if (e.currentToken != null) {
                scripts.editor.setErrorMarkers(hashMapOf<Int, String>(e.currentToken.beginLine to e.message!!))
            }
            return false
        } catch (e: TokenMgrError) {
            if (e.message != null) {
                scripts.editor.setErrorMarkers(hashMapOf<Int, String>(1 to e.message!!))
                return false
            }
        }
        return true
    }

    override fun Renderer.dockspace() {
        val scripts = getViewport<ScriptViewport>() ?: return
        if (scripts.errors.isNotEmpty()) {
            scripts.editor.setErrorMarkers(scripts.errors)
        }
//        menuBar {
        ImGui.pushStyleColor(ImGuiCol.Button, 45, 46, 46, 255)
        if (ImGui.button("Compile ${Icons.Sun}")) {
            if (parseScript(scripts.editor.text)) {
                scripts.errors.clear()
                Registry.Net.sendToServer(CompileSource(worldPos.position, worldPos.world, scripts.editor.text))
            }
        }
        ImGui.sameLine()
        if (ImGui.button("Clear ${Icons.HandPointDown}")) {
            scripts.messages.clear()
        }
        ImGui.popStyleColor()
//        }
        if (ImGui.beginChild("console")) {
            for (message in scripts.messages) {
                val time = message.second.substringBefore('-')
                val msg = message.second.substringAfter('-')
                ImGui.textDisabled(time)
                ImGui.sameLine()
                when (message.first) {
                    LogLevel.Info -> ImGui.text(msg)
                    LogLevel.Warn -> ImGui.textColored(242 / 255f, 183 / 255f, 19 / 255f, 1.0f, msg)
                    LogLevel.Debug -> ImGui.textColored(13 / 255f, 222 / 255f, 52 / 255f, 1.0f, msg)
                    LogLevel.Error -> ImGui.textColored(242 / 255f, 35 / 255f, 7 / 255f, 1.0f, msg)
                }
            }
            if (updated) {
                ImGui.setScrollHereY()
                updated = false
            }
        }

        ImGui.endChild()


    }

    override fun Renderer.createDock(parentId: ImInt) {
        val dockDown: Int =
            imgui.internal.ImGui.dockBuilderSplitNode(parentId.get(), ImGuiDir.Down, 0.35f, null, parentId)
        imgui.internal.ImGui.dockBuilderDockWindow(name, dockDown)
    }

    override fun onClose() {
        Registry.Console.removeCallback(worldPos)
    }


}