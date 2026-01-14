import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ScoreBreakdown from './ScoreBreakdown.vue'
import type { RmsBreakdown, IpiBreakdown } from '@/types'

describe('ScoreBreakdown', () => {
  const defaultProps = {
    rms: {
      value: 0.9,
      acs: 0.95,
      mas: 0.88,
      eps: 0.87,
    } as RmsBreakdown,
    ipi: {
      value: 0.8,
      uv: 10.5,
      tierBonus: 0.1,
      roleMultiplier: 1.0,
    } as IpiBreakdown,
    rdf: 0.75,
  }

  it('should render RMS section', () => {
    const wrapper = mount(ScoreBreakdown, { props: defaultProps })
    expect(wrapper.text()).toContain('RMS')
    expect(wrapper.text()).toContain('90') // 0.9 * 100
  })

  it('should render RMS components', () => {
    const wrapper = mount(ScoreBreakdown, { props: defaultProps })
    expect(wrapper.text()).toContain('ACS')
    expect(wrapper.text()).toContain('MAS')
    expect(wrapper.text()).toContain('EPS')
  })

  it('should render IPI section', () => {
    const wrapper = mount(ScoreBreakdown, { props: defaultProps })
    expect(wrapper.text()).toContain('IPI')
    expect(wrapper.text()).toContain('80') // 0.8 * 100
  })

  it('should render IPI components', () => {
    const wrapper = mount(ScoreBreakdown, { props: defaultProps })
    expect(wrapper.text()).toContain('Upgrade Value')
    expect(wrapper.text()).toContain('1050%') // UV is displayed as percentage (10.5 * 100)
    expect(wrapper.text()).toContain('Tier Bonus')
    expect(wrapper.text()).toContain('Role Multiplier')
  })

  it('should render RDF section', () => {
    const wrapper = mount(ScoreBreakdown, { props: defaultProps })
    expect(wrapper.text()).toContain('RDF')
    expect(wrapper.text()).toContain('75') // 0.75 * 100
  })

  it('should display progress bars', () => {
    const wrapper = mount(ScoreBreakdown, { props: defaultProps })
    // Check that progress bar elements exist
    const progressBars = wrapper.findAll('.bg-gray-700')
    expect(progressBars.length).toBeGreaterThan(0)
  })
})
