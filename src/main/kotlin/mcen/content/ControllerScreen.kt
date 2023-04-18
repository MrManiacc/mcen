package mcen.content

import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.extension.texteditor.TextEditor
import imgui.extension.texteditor.TextEditorLanguageDefinition
import imgui.flag.ImGuiCol
import mcen.content.internal.Registry
import mcen.content.internal.WorldPos
import mcen.gui.*
import mcen.gui.viewports.*
import mcen.registry.net.CompileSource
import mcen.scripting.console.LogLevel
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import org.luaj.vm2.ast.Exp.NameExp
import org.luaj.vm2.ast.Visitor
import org.luaj.vm2.parser.LuaParser
import org.luaj.vm2.parser.ParseException
import org.luaj.vm2.parser.TokenMgrError
import org.lwjgl.system.Platform


class ControllerScreen(private val level: Level, private val blockPos: BlockPos, private val controllerTile: ControllerTile) :
    RenderScreen(Renderer) {
    private val worldPos = WorldPos(blockPos, level.dimension())
    private val viewports = Dockspace(arrayListOf(ScriptViewport(worldPos).apply {
        editor.textLines = controllerTile.scriptSource.trimIndent().split("\n").toTypedArray()
    },NodeViewport(worldPos), ConsoleViewport(worldPos)), MenubarViewport(worldPos))


    override fun onClose() {
        super.onClose()
        viewports.onClose()
    }

    /**
     * Renders our frame
     */
    override fun Renderer.render() {
        dockspace("mainview", viewports)
    }


    override fun renderToBuffer(stack: PoseStack) {
        renderInventory(controllerTile.inventory, stack)
    }

    private fun Renderer.renderBrowser() {

        ImGui.image(super.targetId, 512f, 400f, 0f, 1f, 1f, 0f)

//        text("${Icons.HandPointRight} todo soon")
//        folderNode("Project", {
//            editor.text = "local blockpos = $blockPos\n\n"
//        }, {}) {
//            folderNode("Project", {}, {}) {
//            }
//        }
    }


    companion object {

        /**
         * Stores our imgui renderer, an initializes the backend upon loading completion on the clietn
         */
        val Renderer = RenderContext(object : RendererBackend {
            override val windowHandle: Long
                get() = Minecraft.getInstance().window.window
            override val glslVersion: String
                get() = if (Platform.get() != Platform.MACOSX) "#version 120" else "#version 410"
        })


    }
}