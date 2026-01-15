import { computed } from 'vue'
import { usePlanEditorStore } from '@/stores/planEditor'
import type { PlanMarker, PlanShape } from '@/api/raidplan'

/**
 * Composable for handling canvas editor interactions.
 *
 * Provides canvas props and event handlers that integrate with the planEditor store.
 */
export function useCanvasEditor() {
  const store = usePlanEditorStore()

  // Canvas props derived from store state
  const canvasProps = computed(() => ({
    markers: store.currentStepData?.markers ?? [],
    shapes: store.currentStepData?.shapes ?? [],
    zoom: store.zoom,
    panX: store.panX,
    panY: store.panY,
    selectedMarkerIndex: store.selectedMarkerIndex,
    selectedShapeIndex: store.selectedShapeIndex,
    currentTool: store.currentTool,
  }))

  // Whether delete action is available
  const canDelete = computed(() => {
    return store.selectedMarkerIndex >= 0 || store.selectedShapeIndex >= 0
  })

  /**
   * Handle click on canvas background.
   * Adds marker/shape or clears selection depending on current tool.
   */
  function handleCanvasClick(position: { x: number; y: number }) {
    if (store.currentTool === 'marker' && store.selectedMarker) {
      store.addMarker({
        type: store.selectedMarker,
        x: position.x,
        y: position.y,
        color: store.selectedColor,
      })
    } else if (store.currentTool === 'shape' && store.selectedShape) {
      // For shapes, we'd typically need two clicks for start/end points
      // This is a simplified version that creates at click position
      store.addShape({
        shapeType: store.selectedShape,
        x1: position.x,
        y1: position.y,
        x2: position.x + 10,
        y2: position.y + 10,
        radius: store.selectedShape === 'CIRCLE' ? 10 : undefined,
        color: store.selectedColor,
        strokeWidth: 2,
      })
    } else if (store.currentTool === 'select') {
      // Clear selection when clicking on empty space
      store.selectedMarkerIndex = -1
      store.selectedShapeIndex = -1
    }
    // Pan tool does nothing on click (pan is handled by drag)
  }

  /**
   * Handle click on a marker.
   */
  function handleMarkerClick(index: number, _marker: PlanMarker) {
    store.selectedMarkerIndex = index
    store.selectedShapeIndex = -1
    store.currentTool = 'select'
  }

  /**
   * Handle click on a shape.
   */
  function handleShapeClick(index: number, _shape: PlanShape) {
    store.selectedShapeIndex = index
    store.selectedMarkerIndex = -1
    store.currentTool = 'select'
  }

  /**
   * Handle zoom level change.
   */
  function handleZoomChange(zoom: number) {
    store.setZoom(zoom)
  }

  /**
   * Handle pan position change.
   */
  function handlePanChange(pan: { x: number; y: number }) {
    store.setPan(pan.x, pan.y)
  }

  /**
   * Delete currently selected marker or shape.
   */
  function deleteSelected() {
    if (store.selectedMarkerIndex >= 0) {
      store.removeMarker(store.selectedMarkerIndex)
    } else if (store.selectedShapeIndex >= 0) {
      store.removeShape(store.selectedShapeIndex)
    }
  }

  return {
    canvasProps,
    canDelete,
    handleCanvasClick,
    handleMarkerClick,
    handleShapeClick,
    handleZoomChange,
    handlePanChange,
    deleteSelected,
  }
}
