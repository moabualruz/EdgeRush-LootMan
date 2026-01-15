import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCanvasEditor } from './useCanvasEditor'
import { usePlanEditorStore } from '@/stores/planEditor'
import type { RaidPlan } from '@/api/raidplan'

describe('useCanvasEditor', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const createMockPlan = (): RaidPlan => ({
    id: 'test-plan',
    guildId: 'test-guild',
    encounterId: 2902,
    encounterName: 'Queen Ansurek',
    name: 'Test Plan',
    steps: [
      {
        order: 0,
        notes: 'Phase 1',
        markers: [{ type: 'SKULL', x: 50, y: 50 }],
        shapes: [{ shapeType: 'CIRCLE', x1: 25, y1: 25, radius: 10, strokeWidth: 2 }],
      },
    ],
    visibility: 'GUILD',
    shareToken: undefined,
    createdBy: 1,
    createdAt: '2026-01-15T10:00:00Z',
    updatedAt: '2026-01-15T12:00:00Z',
  })

  describe('initialization', () => {
    it('should return canvas props from store state', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      const { canvasProps } = useCanvasEditor()

      expect(canvasProps.value.markers).toHaveLength(1)
      expect(canvasProps.value.shapes).toHaveLength(1)
    })

    it('should return empty arrays when no plan', () => {
      const { canvasProps } = useCanvasEditor()

      expect(canvasProps.value.markers).toHaveLength(0)
      expect(canvasProps.value.shapes).toHaveLength(0)
    })
  })

  describe('handleCanvasClick', () => {
    it('should add marker when marker tool is selected', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.selectMarkerType('CROSS')

      const { handleCanvasClick } = useCanvasEditor()
      handleCanvasClick({ x: 75, y: 25 })

      expect(store.currentStepData?.markers).toHaveLength(2)
      expect(store.currentStepData?.markers[1]).toEqual({
        type: 'CROSS',
        x: 75,
        y: 25,
        color: '#ffffff',
      })
    })

    it('should clear selection when select tool is active', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.selectedMarkerIndex = 0
      store.setTool('select')

      const { handleCanvasClick } = useCanvasEditor()
      handleCanvasClick({ x: 75, y: 25 })

      expect(store.selectedMarkerIndex).toBe(-1)
    })

    it('should do nothing when pan tool is active', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.setTool('pan')
      const initialMarkerCount = store.currentStepData?.markers.length ?? 0

      const { handleCanvasClick } = useCanvasEditor()
      handleCanvasClick({ x: 75, y: 25 })

      expect(store.currentStepData?.markers).toHaveLength(initialMarkerCount)
    })
  })

  describe('handleMarkerClick', () => {
    it('should select marker', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      const { handleMarkerClick } = useCanvasEditor()
      handleMarkerClick(0, { type: 'SKULL', x: 50, y: 50 })

      expect(store.selectedMarkerIndex).toBe(0)
      expect(store.currentTool).toBe('select')
    })
  })

  describe('handleShapeClick', () => {
    it('should select shape', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      const { handleShapeClick } = useCanvasEditor()
      handleShapeClick(0, { shapeType: 'CIRCLE', x1: 25, y1: 25, radius: 10, strokeWidth: 2 })

      expect(store.selectedShapeIndex).toBe(0)
      expect(store.currentTool).toBe('select')
    })
  })

  describe('handleZoomChange', () => {
    it('should update zoom in store', () => {
      const store = usePlanEditorStore()

      const { handleZoomChange } = useCanvasEditor()
      handleZoomChange(1.5)

      expect(store.zoom).toBe(1.5)
    })
  })

  describe('handlePanChange', () => {
    it('should update pan in store', () => {
      const store = usePlanEditorStore()

      const { handlePanChange } = useCanvasEditor()
      handlePanChange({ x: 100, y: 50 })

      expect(store.panX).toBe(100)
      expect(store.panY).toBe(50)
    })
  })

  describe('deleteSelected', () => {
    it('should delete selected marker', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.selectedMarkerIndex = 0

      const { deleteSelected } = useCanvasEditor()
      deleteSelected()

      expect(store.currentStepData?.markers).toHaveLength(0)
      expect(store.selectedMarkerIndex).toBe(-1)
    })

    it('should delete selected shape', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.selectedShapeIndex = 0

      const { deleteSelected } = useCanvasEditor()
      deleteSelected()

      expect(store.currentStepData?.shapes).toHaveLength(0)
      expect(store.selectedShapeIndex).toBe(-1)
    })

    it('should do nothing when nothing selected', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      const { deleteSelected } = useCanvasEditor()
      deleteSelected()

      expect(store.currentStepData?.markers).toHaveLength(1)
      expect(store.currentStepData?.shapes).toHaveLength(1)
    })
  })

  describe('canDelete', () => {
    it('should return true when marker is selected', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.selectedMarkerIndex = 0

      const { canDelete } = useCanvasEditor()

      expect(canDelete.value).toBe(true)
    })

    it('should return true when shape is selected', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.selectedShapeIndex = 0

      const { canDelete } = useCanvasEditor()

      expect(canDelete.value).toBe(true)
    })

    it('should return false when nothing selected', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      const { canDelete } = useCanvasEditor()

      expect(canDelete.value).toBe(false)
    })
  })
})
