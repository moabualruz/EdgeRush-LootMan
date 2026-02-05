import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DecayProjectionChart from './DecayProjectionChart.vue'

describe('DecayProjectionChart', () => {
  const defaultProps = {
    currentFlps: 0.75,
    rdfDecayRate: 0.85,
    projectionWeeks: 4,
  }

  it('should render the chart container', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    expect(wrapper.find('.decay-projection-chart').exists()).toBe(true)
  })

  it('should render an SVG element', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    expect(wrapper.find('svg').exists()).toBe(true)
  })

  it('should display current week as "Now"', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    expect(wrapper.text()).toContain('Now')
  })

  it('should display week labels W1-W4', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    expect(wrapper.text()).toContain('W1')
    expect(wrapper.text()).toContain('W2')
    expect(wrapper.text()).toContain('W3')
    expect(wrapper.text()).toContain('W4')
  })

  it('should render the decay projection line path', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    const path = wrapper.find('.decay-line')
    expect(path.exists()).toBe(true)
    expect(path.attributes('stroke-dasharray')).toBe('2,1')
  })

  it('should render current value point', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    const currentPoint = wrapper.find('.current-point')
    expect(currentPoint.exists()).toBe(true)
  })

  it('should render projected value points', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    const projectedPoints = wrapper.findAll('.projected-point')
    expect(projectedPoints.length).toBe(4) // 4 weeks of projection
  })

  it('should show legend with Current and Projected labels', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    expect(wrapper.text()).toContain('Current')
    expect(wrapper.text()).toContain('Projected Decay')
  })

  it('should calculate correct decay values', () => {
    const wrapper = mount(DecayProjectionChart, { props: defaultProps })
    
    // Week 1: 0.75 * 0.85 = 0.6375
    // Week 2: 0.6375 * 0.85 = 0.5419
    // Week 3: 0.5419 * 0.85 = 0.4606
    // Week 4: 0.4606 * 0.85 = 0.3915
    
    // Check the end value annotation contains approximately 0.392
    const endValue = wrapper.find('.end-value')
    expect(endValue.exists()).toBe(true)
    expect(endValue.text()).toMatch(/0\.39\d/)
  })

  it('should respect custom projectionWeeks prop', () => {
    const wrapper = mount(DecayProjectionChart, {
      props: { ...defaultProps, projectionWeeks: 2 },
    })
    
    const projectedPoints = wrapper.findAll('.projected-point')
    expect(projectedPoints.length).toBe(2)
    expect(wrapper.text()).toContain('W1')
    expect(wrapper.text()).toContain('W2')
    expect(wrapper.text()).not.toContain('W3')
  })

  it('should use custom colors when provided', () => {
    const wrapper = mount(DecayProjectionChart, {
      props: {
        ...defaultProps,
        lineColor: '#00ff00',
        projectedColor: '#ff0000',
      },
    })
    
    const decayLine = wrapper.find('.decay-line')
    expect(decayLine.attributes('stroke')).toBe('#ff0000')
  })

  it('should apply custom height', () => {
    const wrapper = mount(DecayProjectionChart, {
      props: { ...defaultProps, height: 200 },
    })
    
    const svg = wrapper.find('svg')
    expect(svg.attributes('style')).toContain('height: 200px')
  })

  it('should handle zero FLPS value', () => {
    const wrapper = mount(DecayProjectionChart, {
      props: { ...defaultProps, currentFlps: 0 },
    })
    
    // Should still render without errors
    expect(wrapper.find('svg').exists()).toBe(true)
  })

  it('should handle very small FLPS value', () => {
    const wrapper = mount(DecayProjectionChart, {
      props: { ...defaultProps, currentFlps: 0.001 },
    })
    
    expect(wrapper.find('svg').exists()).toBe(true)
    expect(wrapper.find('.decay-line').exists()).toBe(true)
  })
})
