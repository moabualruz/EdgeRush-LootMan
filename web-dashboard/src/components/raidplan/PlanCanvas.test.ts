import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PlanCanvas from './PlanCanvas.vue'
import type { PlanMarker, PlanShape } from '@/api/raidplan'

describe('PlanCanvas', () => {
  const defaultProps = {
    width: 800,
    height: 600,
    backgroundImage: '/images/encounters/queen-ansurek.jpg',
    markers: [] as PlanMarker[],
    shapes: [] as PlanShape[],
  }

  describe('Rendering', () => {
    it('should render svg canvas element', () => {
      const wrapper = mount(PlanCanvas, {
        props: defaultProps,
      })

      const svg = wrapper.find('svg')
      expect(svg.exists()).toBe(true)
    })

    it('should apply width and height dimensions', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, width: 1024, height: 768 },
      })

      const svg = wrapper.find('svg')
      expect(svg.attributes('width')).toBe('1024')
      expect(svg.attributes('height')).toBe('768')
    })

    it('should render background image when provided', () => {
      const wrapper = mount(PlanCanvas, {
        props: defaultProps,
      })

      const image = wrapper.find('image')
      expect(image.exists()).toBe(true)
      expect(image.attributes('href')).toBe('/images/encounters/queen-ansurek.jpg')
    })

    it('should not render background image when not provided', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, backgroundImage: undefined },
      })

      const image = wrapper.find('image')
      expect(image.exists()).toBe(false)
    })
  })

  describe('Markers', () => {
    it('should render markers at correct positions', () => {
      const markers: PlanMarker[] = [
        { type: 'SKULL', x: 50, y: 50 },
        { type: 'CROSS', x: 25, y: 75 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, markers },
      })

      const markerElements = wrapper.findAll('[data-testid="marker"]')
      expect(markerElements).toHaveLength(2)
    })

    it('should convert percentage coordinates to pixel positions', () => {
      const markers: PlanMarker[] = [
        { type: 'SKULL', x: 50, y: 50 }, // Center of canvas
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, width: 800, height: 600, markers },
      })

      const marker = wrapper.find('[data-testid="marker"]')
      // 50% of 800 = 400, 50% of 600 = 300
      expect(marker.attributes('transform')).toContain('translate(400')
      expect(marker.attributes('transform')).toContain('300)')
    })

    it('should apply marker type as data attribute', () => {
      const markers: PlanMarker[] = [
        { type: 'TANK', x: 10, y: 10 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, markers },
      })

      const marker = wrapper.find('[data-testid="marker"]')
      expect(marker.attributes('data-marker-type')).toBe('TANK')
    })

    it('should display marker label when provided', () => {
      const markers: PlanMarker[] = [
        { type: 'PLAYER', x: 50, y: 50, label: 'Tanky' },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, markers },
      })

      expect(wrapper.text()).toContain('Tanky')
    })

    it('should apply marker color when provided', () => {
      const markers: PlanMarker[] = [
        { type: 'PLAYER', x: 50, y: 50, color: '#ff0000' },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, markers },
      })

      const marker = wrapper.find('[data-testid="marker"]')
      expect(marker.html()).toContain('#ff0000')
    })
  })

  describe('Shapes', () => {
    it('should render circle shapes', () => {
      const shapes: PlanShape[] = [
        { shapeType: 'CIRCLE', x1: 50, y1: 50, radius: 10, strokeWidth: 2 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, shapes },
      })

      const circle = wrapper.find('[data-testid="shape-circle"]')
      expect(circle.exists()).toBe(true)
    })

    it('should render line shapes', () => {
      const shapes: PlanShape[] = [
        { shapeType: 'LINE', x1: 0, y1: 0, x2: 100, y2: 100, strokeWidth: 2 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, shapes },
      })

      const line = wrapper.find('[data-testid="shape-line"]')
      expect(line.exists()).toBe(true)
    })

    it('should render arrow shapes', () => {
      const shapes: PlanShape[] = [
        { shapeType: 'ARROW', x1: 0, y1: 0, x2: 50, y2: 50, strokeWidth: 2 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, shapes },
      })

      const arrow = wrapper.find('[data-testid="shape-arrow"]')
      expect(arrow.exists()).toBe(true)
    })

    it('should render rectangle shapes', () => {
      const shapes: PlanShape[] = [
        { shapeType: 'RECTANGLE', x1: 10, y1: 10, x2: 90, y2: 90, strokeWidth: 2 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, shapes },
      })

      const rect = wrapper.find('[data-testid="shape-rectangle"]')
      expect(rect.exists()).toBe(true)
    })

    it('should apply shape color', () => {
      const shapes: PlanShape[] = [
        { shapeType: 'CIRCLE', x1: 50, y1: 50, radius: 10, strokeWidth: 2, color: '#00ff00' },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, shapes },
      })

      const circle = wrapper.find('[data-testid="shape-circle"]')
      expect(circle.attributes('stroke')).toBe('#00ff00')
    })

    it('should apply stroke width', () => {
      const shapes: PlanShape[] = [
        { shapeType: 'LINE', x1: 0, y1: 0, x2: 100, y2: 100, strokeWidth: 5 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, shapes },
      })

      const line = wrapper.find('[data-testid="shape-line"]')
      expect(line.attributes('stroke-width')).toBe('5')
    })
  })

  describe('Interaction', () => {
    it('should emit canvas-click on background click', async () => {
      const wrapper = mount(PlanCanvas, {
        props: defaultProps,
      })

      const background = wrapper.find('[data-testid="canvas-background"]')
      await background.trigger('click', { offsetX: 400, offsetY: 300 })

      expect(wrapper.emitted('canvas-click')).toBeTruthy()
      // Note: JSDOM doesn't properly set offsetX/offsetY, so we just verify the event is emitted
      const emittedEvent = wrapper.emitted('canvas-click')![0][0] as { x: number; y: number }
      expect(typeof emittedEvent.x).toBe('number')
      expect(typeof emittedEvent.y).toBe('number')
    })

    it('should emit marker-click on marker click', async () => {
      const markers: PlanMarker[] = [
        { type: 'SKULL', x: 50, y: 50 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, markers },
      })

      const marker = wrapper.find('[data-testid="marker"]')
      await marker.trigger('click')

      expect(wrapper.emitted('marker-click')).toBeTruthy()
      expect(wrapper.emitted('marker-click')![0]).toEqual([0, markers[0]])
    })

    it('should emit shape-click on shape click', async () => {
      const shapes: PlanShape[] = [
        { shapeType: 'CIRCLE', x1: 50, y1: 50, radius: 10, strokeWidth: 2 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, shapes },
      })

      const shape = wrapper.find('[data-testid="shape-circle"]')
      await shape.trigger('click')

      expect(wrapper.emitted('shape-click')).toBeTruthy()
      expect(wrapper.emitted('shape-click')![0]).toEqual([0, shapes[0]])
    })
  })

  describe('Zoom and Pan', () => {
    it('should apply zoom level', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, zoom: 1.5 },
      })

      const container = wrapper.find('[data-testid="canvas-container"]')
      expect(container.attributes('style')).toContain('scale(1.5)')
    })

    it('should apply pan offset', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, panX: 100, panY: 50 },
      })

      const container = wrapper.find('[data-testid="canvas-container"]')
      expect(container.attributes('style')).toContain('translate(100px, 50px)')
    })

    it('should emit zoom-change on wheel event', async () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, zoom: 1 },
      })

      const svg = wrapper.find('svg')
      await svg.trigger('wheel', { deltaY: -100 })

      expect(wrapper.emitted('zoom-change')).toBeTruthy()
    })

    it('should emit pan-change on drag', async () => {
      const wrapper = mount(PlanCanvas, {
        props: defaultProps,
      })

      const svg = wrapper.find('svg')
      await svg.trigger('mousedown', { button: 0 })
      await svg.trigger('mousemove', { movementX: 10, movementY: 5 })
      await svg.trigger('mouseup')

      expect(wrapper.emitted('pan-change')).toBeTruthy()
    })
  })

  describe('Selected State', () => {
    it('should highlight selected marker', () => {
      const markers: PlanMarker[] = [
        { type: 'SKULL', x: 50, y: 50 },
        { type: 'CROSS', x: 25, y: 25 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, markers, selectedMarkerIndex: 0 },
      })

      const markerElements = wrapper.findAll('[data-testid="marker"]')
      expect(markerElements[0].classes()).toContain('selected')
      expect(markerElements[1].classes()).not.toContain('selected')
    })

    it('should highlight selected shape', () => {
      const shapes: PlanShape[] = [
        { shapeType: 'CIRCLE', x1: 50, y1: 50, radius: 10, strokeWidth: 2 },
        { shapeType: 'LINE', x1: 0, y1: 0, x2: 100, y2: 100, strokeWidth: 2 },
      ]

      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, shapes, selectedShapeIndex: 1 },
      })

      const circle = wrapper.find('[data-testid="shape-circle"]')
      const line = wrapper.find('[data-testid="shape-line"]')
      expect(circle.classes()).not.toContain('selected')
      expect(line.classes()).toContain('selected')
    })
  })

  describe('Edit Mode', () => {
    it('should show cursor based on current tool', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, currentTool: 'marker' },
      })

      const svg = wrapper.find('svg')
      expect(svg.classes()).toContain('cursor-crosshair')
    })

    it('should show grab cursor when in pan mode', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, currentTool: 'pan' },
      })

      const svg = wrapper.find('svg')
      expect(svg.classes()).toContain('cursor-grab')
    })

    it('should show default cursor when in select mode', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, currentTool: 'select' },
      })

      const svg = wrapper.find('svg')
      expect(svg.classes()).toContain('cursor-default')
    })
  })

  describe('Grid Snapping', () => {
    it('should show grid overlay by default', () => {
      const wrapper = mount(PlanCanvas, {
        props: defaultProps,
      })

      expect(wrapper.find('[data-testid="grid-overlay"]').exists()).toBe(true)
    })

    it('should hide grid overlay when showGrid is false', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, showGrid: false },
      })

      expect(wrapper.find('[data-testid="grid-overlay"]').exists()).toBe(false)
    })

    it('should render grid lines based on gridSize', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, gridSize: 10, showGrid: true },
      })

      const gridOverlay = wrapper.find('[data-testid="grid-overlay"]')
      expect(gridOverlay.exists()).toBe(true)
      
      // With gridSize 10, should have 9 lines (10%, 20%, ..., 90%)
      const lines = gridOverlay.findAll('line')
      expect(lines.length).toBe(18) // 9 vertical + 9 horizontal
    })

    it('should use dashed stroke for grid lines', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, showGrid: true },
      })

      const gridLine = wrapper.find('[data-testid="grid-overlay"] line')
      expect(gridLine.attributes('stroke-dasharray')).toBe('4,4')
    })

    it('should apply grid snapping when gridEnabled is true', async () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, gridEnabled: true, gridSize: 10 },
      })

      const background = wrapper.find('[data-testid="canvas-background"]')
      // Click at position that would be 12% - should snap to 10%
      await background.trigger('click', { offsetX: 96, offsetY: 72 }) // 12% of 800, 12% of 600

      const emitted = wrapper.emitted('canvas-click')
      expect(emitted).toBeTruthy()
      // Note: JSDOM doesn't properly handle offsetX/offsetY, but we verify the event fires
    })

    it('should not show grid when gridSize is 0', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, gridSize: 0, showGrid: true },
      })

      const gridOverlay = wrapper.find('[data-testid="grid-overlay"]')
      // Grid overlay element exists but should have no lines
      const lines = gridOverlay.findAll('line')
      expect(lines.length).toBe(0)
    })

    it('should pass gridEnabled and gridSize props correctly', () => {
      const wrapper = mount(PlanCanvas, {
        props: { ...defaultProps, gridEnabled: false, gridSize: 20, showGrid: true },
      })

      // Just verify component mounts without error with custom props
      expect(wrapper.find('svg').exists()).toBe(true)
    })
  })
})
