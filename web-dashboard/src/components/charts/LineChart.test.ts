import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LineChart from './LineChart.vue'

describe('LineChart', () => {
  const mockData = [
    { x: 'Jan', y: 100 },
    { x: 'Feb', y: 150 },
    { x: 'Mar', y: 120 },
    { x: 'Apr', y: 180 },
  ]

  it('should render the line path', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData },
    })

    const line = wrapper.find('.line')
    expect(line.exists()).toBe(true)
    expect(line.attributes('d')).toContain('M')
    expect(line.attributes('d')).toContain('L')
  })

  it('should render area fill when showArea is true', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, showArea: true },
    })

    const area = wrapper.find('.area')
    expect(area.exists()).toBe(true)
  })

  it('should hide area when showArea is false', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, showArea: false },
    })

    const area = wrapper.find('.area')
    expect(area.exists()).toBe(false)
  })

  it('should render data points when showPoints is true', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, showPoints: true },
    })

    const points = wrapper.findAll('.point')
    expect(points).toHaveLength(4)
  })

  it('should hide data points when showPoints is false', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, showPoints: false },
    })

    const points = wrapper.findAll('.point')
    expect(points).toHaveLength(0)
  })

  it('should render grid lines when showGrid is true', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, showGrid: true },
    })

    const gridLines = wrapper.findAll('.grid-lines line')
    expect(gridLines.length).toBeGreaterThan(0)
  })

  it('should hide grid lines when showGrid is false', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, showGrid: false },
    })

    const gridLines = wrapper.find('.grid-lines')
    expect(gridLines.exists()).toBe(false)
  })

  it('should apply custom line color', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, lineColor: '#ff0000' },
    })

    const line = wrapper.find('.line')
    expect(line.attributes('stroke')).toBe('#ff0000')
  })

  it('should apply custom point color', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, pointColor: '#00ff00', showPoints: true },
    })

    const point = wrapper.find('.point')
    expect(point.attributes('fill')).toBe('#00ff00')
  })

  it('should apply custom height', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData, height: 300 },
    })

    const svg = wrapper.find('svg')
    expect(svg.attributes('style')).toContain('height: 300px')
  })

  it('should handle single data point', () => {
    const wrapper = mount(LineChart, {
      props: { data: [{ x: 'Jan', y: 100 }] },
    })

    // Should render point but no line (need at least 2 points)
    const points = wrapper.findAll('.point')
    expect(points).toHaveLength(1)
  })

  it('should handle empty data', () => {
    const wrapper = mount(LineChart, {
      props: { data: [] },
    })

    expect(wrapper.find('.line').exists()).toBe(false)
    expect(wrapper.findAll('.point')).toHaveLength(0)
  })

  it('should use custom minY and maxY', () => {
    const wrapper = mount(LineChart, {
      props: {
        data: [{ x: 'A', y: 50 }, { x: 'B', y: 75 }],
        minY: 0,
        maxY: 100,
      },
    })

    // Just verify it renders without error
    const line = wrapper.find('.line')
    expect(line.exists()).toBe(true)
  })

  it('should render x-axis labels', () => {
    const wrapper = mount(LineChart, {
      props: { data: mockData },
    })

    const labels = wrapper.findAll('.axis-label')
    expect(labels.length).toBe(4)
  })
})
