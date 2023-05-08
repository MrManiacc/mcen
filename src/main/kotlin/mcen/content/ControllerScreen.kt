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

/**
 * This is the screen that that renders the viewports containing the code and node editor for the controller block.
 */
class ControllerScreen(level: Level, blockPos: BlockPos, private val controllerTile: ControllerTile) : RenderScreen() {
    // stores the current world position of the controller
    private val worldPos = WorldPos(blockPos, level.dimension())

    //provides a reference to all the active viewports within the controller screen
    private val viewports = Dockspace(
        arrayListOf(
            NodeViewport(), ScriptViewport(worldPos).apply {
                //Make sure to reset the text to what text is stored within the controller tile upon opening the screen
                editor.textLines = controllerTile.scriptSource.trimIndent().split("\n").toTypedArray()
            }, ConsoleViewport(worldPos),
            ExplorerViewport(controllerTile.workspace, worldPos),
        ), MenubarViewport(worldPos)
    )

    /**
     * Renders our frame containing the dockspace.
     */
    override fun Renderer.render() {
        dockspace("mainview", viewports)
    }

    /**
     * Callback to close the viewports guis states
     */
    override fun onClose() {
        super.onClose()
        viewports.onClose()
    }

}