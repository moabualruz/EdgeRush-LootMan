import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SimulationResults from './SimulationResults.vue'
import type { SimulationResultDto } from '@/api/simulation'

describe('SimulationResults', () => {
  const mockResults: SimulationResultDto[] = [
    {
      itemId: 1001,
      itemName: 'Void-Touched Blade',
      slot: 'Main Hand',
      dpsGain: 15000,
      percentGain: 12.5,
      isUpgrade: true,
      normalizedValue: 1.0,
      simulatedAt: '2026-01-14T10:00:00Z',
    },
    {
      itemId: 1002,
      itemName: 'Crown of Endless Fury',
      slot: 'Head',
      dpsGain: 8500,
      percentGain: 7.2,
      isUpgrade: true,
      normalizedValue: 0.72,
      simulatedAt: '2026-01-14T10:00:00Z',
    },
    {
      itemId: 1003,
      itemName: 'Ring of Dark Whispers',
      slot: 'Finger',
      dpsGain: 3500,
      percentGain: 3.1,
      isUpgrade: true,
      normalizedValue: 0.31,
      simulatedAt: '2026-01-14T10:00:00Z',
    },
    {
      itemId: 1004,
      itemName: 'Boots of the Void',
      slot: 'Feet',
      dpsGain: -500,
      percentGain: -0.5,
      isUpgrade: false,
      normalizedValue: 0,
      simulatedAt: '2026-01-14T10:00:00Z',
    },
  ]

  const mountComponent = (props = {}) => {
    return mount(SimulationResults, {
      props: {
        results: mockResults,
        characterName: 'TestRaider',
        characterRealm: 'Illidan',
        ...props,
      },
    })
  }

  it('should render character name and realm', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('TestRaider')
    expect(wrapper.text()).toContain('Illidan')
  })

  it('should display all simulation results', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Void-Touched Blade')
    expect(wrapper.text()).toContain('Crown of Endless Fury')
    expect(wrapper.text()).toContain('Ring of Dark Whispers')
    expect(wrapper.text()).toContain('Boots of the Void')
  })

  it('should show slot information', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Main Hand')
    expect(wrapper.text()).toContain('Head')
    expect(wrapper.text()).toContain('Finger')
    expect(wrapper.text()).toContain('Feet')
  })

  it('should display DPS gain with formatting', () => {
    const wrapper = mountComponent()

    // DPS should be displayed (locale may format differently with comma or period as separator)
    expect(wrapper.text()).toMatch(/\+15[,.]?000/)
    expect(wrapper.text()).toMatch(/\+8[,.]?500/)
    expect(wrapper.text()).toMatch(/\+3[,.]?500/)
  })

  it('should display percent gain', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('12.5%')
    expect(wrapper.text()).toContain('7.2%')
    expect(wrapper.text()).toContain('3.1%')
  })

  it('should indicate upgrades vs downgrades', () => {
    const wrapper = mountComponent()

    // Positive gains should have + prefix (locale may format differently)
    expect(wrapper.text()).toMatch(/\+15[,.]?000/)
    expect(wrapper.text()).toContain('+12.5%')

    // Negative should show minus
    expect(wrapper.text()).toMatch(/-500/)
    expect(wrapper.text()).toContain('-0.5%')
  })

  it('should apply upgrade color classes for high values', () => {
    const wrapper = mountComponent()

    // High upgrade (>10%) should be purple
    expect(wrapper.html()).toContain('text-purple-400')
    // Major upgrade (5-10%) should be blue
    expect(wrapper.html()).toContain('text-blue-400')
    // Minor upgrade (2-5%) should be green
    expect(wrapper.html()).toContain('text-green-400')
  })

  it('should apply downgrade color class', () => {
    const wrapper = mountComponent()

    // Downgrades should be red
    expect(wrapper.html()).toContain('text-red-400')
  })

  it('should sort results by percent gain by default (descending)', () => {
    const wrapper = mountComponent()
    const rows = wrapper.findAll('tbody tr')

    // First row should be highest gain
    expect(rows[0].text()).toContain('Void-Touched Blade')
    expect(rows[0].text()).toContain('12.5%')

    // Last row should be lowest (or negative)
    expect(rows[rows.length - 1].text()).toContain('Boots of the Void')
  })

  it('should show empty state when no results', () => {
    const wrapper = mountComponent({ results: [] })

    expect(wrapper.text()).toContain('No simulation results')
  })

  it('should show upgrade count summary', () => {
    const wrapper = mountComponent()

    // 3 upgrades out of 4 items
    expect(wrapper.text()).toContain('3')
    expect(wrapper.text()).toContain('upgrade')
  })

  it('should show normalized value bar', () => {
    const wrapper = mountComponent()

    // Should have progress bars for normalized values
    const progressBars = wrapper.findAll('.progress-bar')
    expect(progressBars.length).toBeGreaterThan(0)
  })

  it('should display simulation timestamp', () => {
    const wrapper = mountComponent()

    // Should show when simulation was run
    expect(wrapper.text()).toMatch(/simulated|Jan|2026/i)
  })

  it('should have sortable headers', () => {
    const wrapper = mountComponent()

    const headers = wrapper.findAll('th')
    expect(headers.length).toBeGreaterThan(0)

    // Headers should indicate they're sortable
    const sortableHeader = headers.find((h) => h.classes().some((c) => c.includes('cursor')))
    expect(sortableHeader).toBeDefined()
  })

  it('should emit item-click event when clicking a result row', async () => {
    const wrapper = mountComponent()

    const firstRow = wrapper.find('tbody tr')
    await firstRow.trigger('click')

    expect(wrapper.emitted('item-click')).toBeTruthy()
    expect(wrapper.emitted('item-click')?.[0][0]).toMatchObject({
      itemId: 1001,
      itemName: 'Void-Touched Blade',
    })
  })

  it('should show best in slot indicator for highest normalized value', () => {
    const wrapper = mountComponent()

    // Item with normalizedValue of 1.0 should be marked as BiS
    expect(wrapper.text()).toContain('BiS')
  })

  it('should filter upgrades only when showUpgradesOnly is true', async () => {
    const wrapper = mountComponent({ showUpgradesOnly: true })

    // Should not show downgrades
    expect(wrapper.text()).not.toContain('Boots of the Void')
    expect(wrapper.text()).toContain('Void-Touched Blade')
  })

  it('should show loading state when loading prop is true', () => {
    const wrapper = mountComponent({ loading: true })

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })
})
