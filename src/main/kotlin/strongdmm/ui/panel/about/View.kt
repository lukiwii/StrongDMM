package strongdmm.ui.panel.about

import imgui.ImGui.textWrapped
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 550f * Window.pointSize
        private val height: Float
            get() = 170f * Window.pointSize

        private const val TITLE: String = "About"
    }

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        ImGuiUtil.setNextWindowCentered(width, height)

        window(TITLE, state.isOpened) {
            textWrapped(state.aboutText)
        }
    }
}
