package mcen.gui.viewports

data class Dockspace(val viewports: List<Viewport>, val mainViewport: Viewport = Viewport.EmptyViewport) {
    fun onClose() {
        viewports.forEach { it.onClose() }
        mainViewport.onClose()
    }

    init {
        viewports.forEach {
            it.parent = this
        }
        mainViewport.parent = this
    }

}