import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SkeletonProfile from './SkeletonProfile.vue'
import Skeleton from './Skeleton.vue'

describe('SkeletonProfile', () => {
  it('should render with default props', () => {
    const wrapper = mount(SkeletonProfile)

    // Avatar + Name + Subtitle + (3 stats × 2 skeletons each) = 9 skeletons
    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(9)
  })

  it('should hide avatar when showAvatar is false', () => {
    const wrapper = mount(SkeletonProfile, {
      props: {
        showAvatar: false,
      },
    })

    // No avatar: Name + Subtitle + (3 stats × 2) = 8 skeletons
    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(8)
  })

  it('should render circle avatar skeleton', () => {
    const wrapper = mount(SkeletonProfile)

    const skeletons = wrapper.findAllComponents(Skeleton)
    // First skeleton should be the avatar (circle shape)
    expect(skeletons[0].props('shape')).toBe('circle')
  })

  it('should hide stats when showStats is false', () => {
    const wrapper = mount(SkeletonProfile, {
      props: {
        showStats: false,
      },
    })

    // Avatar + Name + Subtitle = 3 skeletons
    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(3)
  })

  it('should render custom stats count', () => {
    const wrapper = mount(SkeletonProfile, {
      props: {
        statsCount: 5,
        showAvatar: false,
      },
    })

    // Name + Subtitle + (5 stats × 2) = 12 skeletons
    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(12)
  })

  it('should apply custom avatar size', () => {
    const wrapper = mount(SkeletonProfile, {
      props: {
        avatarSize: '6rem',
      },
    })

    const avatarSkeleton = wrapper.findAllComponents(Skeleton)[0]
    expect(avatarSkeleton.props('height')).toBe('6rem')
  })

  it('should render with card styling', () => {
    const wrapper = mount(SkeletonProfile)

    expect(wrapper.find('.bg-gray-800\\/50').exists()).toBe(true)
    expect(wrapper.find('.rounded-xl').exists()).toBe(true)
  })
})
