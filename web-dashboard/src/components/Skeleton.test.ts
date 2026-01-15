import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Skeleton from './Skeleton.vue'

describe('Skeleton', () => {
  it('should render with default props', () => {
    const wrapper = mount(Skeleton)

    const element = wrapper.find('.skeleton')
    expect(element.exists()).toBe(true)
    expect(element.classes()).toContain('skeleton-animated')
    expect(element.classes()).toContain('rounded-md')
  })

  it('should apply custom width and height', () => {
    const wrapper = mount(Skeleton, {
      props: {
        width: '200px',
        height: '50px',
      },
    })

    const element = wrapper.find('.skeleton')
    expect(element.attributes('style')).toContain('width: 200px')
    expect(element.attributes('style')).toContain('height: 50px')
  })

  it('should render circle shape', () => {
    const wrapper = mount(Skeleton, {
      props: {
        shape: 'circle',
        height: '4rem',
      },
    })

    const element = wrapper.find('.skeleton')
    expect(element.classes()).toContain('rounded-full')
    // Circle should use height for both dimensions
    expect(element.attributes('style')).toContain('width: 4rem')
    expect(element.attributes('style')).toContain('height: 4rem')
  })

  it('should render text shape', () => {
    const wrapper = mount(Skeleton, {
      props: {
        shape: 'text',
      },
    })

    const element = wrapper.find('.skeleton')
    expect(element.classes()).toContain('rounded')
  })

  it('should disable animation when animated is false', () => {
    const wrapper = mount(Skeleton, {
      props: {
        animated: false,
      },
    })

    const element = wrapper.find('.skeleton')
    expect(element.classes()).not.toContain('skeleton-animated')
  })

  it('should use full width by default', () => {
    const wrapper = mount(Skeleton)

    const element = wrapper.find('.skeleton')
    expect(element.attributes('style')).toContain('width: 100%')
  })

  it('should use 1rem height by default', () => {
    const wrapper = mount(Skeleton)

    const element = wrapper.find('.skeleton')
    expect(element.attributes('style')).toContain('height: 1rem')
  })
})
