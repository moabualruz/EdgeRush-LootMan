import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BarChart from './BarChart.vue'

describe('BarChart', () => {
  const mockData = [
    { label: 'Jan', value: 100, color: '#ff0000' },
    { label: 'Feb', value: 200 },
    { label: 'Mar', value: 150 },
  ]

  it('should render bars for each data point', () => {
    const wrapper = mount(BarChart, {
      props: { data: mockData },
    })

    const bars = wrapper.findAll('.bar')
    expect(bars).toHaveLength(3)
  })

  it('should render with custom bar color', () => {
    const wrapper = mount(BarChart, {
      props: {
        data: [{ label: 'Test', value: 100 }],
        barColor: '#00ff00',
      },
    })

    const bar = wrapper.find('.bar')
    expect(bar.attributes('fill')).toBe('#00ff00')
  })

  it('should use individual bar colors when provided', () => {
    const wrapper = mount(BarChart, {
      props: { data: mockData },
    })

    const bars = wrapper.findAll('.bar')
    expect(bars[0].attributes('fill')).toBe('#ff0000')
  })

  it('should render labels when showLabels is true', () => {
    const wrapper = mount(BarChart, {
      props: { data: mockData, showLabels: true },
    })

    expect(wrapper.text()).toContain('Jan')
    expect(wrapper.text()).toContain('Feb')
    expect(wrapper.text()).toContain('Mar')
  })

  it('should hide labels when showLabels is false', () => {
    const wrapper = mount(BarChart, {
      props: { data: mockData, showLabels: false },
    })

    // Labels are only shown when showLabels is true and not horizontal
    expect(wrapper.find('.mt-2').exists()).toBe(false)
  })

  it('should render value labels when showValues is true', () => {
    const wrapper = mount(BarChart, {
      props: { data: mockData, showValues: true },
    })

    const valueLabels = wrapper.findAll('.value-label')
    expect(valueLabels.length).toBeGreaterThan(0)
  })

  it('should hide value labels when showValues is false', () => {
    const wrapper = mount(BarChart, {
      props: { data: mockData, showValues: false },
    })

    const valueLabels = wrapper.findAll('.value-label')
    expect(valueLabels.length).toBe(0)
  })

  it('should render horizontal bars when horizontal is true', () => {
    const wrapper = mount(BarChart, {
      props: { data: mockData, horizontal: true },
    })

    // Horizontal mode should render background bars
    const backgroundBars = wrapper.findAll('rect[fill="rgb(55 65 81 / 0.3)"]')
    expect(backgroundBars.length).toBe(3)
  })

  it('should apply custom height', () => {
    const wrapper = mount(BarChart, {
      props: { data: mockData, height: 300 },
    })

    const svg = wrapper.find('svg')
    expect(svg.attributes('style')).toContain('height: 300px')
  })

  it('should handle empty data', () => {
    const wrapper = mount(BarChart, {
      props: { data: [] },
    })

    expect(wrapper.findAll('.bar')).toHaveLength(0)
  })

  it('should use custom maxValue', () => {
    const wrapper = mount(BarChart, {
      props: {
        data: [{ label: 'Test', value: 50 }],
        maxValue: 100,
      },
    })

    const bar = wrapper.find('.bar')
    // Height should be 50% of chart height
    expect(bar.attributes('height')).toBe('50')
  })

  it('should format large values correctly', () => {
    const wrapper = mount(BarChart, {
      props: {
        data: [{ label: 'Big', value: 1500000 }],
        showValues: true,
      },
    })

    expect(wrapper.text()).toContain('1.5M')
  })
})
