import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FlpsVisualization from './FlpsVisualization.vue'

// Mock chart components
vi.mock('@/components/charts', () => ({
  DonutChart: {
    name: 'DonutChart',
    template: '<div class="mock-donut-chart"></div>',
    props: ['data', 'size', 'thickness', 'showCenterValue', 'showLegend'],
  },
  ProgressBar: {
    name: 'ProgressBar',
    template: '<div class="mock-progress-bar"></div>',
    props: ['value', 'max', 'label', 'color', 'height'],
  },
}))

describe('FlpsVisualization', () => {
  const defaultProps = {
    flps: 0.75,
    rms: {
      value: 0.85,
      acs: 0.9,
      mas: 0.8,
      eps: 0.85,
    },
    ipi: {
      value: 0.88,
      uv: 0.7,
      tierBonus: 0.1,
      roleMultiplier: 1.0,
    },
    rdf: 1.0,
  }

  it('should render FLPS formula overview', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.text()).toContain('FLPS Formula')
    expect(wrapper.text()).toContain('RMS')
    expect(wrapper.text()).toContain('IPI')
    expect(wrapper.text()).toContain('RDF')
  })

  it('should display correct score values', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.text()).toContain('0.850') // RMS
    expect(wrapper.text()).toContain('0.880') // IPI
    expect(wrapper.text()).toContain('1.000') // RDF
    expect(wrapper.text()).toContain('0.750') // FLPS
  })

  it('should show RMS breakdown section', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.text()).toContain('RMS (Raider Merit)')
    // ProgressBar labels are rendered by mock components, just verify section exists
  })

  it('should show IPI breakdown section', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.text()).toContain('IPI (Item Priority)')
    // ProgressBar label may be mocked, so just check for tier and role text
    expect(wrapper.text()).toContain('Tier Set Bonus')
    expect(wrapper.text()).toContain('Role Multiplier')
  })

  it('should show RDF section', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.text()).toContain('RDF (Recency Decay)')
  })

  it('should show no penalty status when RDF is 1', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.text()).toContain('No Penalty Active')
    expect(wrapper.text()).toContain('You have no recent loot affecting your priority score')
  })

  it('should show penalty status when RDF is less than 1', () => {
    const wrapper = mount(FlpsVisualization, {
      props: { ...defaultProps, rdf: 0.8 },
    })

    expect(wrapper.text()).toContain('Recent Loot Penalty')
    expect(wrapper.text()).toContain('Your score is reduced due to recent loot awards')
  })

  it('should apply green color to RDF when no penalty', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    // Check for green color class on RDF value
    expect(wrapper.find('.text-green-400').exists()).toBe(true)
  })

  it('should apply yellow color to RDF when penalty active', () => {
    const wrapper = mount(FlpsVisualization, {
      props: { ...defaultProps, rdf: 0.75 },
    })

    // Check for yellow color class on RDF value
    expect(wrapper.find('.text-yellow-400').exists()).toBe(true)
  })

  it('should render DonutChart for RMS breakdown', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.findComponent({ name: 'DonutChart' }).exists()).toBe(true)
  })

  it('should render ProgressBar components', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    const progressBars = wrapper.findAllComponents({ name: 'ProgressBar' })
    expect(progressBars.length).toBeGreaterThan(0)
  })

  it('should display role multiplier correctly', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.text()).toContain('1.000x')
  })

  it('should show tier bonus percentage', () => {
    const wrapper = mount(FlpsVisualization, { props: defaultProps })

    expect(wrapper.text()).toContain('+10%')
  })
})
