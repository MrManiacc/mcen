package mcen.gui.viewports

import imgui.type.ImInt
import mcen.content.internal.WorldPos
import mcen.content.internal.WorldPos.Companion.Identity
import mcen.gui.Renderer

abstract class Viewport(
    open val name: String,
) {
    lateinit var parent: Dockspace
    protected fun getViewport(name: String) = parent.viewports.find { it.name == name } ?: Viewport.EmptyViewport
    protected inline fun <reified T : Viewport> getViewport(): T? = parent.viewports.find { it is T } as T?

    fun dockspace() =
        Renderer.dockspace()

    fun createDock(parentId: ImInt) =
        Renderer.createDock(parentId)

    protected abstract fun Renderer.dockspace()
    protected open fun Renderer.createDock(parentId: ImInt) = Unit

    open fun onClose() = Unit
    open fun popup() = Unit

    object EmptyViewport : Viewport("Empty") {
        override fun Renderer.dockspace() {
        }

        override fun Renderer.createDock(parentId: ImInt) {
        }
    }
}
