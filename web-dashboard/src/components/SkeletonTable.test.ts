import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SkeletonTable from './SkeletonTable.vue'
import Skeleton from './Skeleton.vue'

describe('SkeletonTable', () => {
  it('should render with default props', () => {
    const wrapper = mount(SkeletonTable)

    // Default: 5 rows, 4 columns, with header
    // Header: 4 skeletons
    // Body: 5 rows × 4 columns = 20 skeletons
    // Total: 24 skeletons
    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(24)
  })

  it('should render correct number of rows and columns', () => {
    const wrapper = mount(SkeletonTable, {
      props: {
        rows: 3,
        columns: 2,
        showHeader: false,
      },
    })

    // 3 rows × 2 columns = 6 skeletons
    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(6)
  })

  it('should hide header when showHeader is false', () => {
    const wrapper = mount(SkeletonTable, {
      props: {
        rows: 2,
        columns: 3,
        showHeader: false,
      },
    })

    // No header, just 2 rows × 3 columns = 6 skeletons
    expect(wrapper.findAllComponents(Skeleton)).toHaveLength(6)
    expect(wrapper.find('.bg-gray-900\\/50').exists()).toBe(false)
  })

  it('should render header row when showHeader is true', () => {
    const wrapper = mount(SkeletonTable, {
      props: {
        showHeader: true,
      },
    })

    expect(wrapper.find('.bg-gray-900\\/50').exists()).toBe(true)
  })

  it('should render with table styling', () => {
    const wrapper = mount(SkeletonTable)

    expect(wrapper.find('.rounded-xl').exists()).toBe(true)
    expect(wrapper.find('.overflow-hidden').exists()).toBe(true)
  })
})
