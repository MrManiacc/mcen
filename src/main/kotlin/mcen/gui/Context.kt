package mcen.gui

import imgui.*
import imgui.extension.imnodes.ImNodes
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiConfigFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Paths


interface RendererBackend {
    val windowHandle: Long
    val glslVersion: String
}

@OnlyIn(Dist.CLIENT)
class RenderContext(private val backend: RendererBackend) {

    /**We only need to initialize once**/
    private var initialized = false

    /**This stores the glfw backend implementation for imgui**/
    val imGuiGlfw: ImGuiImplGlfw = ImGuiImplGlfw()

    /**This stores the opengl backend implementation for imgui**/
    val imGuiGl3: ImGuiImplGl3 = ImGuiImplGl3()

    lateinit var codeFont: ImFont
        private set

    /**
     * Initialize the render context using the supplied window handle and glsl version.
     */
    fun initialize() {
        if (initialized) return
        initialized = true
        ImGui.createContext();
        ImNodes.createContext()
        configureImGui()
        imGuiGlfw.init(backend.windowHandle, true);
        imGuiGl3.init(backend.glslVersion);
    }

    /**
     * Does all the configuration for imgui.
     */
    fun configureImGui() {
        configureStyles(ImGui.getStyle())
        configureFlags(ImGui.getIO())
        configureFonts(ImGui.getIO())
    }

    /**
     * This setups up the initial style for the ImGui context.
     */
    private fun configureStyles(style: ImGuiStyle) {
        style.windowRounding = 5.0f
        style.frameBorderSize = 2.5f
        ImGui.styleColorsDark()
        style.colors[ImGuiCol.FrameBg] = floatArrayOf(51f, 51f, 50f, 255f)
        style.colors[ImGuiCol.Tab] = floatArrayOf(117f, 70f, 153f, 255f)
        style.colors[ImGuiCol.TabActive] = floatArrayOf(130f, 68f, 178f, 255f)
        style.colors[ImGuiCol.FrameBg] = floatArrayOf(130f, 68f, 178f, 255f)
        style.colors[ImGuiCol.TabHovered] = floatArrayOf(87f, 37f, 125f, 255f)
        style.colors[ImGuiCol.FrameBgActive] = floatArrayOf(87f, 37f, 125f, 255f)
        style.colors[ImGuiCol.FrameBgHovered] = floatArrayOf(87f, 37f, 125f, 255f)
        style.colors[ImGuiCol.Button] = floatArrayOf(79f, 37f, 121f, 255f)
        style.colors[ImGuiCol.ButtonActive] = floatArrayOf(92f, 45f, 130f, 255f)
        style.colors[ImGuiCol.ButtonHovered] = floatArrayOf(79f, 37f, 121f, 255f)
        style.colors[ImGuiCol.TabUnfocused] = floatArrayOf(104f, 68f, 133f, 255f)
        style.colors[ImGuiCol.TabUnfocusedActive] = floatArrayOf(117f, 70f, 153f, 255f)
        style.colors[ImGuiCol.TitleBg] = floatArrayOf(70f, 69f, 71f, 255f)
        style.colors[ImGuiCol.TitleBgActive] = floatArrayOf(89f, 87f, 90f, 255f)
        style.colors[ImGuiCol.TitleBgCollapsed] = floatArrayOf(89f, 87f, 90f, 255f)
        style.colors[ImGuiCol.NavHighlight] = floatArrayOf(89f, 87f, 90f, 255f)
        style.colors[ImGuiCol.NavWindowingDimBg] = floatArrayOf(89f, 87f, 90f, 255f)
        style.colors[ImGuiCol.NavWindowingHighlight] = floatArrayOf(89f, 87f, 90f, 255f)
        style.colors[ImGuiCol.Header] = floatArrayOf(70f, 69f, 71f, 255f)
        style.colors[ImGuiCol.HeaderActive] = floatArrayOf(89f, 87f, 90f, 255f)
        style.colors[ImGuiCol.HeaderHovered] = floatArrayOf(89f, 87f, 90f, 255f)
        style.colors[ImGuiCol.MenuBarBg] = floatArrayOf(108f, 106f, 102f, 255f)
        style.colors[ImGuiCol.Border] = floatArrayOf(108f, 106f, 102f, 255f)
        style.colors[ImGuiCol.BorderShadow] = floatArrayOf(108f, 106f, 102f, 255f)
        style.colors[ImGuiCol.DockingPreview] = floatArrayOf(108f, 106f, 102f, 255f)
        style.colors[ImGuiCol.DockingEmptyBg] = floatArrayOf(89f, 87f, 90f, 255f)
        style.colors[ImGuiCol.WindowBg] = floatArrayOf(89f, 87f, 90f, 255f)
        style.tabBorderSize = 5f
        style.tabRounding = 0f
    }

    /**
     * This initializes the imgui default flags
     */
    private fun configureFlags(io: ImGuiIO) {
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)
        io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleViewports)
        io.iniFilename = null
    }

    /**
     * Initializes the imgui fonts
     */
    private fun configureFonts(io: ImGuiIO) {
        val rangesBuilder = ImFontGlyphRangesBuilder() // Glyphs ranges provide
        rangesBuilder.addRanges(io.fonts.glyphRangesDefault)
        rangesBuilder.addRanges(Icons.IconRange);
        rangesBuilder.addRanges(Ico.ICON_RANGE);
        val glyphRanges = rangesBuilder.buildRanges()
        val defaultConfig = ImFontConfig()
        defaultConfig.mergeMode = false
        io.fonts.addFontFromMemoryTTF(loadFromResources("futura light bt.ttf"), 21f, defaultConfig, glyphRanges)
        val iconFont = ImFontConfig()
        iconFont.mergeMode = true
        io.fonts.addFontFromMemoryTTF(
            loadFromResources("Font Awesome 6 Free-Regular-400.otf"),
            16f,
            iconFont,
            glyphRanges
        )
        io.fonts.addFontFromMemoryTTF(
            loadFromResources("codicon.ttf"),
            16f,
            iconFont,
            glyphRanges
        )
        val codeConfig = ImFontConfig()
        codeConfig.mergeMode = false
        this.codeFont = io.fonts.addFontFromMemoryTTF(loadFromResources("SourceCodePro-Regular.ttf"), 19f, defaultConfig, glyphRanges)
//        io.fonts.addFont(codeConfig)
//        io.fonts.addFont(iconFont)
        io.fonts.addFontDefault(defaultConfig)
        io.fonts.build();
        defaultConfig.destroy();
        codeConfig.destroy();
        iconFont.destroy()
    }

    inline fun render(renderCtx: Renderer.() -> Unit) {
        imGuiGlfw.newFrame();
        ImGui.newFrame()
        Renderer.apply(renderCtx)
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }


    /**
     * Destroy the imgui context
     */
    fun dispose() {
        if (!initialized) return
        imGuiGlfw.dispose()
        imGuiGl3.dispose()
        ImGui.destroyContext()
        ImNodes.destroyContext()
    }


    private fun loadFromResources(name: String): ByteArray? {
        return try {
            Files.readAllBytes(Paths.get(RenderContext::class.java.getResource("/fonts/$name")!!.toURI()))
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

}