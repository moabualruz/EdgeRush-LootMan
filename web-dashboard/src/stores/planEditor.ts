import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { RaidPlan, PlanStep, PlanMarker, PlanShape, MarkerType, ShapeType } from '@/api/raidplan'

// Deep clone helper that works in JSDOM (deepClone fails on Vue refs)
function deepClone<T>(obj: T): T {
  return JSON.parse(JSON.stringify(obj))
}

/**
 * Pinia store for managing raid plan editor state.
 *
 * Handles plan data, current step, tool selection, undo/redo history,
 * and viewport (zoom/pan) state.
 */
export const usePlanEditorStore = defineStore('planEditor', () => {
  // Plan state
  const plan = ref<RaidPlan | null>(null)
  const currentStep = ref(0)
  const isDirty = ref(false)

  // Tool state
  const currentTool = ref<'select' | 'pan' | 'marker' | 'shape'>('select')
  const selectedMarker = ref<MarkerType | undefined>()
  const selectedShape = ref<ShapeType | undefined>()
  const selectedColor = ref('#ffffff')

  // Selection state
  const selectedMarkerIndex = ref(-1)
  const selectedShapeIndex = ref(-1)

  // Viewport state
  const zoom = ref(1)
  const panX = ref(0)
  const panY = ref(0)

  // Undo/redo history
  const undoStack = ref<RaidPlan[]>([])
  const redoStack = ref<RaidPlan[]>([])
  const maxHistorySize = 50

  // Computed
  const currentStepData = computed((): PlanStep | undefined => {
    return plan.value?.steps[currentStep.value]
  })

  const canUndo = computed(() => undoStack.value.length > 0)
  const canRedo = computed(() => redoStack.value.length > 0)

  // Actions
  function setPlan(newPlan: RaidPlan) {
    plan.value = deepClone(newPlan)
    currentStep.value = 0
    isDirty.value = false
    selectedMarkerIndex.value = -1
    selectedShapeIndex.value = -1
    undoStack.value = []
    redoStack.value = []
  }

  function goToStep(index: number) {
    if (!plan.value) return
    currentStep.value = Math.max(0, Math.min(index, plan.value.steps.length - 1))
    selectedMarkerIndex.value = -1
    selectedShapeIndex.value = -1
  }

  function setTool(tool: 'select' | 'pan' | 'marker' | 'shape') {
    currentTool.value = tool
    if (tool === 'select' || tool === 'pan') {
      selectedMarker.value = undefined
      selectedShape.value = undefined
    }
  }

  function selectMarkerType(marker: MarkerType) {
    selectedMarker.value = marker
    selectedShape.value = undefined
    currentTool.value = 'marker'
  }

  function selectShapeType(shape: ShapeType) {
    selectedShape.value = shape
    selectedMarker.value = undefined
    currentTool.value = 'shape'
  }

  function saveToHistory() {
    if (!plan.value) return
    undoStack.value.push(deepClone(plan.value))
    if (undoStack.value.length > maxHistorySize) {
      undoStack.value.shift()
    }
    redoStack.value = []
  }

  function addMarker(marker: PlanMarker) {
    if (!plan.value || !currentStepData.value) return
    saveToHistory()
    plan.value.steps[currentStep.value].markers.push(marker)
    isDirty.value = true
  }

  function removeMarker(index: number) {
    if (!plan.value || !currentStepData.value) return
    saveToHistory()
    plan.value.steps[currentStep.value].markers.splice(index, 1)
    if (selectedMarkerIndex.value === index) {
      selectedMarkerIndex.value = -1
    } else if (selectedMarkerIndex.value > index) {
      selectedMarkerIndex.value--
    }
    isDirty.value = true
  }

  function addShape(shape: PlanShape) {
    if (!plan.value || !currentStepData.value) return
    saveToHistory()
    plan.value.steps[currentStep.value].shapes.push(shape)
    isDirty.value = true
  }

  function removeShape(index: number) {
    if (!plan.value || !currentStepData.value) return
    saveToHistory()
    plan.value.steps[currentStep.value].shapes.splice(index, 1)
    if (selectedShapeIndex.value === index) {
      selectedShapeIndex.value = -1
    } else if (selectedShapeIndex.value > index) {
      selectedShapeIndex.value--
    }
    isDirty.value = true
  }

  function undo() {
    if (!canUndo.value || !plan.value) return
    redoStack.value.push(deepClone(plan.value))
    plan.value = undoStack.value.pop()!
    isDirty.value = true
  }

  function redo() {
    if (!canRedo.value || !plan.value) return
    undoStack.value.push(deepClone(plan.value))
    plan.value = redoStack.value.pop()!
    isDirty.value = true
  }

  function setZoom(newZoom: number) {
    zoom.value = Math.max(0.5, Math.min(3, newZoom))
  }

  function setPan(x: number, y: number) {
    panX.value = x
    panY.value = y
  }

  function resetView() {
    zoom.value = 1
    panX.value = 0
    panY.value = 0
  }

  function reset() {
    plan.value = null
    currentStep.value = 0
    isDirty.value = false
    currentTool.value = 'select'
    selectedMarker.value = undefined
    selectedShape.value = undefined
    selectedColor.value = '#ffffff'
    selectedMarkerIndex.value = -1
    selectedShapeIndex.value = -1
    zoom.value = 1
    panX.value = 0
    panY.value = 0
    undoStack.value = []
    redoStack.value = []
  }

  return {
    // State
    plan,
    currentStep,
    isDirty,
    currentTool,
    selectedMarker,
    selectedShape,
    selectedColor,
    selectedMarkerIndex,
    selectedShapeIndex,
    zoom,
    panX,
    panY,

    // Computed
    currentStepData,
    canUndo,
    canRedo,

    // Actions
    setPlan,
    goToStep,
    setTool,
    selectMarkerType,
    selectShapeType,
    addMarker,
    removeMarker,
    addShape,
    removeShape,
    undo,
    redo,
    setZoom,
    setPan,
    resetView,
    reset,
  }
})
