package strongdmm.service.tool

import strongdmm.Service
import strongdmm.byond.dmm.MapArea
import strongdmm.byond.dmm.MapPos
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerToolsService
import strongdmm.service.tool.select.SelectComplexTool
import strongdmm.service.tool.tile.TileComplexTool
import strongdmm.util.OUT_OF_BOUNDS

class ToolsService : Service, EventHandler {
    private var currentTool: Tool = TileComplexTool()
    private var currentMapPos: MapPos = MapPos(OUT_OF_BOUNDS, OUT_OF_BOUNDS)

    private var isMapOpened: Boolean = false
    private var selectedTileItem: TileItem? = null

    init {
        consumeEvent(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(Reaction.MapMouseDragStarted::class.java, ::handleMapMouseDragStarted)
        consumeEvent(Reaction.MapMouseDragStopped::class.java, ::handleMapMouseDragStopped)
        consumeEvent(Reaction.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapZSelectedChanged::class.java, ::handleSelectedMapZSelectedChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(TriggerToolsService.ChangeTool::class.java, ::handleChangeTool)
        consumeEvent(TriggerToolsService.ResetTool::class.java, ::handleResetTool)
        consumeEvent(TriggerToolsService.FetchSelectedArea::class.java, ::handleFetchSelectedArea)
        consumeEvent(TriggerToolsService.SelectArea::class.java, ::handleSelectArea)
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        currentMapPos = event.body
        if (currentTool.isActive && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onMapPosChanged(currentMapPos)
        }
    }

    private fun handleMapMouseDragStarted() {
        if (isMapOpened && currentMapPos.x != OUT_OF_BOUNDS && currentMapPos.y != OUT_OF_BOUNDS) {
            currentTool.onStart(currentMapPos)
        }
    }

    private fun handleMapMouseDragStopped() {
        if (currentTool.isActive) {
            currentTool.onStop()
        }
    }

    private fun handleSelectedTileItemChanged(event: Event<TileItem?, Unit>) {
        selectedTileItem = event.body
        currentTool.onTileItemSwitch(event.body)
    }

    private fun handleSelectedMapChanged() {
        isMapOpened = true
        currentTool.reset()
    }

    private fun handleSelectedMapZSelectedChanged() {
        currentTool.reset()
    }

    private fun handleSelectedMapClosed() {
        isMapOpened = false
        currentTool.reset()
    }

    private fun handleEnvironmentReset() {
        isMapOpened = false
        currentTool.destroy()
        currentTool.onTileItemSwitch(null)
    }

    private fun handleChangeTool(event: Event<ToolType, Unit>) {
        currentTool.destroy()
        currentTool = event.body.createTool()
        currentTool.onTileItemSwitch(selectedTileItem)
        sendEvent(Reaction.SelectedToolChanged(event.body))
    }

    private fun handleResetTool() {
        currentTool.reset()
    }

    private fun handleFetchSelectedArea(event: Event<Unit, MapArea>) {
        val selectedArea = if (currentTool is SelectComplexTool) {
            currentTool.getSelectedArea()
        } else {
            MapArea(currentMapPos.x, currentMapPos.y, currentMapPos.x, currentMapPos.y)
        }

        if (selectedArea.isNotOutOfBounds()) {
            event.reply(selectedArea)
        }
    }

    private fun handleSelectArea(event: Event<MapArea, Unit>) {
        sendEvent(TriggerToolsService.ChangeTool(ToolType.SELECT))
        (currentTool as SelectComplexTool).selectArea(event.body)
    }
}
