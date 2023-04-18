package mcen.gui

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiTreeNodeFlags
import imgui.flag.ImGuiWindowFlags
import imgui.internal.flag.ImGuiDockNodeFlags
import imgui.internal.flag.ImGuiDockNodeFlags.*
import imgui.type.ImBoolean
import imgui.type.ImInt
import mcen.content.ControllerScreen
import mcen.gui.viewports.Dockspace
import mcen.gui.viewports.Viewport

object Renderer {

    fun text(text: String) = ImGui.text(text)

    fun render(call: Renderer.() -> Unit) = this.apply(call)

    inline fun begin(name: String, flags: Int, window: Renderer.() -> Unit) {
        if (ImGui.begin(name, flags))
            this.apply(window)
        ImGui.end()
    }

    inline fun begin(name: String, window: Renderer.() -> Unit) {
        if (ImGui.begin(name))
            this.apply(window)
        ImGui.end()
    }

    inline fun treeNode(name: String, window: Renderer.() -> Unit): Boolean {
        if (ImGui.treeNode(name)) {
            this.apply(window)
            ImGui.treePop()
            return true
        }
        return false
    }

    inline fun treeNodeEx(name: String, flags: Int, window: Renderer.() -> Unit): Boolean {
        if (ImGui.treeNodeEx(name, flags)) {
            this.apply(window)
            ImGui.treePop()
            return true
        }
        return false
    }

    inline fun codeFont(window: Renderer.() -> Unit) {
        ImGui.pushFont(ControllerScreen.Renderer.codeFont)
        this.apply(window)
        ImGui.popFont()
    }

    inline fun drawFolderCreationContext(createFolder: Renderer.() -> Unit, createFile: Renderer.() -> Unit) {
        ImGui.sameLine()
        ImGui.pushStyleColor(ImGuiCol.Button, 0.5f, 0.5f, 0.5f, 0.0f)
        ImGui.pushStyleVar(ImGuiStyleVar.CellPadding, 0f, 0f)
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 0f, 0f)
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
        if (ImGui.button(Icons.PlusSquare)) {
            //Add item
            ImGui.openPopup("ElementList")
        }
        ImGui.popStyleColor()
        ImGui.popStyleVar(3)
        popup("ElementList") {
            menuItem("Create Folder", "", createFolder)
            menuItem("Create File", "", createFile)
        }
//        if (ImGui.isItemHovered()) ImGui.setTooltip("Create element");

    }

    inline fun folderNode(name: String, createFolder: Renderer.() -> Unit, createFile: Renderer.() -> Unit, window: Renderer.() -> Unit) {
        treeNodeEx("${Icons.Folder} $name", ImGuiTreeNodeFlags.AllowItemOverlap) {
            if (ImGui.isItemClicked(1)) {
                ImGui.openPopup("Create element")
            }
            drawFolderCreationContext(createFolder, createFile)
            window()
        }
    }

    inline fun popup(name: String, window: Renderer.() -> Unit) {
        if (ImGui.beginPopup(name)) {
            this.apply(window)
            ImGui.endPopup()
        }
    }

    inline fun begin(name: String, open: ImBoolean, window: Renderer.() -> Unit) {
        if (ImGui.begin(name, open))
            this.apply(window)
        ImGui.end()
    }

    inline fun menuBar(window: Renderer.() -> Unit) {
        if (ImGui.beginMenuBar()) {
            this.apply(window)
            ImGui.endMenuBar()
        }
    }

    inline fun mainMenuBar(window: Renderer.() -> Unit) {
        if (ImGui.beginMainMenuBar()) {
            this.apply(window)
            ImGui.endMainMenuBar()
        }
    }

    inline fun menu(name: String, window: Renderer.() -> Unit) {
        if (ImGui.beginMenu(name)) {
            this.apply(window)
            ImGui.endMenu()
        }
    }

    inline fun menuItem(name: String, shortcut: String, selected: Boolean, window: Renderer.() -> Unit) {
        if (ImGui.menuItem(name, shortcut, selected)) {
            this.apply(window)
        }
    }

    inline fun menuItem(name: String, shortcut: String, window: Renderer.() -> Unit) {
        if (ImGui.menuItem(name, shortcut)) {
            this.apply(window)
        }
    }


//    inline fun dockspace(name: String, dockspace: Renderer.() -> Unit) {
//        val flags = ImGuiWindowFlags.NoNavFocus or
//                ImGuiWindowFlags.NoTitleBar or
//                ImGuiWindowFlags.NoCollapse or
//                ImGuiWindowFlags.NoResize or
//                ImGuiWindowFlags.NoMove or
//                ImGuiWindowFlags.NoBringToFrontOnFocus
//        val height = ImGui.getFrameHeight()
//        val size = ImGui.getIO().displaySize
//        val viewport = ImGui.getMainViewport()
//        ImGui.setNextWindowPos(0f, height)
//        ImGui.setNextWindowSize(size.x, size.y - height)
//        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
//        ImGui.begin("Window##$name", flags)
//        ImGui.setNextWindowViewport(viewport.id)
//        ImGui.popStyleVar()
//        var dockspaceID = ImGui.getID(name)
//        val node = imgui.internal.ImGui.dockBuilderGetNode(dockspaceID)
//        if (node == null || node.ptr == 0L || node.id == 0) //Null ptr? it we should now create?
//            createDock(name, dockspaceID)
//        dockspaceID = ImGui.getID(name)
//        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 0f, 0f)
//        ImGui.dockSpace(dockspaceID, 0f, 0f, flags)
//        this.apply(dockspace)
//        ImGui.popStyleVar()
//        ImGui.end()
//    }

    fun dockspace(
        name: String,
        dockspace: Dockspace
    ) {
        val flags = ImGuiWindowFlags.NoNavFocus or
                ImGuiWindowFlags.NoTitleBar or
                ImGuiWindowFlags.NoCollapse or
                ImGuiWindowFlags.NoResize or
                ImGuiWindowFlags.NoMove or
                ImGuiWindowFlags.NoBringToFrontOnFocus
        dockspace.mainViewport.dockspace()
        val height = ImGui.getFrameHeight()
        val size = ImGui.getIO().displaySize
        val viewport = ImGui.getMainViewport()
        ImGui.setNextWindowPos(0f, height)
        ImGui.setNextWindowSize(size.x, size.y - height)
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
        ImGui.begin("Window##$name", flags)
        ImGui.setNextWindowViewport(viewport.id)
        ImGui.popStyleVar()
        var dockspaceID = ImGui.getID(name)
        val node = imgui.internal.ImGui.dockBuilderGetNode(dockspaceID)
        if (node == null || node.ptr == 0L || node.id == 0) //Null ptr? it we should now create?
            createDock(name, dockspace.viewports)
        dockspaceID = ImGui.getID(name)
        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 200f, 150f);
        ImGui.dockSpace(dockspaceID, 0f, 0f, NoWindowMenuButton or NoCloseButton)
        ImGui.end()
        ImGui.popStyleVar()

        dockspace.viewports.forEach { viewport ->
            ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 200f, 150f);
            ImGui.begin(viewport.name, ImGuiWindowFlags.NoTitleBar)
            viewport.dockspace()
            ImGui.end()
            ImGui.popStyleVar()

        }
    }

//    inline fun dockspace(
//        name: String,
//        scripts: Renderer.() -> Unit,
//        browser: Renderer.() -> Unit,
//        console: Renderer.() -> Unit,
//        dockspace: Renderer.() -> Unit
//    ) {
//        val flags = ImGuiWindowFlags.NoNavFocus or
//                ImGuiWindowFlags.NoTitleBar or
//                ImGuiWindowFlags.NoCollapse or
//                ImGuiWindowFlags.NoResize or
//                ImGuiWindowFlags.NoMove or
//                ImGuiWindowFlags.NoBringToFrontOnFocus
//        this.apply(dockspace)
//        val height = ImGui.getFrameHeight()
//        val size = ImGui.getIO().displaySize
//        val viewport = ImGui.getMainViewport()
//        ImGui.setNextWindowPos(0f, height)
//        ImGui.setNextWindowSize(size.x, size.y - height)
//        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
//        ImGui.begin("Window##$name", flags)
//        ImGui.setNextWindowViewport(viewport.id)
//        ImGui.popStyleVar()
//        var dockspaceID = ImGui.getID(name)
//        val node = imgui.internal.ImGui.dockBuilderGetNode(dockspaceID)
//        if (node == null || node.ptr == 0L || node.id == 0) //Null ptr? it we should now create?
//            createDock(name)
//        dockspaceID = ImGui.getID(name)
//        ImGui.pushStyleVar(ImGuiStyleVar.WindowMinSize, 200f, 150f);
//        ImGui.dockSpace(dockspaceID, 0f, 0f, NoWindowMenuButton or NoCloseButton)
//        ImGui.end()
//        ImGui.begin("Browser", ImGuiWindowFlags.NoTitleBar)
//        this.apply(browser)
//        ImGui.end()
//        ImGui.begin("Scripts", ImGuiWindowFlags.NoDecoration or ImGuiWindowFlags.NoBringToFrontOnFocus)
//        this.apply(scripts)
//        ImGui.end()
//        ImGui.begin(
//            "Console",
//            ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoBringToFrontOnFocus or ImGuiWindowFlags.MenuBar
//        )
//        this.apply(console)
//        ImGui.end()
//        ImGui.popStyleVar()
//    }

    private fun createDock(name: String, viewports: List<Viewport>) {
        val viewport = ImGui.getWindowViewport()
        val dockspaceID = ImGui.getID(name)
        imgui.internal.ImGui.dockBuilderRemoveNode(dockspaceID)
        imgui.internal.ImGui.dockBuilderAddNode(dockspaceID, ImGuiDockNodeFlags.DockSpace or ImGuiDockNodeFlags.NoTabBar)
        imgui.internal.ImGui.dockBuilderSetNodeSize(dockspaceID, viewport.sizeX, viewport.sizeY)
        val dockMainId = ImInt(dockspaceID)
        viewports.forEach { viewport ->
            viewport.createDock(dockMainId)
        }
        imgui.internal.ImGui.dockBuilderFinish(dockspaceID)
    }

//
//    /**
//     * This internally creates the dock when it's not present.
//     */
//    fun createDock(name: String, dockspaceID: Int) {
//        val viewport = ImGui.getWindowViewport()
//        val dockspaceID = ImGui.getID(name)
//        imgui.internal.ImGui.dockBuilderRemoveNode(dockspaceID)
//        imgui.internal.ImGui.dockBuilderAddNode(dockspaceID, ImGuiDockNodeFlags.DockSpace or ImGuiDockNodeFlags.NoTabBar)
//        imgui.internal.ImGui.dockBuilderSetNodeSize(dockspaceID, viewport.sizeX, viewport.sizeY)
//        val dockMainId = ImInt(dockspaceID)
//        val outID = ImInt(dockspaceID)
//        val dockLeft: Int =
//            imgui.internal.ImGui.dockBuilderSplitNode(dockMainId.get(), ImGuiDir.Left, 0.23f, outID, dockMainId)
//        val dockDown: Int =
//            imgui.internal.ImGui.dockBuilderSplitNode(dockMainId.get(), ImGuiDir.Down, 0.35f, null, dockMainId)
//        imgui.internal.ImGui.dockBuilderDockWindow("Scripts", dockMainId.get())
//        imgui.internal.ImGui.dockBuilderDockWindow("Browser", dockLeft)
//        imgui.internal.ImGui.dockBuilderDockWindow("Console", dockDown)
//        imgui.internal.ImGui.dockBuilderFinish(dockspaceID)
//    }

}