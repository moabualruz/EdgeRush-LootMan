import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DonutChart from './DonutChart.vue'

describe('DonutChart', () => {
  const mockData = [
    { label: 'DPS', value: 60, color: '#ef4444' },
    { label: 'Tank', value: 20, color: '#3b82f6' },
    { label: 'Healer', value: 20, color: '#22c55e' },
  ]

  it('should render segments for each data point', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData },
    })

    const segments = wrapper.findAll('.segment')
    expect(segments).toHaveLength(3)
  })

  it('should render background circle', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData },
    })

    const circles = wrapper.findAll('circle')
    // Background + 3 segments
    expect(circles).toHaveLength(4)
  })

  it('should apply segment colors', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData },
    })

    const segments = wrapper.findAll('.segment')
    expect(segments[0].attributes('stroke')).toBe('#ef4444')
    expect(segments[1].attributes('stroke')).toBe('#3b82f6')
    expect(segments[2].attributes('stroke')).toBe('#22c55e')
  })

  it('should show center value when showCenterValue is true', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData, showCenterValue: true },
    })

    expect(wrapper.text()).toContain('100') // Total value
  })

  it('should hide center value when showCenterValue is false', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData, showCenterValue: false },
    })

    // Should not have the center text element
    const centerDiv = wrapper.find('.absolute.inset-0')
    expect(centerDiv.exists()).toBe(false)
  })

  it('should display custom center label', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData, centerLabel: 'Raiders' },
    })

    expect(wrapper.text()).toContain('Raiders')
  })

  it('should render legend when showLegend is true', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData, showLegend: true },
    })

    expect(wrapper.text()).toContain('DPS')
    expect(wrapper.text()).toContain('Tank')
    expect(wrapper.text()).toContain('Healer')
  })

  it('should hide legend when showLegend is false', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData, showLegend: false },
    })

    // Legend items have color indicators
    const legendItems = wrapper.findAll('.w-3.h-3.rounded-full')
    expect(legendItems).toHaveLength(0)
  })

  it('should display percentages in legend', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData, showLegend: true },
    })

    expect(wrapper.text()).toContain('60.0%')
    expect(wrapper.text()).toContain('20.0%')
  })

  it('should apply custom size', () => {
    const wrapper = mount(DonutChart, {
      props: { data: mockData, size: 200 },
    })

    const container = wrapper.find('.relative')
    expect(container.attributes('style')).toContain('width: 200px')
    expect(container.attributes('style')).toContain('height: 200px')
  })

  it('should handle empty data', () => {
    const wrapper = mount(DonutChart, {
      props: { data: [] },
    })

    const segments = wrapper.findAll('.segment')
    expect(segments).toHaveLength(0)
  })

  it('should handle zero total value', () => {
    const wrapper = mount(DonutChart, {
      props: {
        data: [
          { label: 'A', value: 0, color: '#ff0000' },
          { label: 'B', value: 0, color: '#00ff00' },
        ],
        showCenterValue: true,
      },
    })

    expect(wrapper.text()).toContain('0')
  })

  it('should format large values correctly', () => {
    const wrapper = mount(DonutChart, {
      props: {
        data: [{ label: 'Big', value: 1500000, color: '#ff0000' }],
        showCenterValue: true,
      },
    })

    expect(wrapper.text()).toContain('1.5M')
  })
})
