import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MarkerPalette from './MarkerPalette.vue'
import type { MarkerType, ShapeType } from '@/api/raidplan'

describe('MarkerPalette', () => {
  describe('Rendering', () => {
    it('should render marker palette container', () => {
      const wrapper = mount(MarkerPalette)
      expect(wrapper.find('[data-testid="marker-palette"]').exists()).toBe(true)
    })

    it('should display raid marker section', () => {
      const wrapper = mount(MarkerPalette)
      expect(wrapper.text()).toContain('Raid Markers')
    })

    it('should display role marker section', () => {
      const wrapper = mount(MarkerPalette)
      expect(wrapper.text()).toContain('Role Markers')
    })

    it('should display shapes section', () => {
      const wrapper = mount(MarkerPalette)
      expect(wrapper.text()).toContain('Shapes')
    })
  })

  describe('Raid Markers', () => {
    it('should render all raid marker icons', () => {
      const wrapper = mount(MarkerPalette)
      const raidMarkers: MarkerType[] = ['SKULL', 'CROSS', 'SQUARE', 'MOON', 'TRIANGLE', 'DIAMOND', 'CIRCLE', 'STAR']

      raidMarkers.forEach((marker) => {
        expect(wrapper.find(`[data-testid="marker-${marker}"]`).exists()).toBe(true)
      })
    })

    it('should emit marker-select when raid marker clicked', async () => {
      const wrapper = mount(MarkerPalette)

      const skullMarker = wrapper.find('[data-testid="marker-SKULL"]')
      await skullMarker.trigger('click')

      expect(wrapper.emitted('marker-select')).toBeTruthy()
      expect(wrapper.emitted('marker-select')![0]).toEqual(['SKULL'])
    })
  })

  describe('Role Markers', () => {
    it('should render all role marker icons', () => {
      const wrapper = mount(MarkerPalette)
      const roleMarkers: MarkerType[] = ['TANK', 'HEALER', 'DPS', 'PLAYER']

      roleMarkers.forEach((marker) => {
        expect(wrapper.find(`[data-testid="marker-${marker}"]`).exists()).toBe(true)
      })
    })

    it('should emit marker-select when role marker clicked', async () => {
      const wrapper = mount(MarkerPalette)

      const tankMarker = wrapper.find('[data-testid="marker-TANK"]')
      await tankMarker.trigger('click')

      expect(wrapper.emitted('marker-select')).toBeTruthy()
      expect(wrapper.emitted('marker-select')![0]).toEqual(['TANK'])
    })
  })

  describe('Shape Tools', () => {
    it('should render all shape tool icons', () => {
      const wrapper = mount(MarkerPalette)
      const shapes: ShapeType[] = ['CIRCLE', 'LINE', 'ARROW', 'RECTANGLE']

      shapes.forEach((shape) => {
        expect(wrapper.find(`[data-testid="shape-${shape}"]`).exists()).toBe(true)
      })
    })

    it('should emit shape-select when shape tool clicked', async () => {
      const wrapper = mount(MarkerPalette)

      const circleTool = wrapper.find('[data-testid="shape-CIRCLE"]')
      await circleTool.trigger('click')

      expect(wrapper.emitted('shape-select')).toBeTruthy()
      expect(wrapper.emitted('shape-select')![0]).toEqual(['CIRCLE'])
    })
  })

  describe('Selection State', () => {
    it('should highlight currently selected marker', () => {
      const wrapper = mount(MarkerPalette, {
        props: { selectedMarker: 'SKULL' as MarkerType },
      })

      const skullMarker = wrapper.find('[data-testid="marker-SKULL"]')
      expect(skullMarker.classes()).toContain('selected')
    })

    it('should highlight currently selected shape', () => {
      const wrapper = mount(MarkerPalette, {
        props: { selectedShape: 'CIRCLE' as ShapeType },
      })

      const circleShape = wrapper.find('[data-testid="shape-CIRCLE"]')
      expect(circleShape.classes()).toContain('selected')
    })

    it('should not highlight unselected items', () => {
      const wrapper = mount(MarkerPalette, {
        props: { selectedMarker: 'SKULL' as MarkerType },
      })

      const crossMarker = wrapper.find('[data-testid="marker-CROSS"]')
      expect(crossMarker.classes()).not.toContain('selected')
    })
  })

  describe('Color Picker', () => {
    it('should show color picker', () => {
      const wrapper = mount(MarkerPalette)
      expect(wrapper.find('[data-testid="color-picker"]').exists()).toBe(true)
    })

    it('should display preset colors', () => {
      const wrapper = mount(MarkerPalette)
      const colorSwatches = wrapper.findAll('[data-testid="color-swatch"]')
      expect(colorSwatches.length).toBeGreaterThan(0)
    })

    it('should emit color-select when color clicked', async () => {
      const wrapper = mount(MarkerPalette)

      const colorSwatches = wrapper.findAll('[data-testid="color-swatch"]')
      await colorSwatches[0].trigger('click')

      expect(wrapper.emitted('color-select')).toBeTruthy()
    })

    it('should highlight selected color', () => {
      const wrapper = mount(MarkerPalette, {
        props: { selectedColor: '#ff0000' },
      })

      const redSwatch = wrapper.find('[data-color="#ff0000"]')
      if (redSwatch.exists()) {
        expect(redSwatch.classes()).toContain('selected')
      }
    })
  })

  describe('Tool Buttons', () => {
    it('should show select tool button', () => {
      const wrapper = mount(MarkerPalette)
      expect(wrapper.find('[data-testid="tool-select"]').exists()).toBe(true)
    })

    it('should show pan tool button', () => {
      const wrapper = mount(MarkerPalette)
      expect(wrapper.find('[data-testid="tool-pan"]').exists()).toBe(true)
    })

    it('should emit tool-select when tool button clicked', async () => {
      const wrapper = mount(MarkerPalette)

      const panTool = wrapper.find('[data-testid="tool-pan"]')
      await panTool.trigger('click')

      expect(wrapper.emitted('tool-select')).toBeTruthy()
      expect(wrapper.emitted('tool-select')![0]).toEqual(['pan'])
    })

    it('should highlight selected tool', () => {
      const wrapper = mount(MarkerPalette, {
        props: { selectedTool: 'pan' },
      })

      const panTool = wrapper.find('[data-testid="tool-pan"]')
      expect(panTool.classes()).toContain('selected')
    })
  })

  describe('Delete Button', () => {
    it('should show delete button', () => {
      const wrapper = mount(MarkerPalette)
      expect(wrapper.find('[data-testid="delete-button"]').exists()).toBe(true)
    })

    it('should emit delete on delete button click', async () => {
      const wrapper = mount(MarkerPalette)

      const deleteBtn = wrapper.find('[data-testid="delete-button"]')
      await deleteBtn.trigger('click')

      expect(wrapper.emitted('delete')).toBeTruthy()
    })

    it('should disable delete button when nothing selected', () => {
      const wrapper = mount(MarkerPalette, {
        props: { canDelete: false },
      })

      const deleteBtn = wrapper.find('[data-testid="delete-button"]')
      expect(deleteBtn.attributes('disabled')).toBeDefined()
    })
  })
})
