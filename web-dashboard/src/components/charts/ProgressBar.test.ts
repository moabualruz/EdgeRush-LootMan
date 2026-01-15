import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ProgressBar from './ProgressBar.vue'

describe('ProgressBar', () => {
  it('should render with default props', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50 },
    })

    expect(wrapper.find('.progress-track').exists()).toBe(true)
    expect(wrapper.find('.progress-fill').exists()).toBe(true)
  })

  it('should calculate correct percentage', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 25, max: 100 },
    })

    const fill = wrapper.find('.progress-fill')
    expect(fill.attributes('style')).toContain('width: 25%')
  })

  it('should handle custom max value', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, max: 200 },
    })

    const fill = wrapper.find('.progress-fill')
    expect(fill.attributes('style')).toContain('width: 25%')
  })

  it('should cap percentage at 100%', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 150, max: 100 },
    })

    const fill = wrapper.find('.progress-fill')
    expect(fill.attributes('style')).toContain('width: 100%')
  })

  it('should display label when provided', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, label: 'Progress' },
    })

    expect(wrapper.text()).toContain('Progress')
  })

  it('should show value when showValue is true', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, max: 100, showValue: true },
    })

    expect(wrapper.text()).toContain('50')
    expect(wrapper.text()).toContain('100')
  })

  it('should hide value when showValue is false', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, max: 100, showValue: false, showPercentage: false },
    })

    expect(wrapper.text()).not.toContain('50')
  })

  it('should show percentage when showPercentage is true', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 25, max: 100, showPercentage: true },
    })

    expect(wrapper.text()).toContain('25.0%')
  })

  it('should hide percentage when showPercentage is false', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 25, max: 100, showPercentage: false, showValue: false },
    })

    expect(wrapper.text()).not.toContain('%')
  })

  it('should apply custom color', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, color: '#ff0000' },
    })

    const fill = wrapper.find('.progress-fill')
    expect(fill.attributes('style')).toContain('background-color: rgb(255, 0, 0)')
  })

  it('should apply custom height', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, height: '1rem' },
    })

    const track = wrapper.find('.progress-track')
    expect(track.attributes('style')).toContain('height: 1rem')
  })

  it('should have animation class when animated is true', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, animated: true },
    })

    const fill = wrapper.find('.progress-fill')
    expect(fill.classes()).toContain('progress-animated')
  })

  it('should not have animation class when animated is false', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, animated: false },
    })

    const fill = wrapper.find('.progress-fill')
    expect(fill.classes()).not.toContain('progress-animated')
  })

  it('should handle zero max value', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 50, max: 0 },
    })

    const fill = wrapper.find('.progress-fill')
    expect(fill.attributes('style')).toContain('width: 0%')
  })

  it('should format large values correctly', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 1500, max: 2000000, showValue: true },
    })

    expect(wrapper.text()).toContain('1.5K')
    expect(wrapper.text()).toContain('2.0M')
  })

  it('should format decimal values correctly', () => {
    const wrapper = mount(ProgressBar, {
      props: { value: 0.75, max: 1, showValue: true },
    })

    expect(wrapper.text()).toContain('0.75')
  })
})
