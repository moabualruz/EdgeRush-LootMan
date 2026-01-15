import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePlanEditorStore } from './planEditor'
import type { RaidPlan, PlanMarker, PlanShape } from '@/api/raidplan'

describe('planEditorStore', () => {
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
        shapes: [],
      },
      {
        order: 1,
        notes: 'Phase 2',
        markers: [],
        shapes: [],
      },
    ],
    visibility: 'GUILD',
    shareToken: null,
    createdBy: 1,
    createdAt: '2026-01-15T10:00:00Z',
    updatedAt: '2026-01-15T12:00:00Z',
  })

  describe('initialization', () => {
    it('should initialize with default values', () => {
      const store = usePlanEditorStore()

      expect(store.plan).toBeNull()
      expect(store.currentStep).toBe(0)
      expect(store.currentTool).toBe('select')
      expect(store.isDirty).toBe(false)
    })
  })

  describe('setPlan', () => {
    it('should set the plan', () => {
      const store = usePlanEditorStore()
      const plan = createMockPlan()

      store.setPlan(plan)

      expect(store.plan).toEqual(plan)
    })

    it('should reset current step to 0', () => {
      const store = usePlanEditorStore()
      store.currentStep = 5
      store.setPlan(createMockPlan())

      expect(store.currentStep).toBe(0)
    })

    it('should clear dirty flag', () => {
      const store = usePlanEditorStore()
      store.isDirty = true
      store.setPlan(createMockPlan())

      expect(store.isDirty).toBe(false)
    })

    it('should clear undo/redo history', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      expect(store.canUndo).toBe(false)
      expect(store.canRedo).toBe(false)
    })
  })

  describe('currentStepData', () => {
    it('should return current step data', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      expect(store.currentStepData?.notes).toBe('Phase 1')
    })

    it('should return undefined when no plan', () => {
      const store = usePlanEditorStore()

      expect(store.currentStepData).toBeUndefined()
    })
  })

  describe('goToStep', () => {
    it('should change current step', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      store.goToStep(1)

      expect(store.currentStep).toBe(1)
    })

    it('should clamp to valid range', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      store.goToStep(100)
      expect(store.currentStep).toBe(1)

      store.goToStep(-1)
      expect(store.currentStep).toBe(0)
    })

    it('should clear selection', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.selectedMarkerIndex = 0

      store.goToStep(1)

      expect(store.selectedMarkerIndex).toBe(-1)
    })
  })

  describe('setTool', () => {
    it('should change current tool', () => {
      const store = usePlanEditorStore()

      store.setTool('pan')

      expect(store.currentTool).toBe('pan')
    })

    it('should clear selected marker/shape when changing to pan', () => {
      const store = usePlanEditorStore()
      store.selectedMarker = 'SKULL'
      store.selectedShape = 'CIRCLE'

      store.setTool('pan')

      expect(store.selectedMarker).toBeUndefined()
      expect(store.selectedShape).toBeUndefined()
    })
  })

  describe('selectMarkerType', () => {
    it('should select marker type and set tool to marker', () => {
      const store = usePlanEditorStore()

      store.selectMarkerType('SKULL')

      expect(store.selectedMarker).toBe('SKULL')
      expect(store.currentTool).toBe('marker')
      expect(store.selectedShape).toBeUndefined()
    })
  })

  describe('selectShapeType', () => {
    it('should select shape type and set tool to shape', () => {
      const store = usePlanEditorStore()

      store.selectShapeType('CIRCLE')

      expect(store.selectedShape).toBe('CIRCLE')
      expect(store.currentTool).toBe('shape')
      expect(store.selectedMarker).toBeUndefined()
    })
  })

  describe('addMarker', () => {
    it('should add marker to current step', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      const marker: PlanMarker = { type: 'CROSS', x: 75, y: 25 }
      store.addMarker(marker)

      expect(store.currentStepData?.markers).toHaveLength(2)
      expect(store.currentStepData?.markers[1]).toEqual(marker)
    })

    it('should set dirty flag', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      store.addMarker({ type: 'CROSS', x: 75, y: 25 })

      expect(store.isDirty).toBe(true)
    })

    it('should add to undo history', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      store.addMarker({ type: 'CROSS', x: 75, y: 25 })

      expect(store.canUndo).toBe(true)
    })
  })

  describe('removeMarker', () => {
    it('should remove marker at index', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      store.removeMarker(0)

      expect(store.currentStepData?.markers).toHaveLength(0)
    })

    it('should clear selection if removed marker was selected', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.selectedMarkerIndex = 0

      store.removeMarker(0)

      expect(store.selectedMarkerIndex).toBe(-1)
    })
  })

  describe('addShape', () => {
    it('should add shape to current step', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      const shape: PlanShape = { shapeType: 'CIRCLE', x1: 50, y1: 50, radius: 10, strokeWidth: 2 }
      store.addShape(shape)

      expect(store.currentStepData?.shapes).toHaveLength(1)
      expect(store.currentStepData?.shapes[0]).toEqual(shape)
    })
  })

  describe('removeShape', () => {
    it('should remove shape at index', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      store.addShape({ shapeType: 'CIRCLE', x1: 50, y1: 50, radius: 10, strokeWidth: 2 })

      store.removeShape(0)

      expect(store.currentStepData?.shapes).toHaveLength(0)
    })
  })

  describe('undo/redo', () => {
    it('should undo last action', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())
      const originalMarkerCount = store.currentStepData?.markers.length ?? 0

      store.addMarker({ type: 'CROSS', x: 75, y: 25 })
      store.undo()

      expect(store.currentStepData?.markers).toHaveLength(originalMarkerCount)
    })

    it('should redo undone action', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      store.addMarker({ type: 'CROSS', x: 75, y: 25 })
      store.undo()
      store.redo()

      expect(store.currentStepData?.markers).toHaveLength(2)
    })

    it('should report canUndo correctly', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      expect(store.canUndo).toBe(false)

      store.addMarker({ type: 'CROSS', x: 75, y: 25 })
      expect(store.canUndo).toBe(true)

      store.undo()
      expect(store.canUndo).toBe(false)
    })

    it('should report canRedo correctly', () => {
      const store = usePlanEditorStore()
      store.setPlan(createMockPlan())

      expect(store.canRedo).toBe(false)

      store.addMarker({ type: 'CROSS', x: 75, y: 25 })
      expect(store.canRedo).toBe(false)

      store.undo()
      expect(store.canRedo).toBe(true)

      store.redo()
      expect(store.canRedo).toBe(false)
    })
  })

  describe('zoom and pan', () => {
    it('should update zoom level', () => {
      const store = usePlanEditorStore()

      store.setZoom(1.5)

      expect(store.zoom).toBe(1.5)
    })

    it('should clamp zoom to valid range', () => {
      const store = usePlanEditorStore()

      store.setZoom(10)
      expect(store.zoom).toBe(3)

      store.setZoom(0.1)
      expect(store.zoom).toBe(0.5)
    })

    it('should update pan position', () => {
      const store = usePlanEditorStore()

      store.setPan(100, 50)

      expect(store.panX).toBe(100)
      expect(store.panY).toBe(50)
    })

    it('should reset view', () => {
      const store = usePlanEditorStore()
      store.setZoom(2)
      store.setPan(100, 50)

      store.resetView()

      expect(store.zoom).toBe(1)
      expect(store.panX).toBe(0)
      expect(store.panY).toBe(0)
    })
  })
})
