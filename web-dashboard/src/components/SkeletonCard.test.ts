import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SkeletonCard from './SkeletonCard.vue'
import Skeleton from './Skeleton.vue'

describe('SkeletonCard', () => {
  it('should render with default props', () => {
    const wrapper = mount(SkeletonCard)

    expect(wrapper.find('.bg-gray-800\\/50').exists()).toBe(true)
    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(4) // 1 header + 3 lines
  })

  it('should hide header when showHeader is false', () => {
    const wrapper = mount(SkeletonCard, {
      props: {
        showHeader: false,
      },
    })

    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(3) // Only 3 lines
  })

  it('should render custom number of lines', () => {
    const wrapper = mount(SkeletonCard, {
      props: {
        lines: 5,
        showHeader: false,
      },
    })

    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(5)
  })

  it('should render with card styling', () => {
    const wrapper = mount(SkeletonCard)

    expect(wrapper.find('.rounded-xl').exists()).toBe(true)
    expect(wrapper.find('.backdrop-blur-sm').exists()).toBe(true)
    expect(wrapper.find('.border').exists()).toBe(true)
  })
})
