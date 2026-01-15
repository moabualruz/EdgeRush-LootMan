import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import WowheadItem from './WowheadItem.vue'

// Mock the useWowhead composable
vi.mock('@/composables/useWowhead', () => ({
  getWowheadItemUrl: (itemId: number, bonusIds?: number[]) => {
    let url = `https://www.wowhead.com/item=${itemId}`
    if (bonusIds && bonusIds.length > 0) {
      url += `?bonus=${bonusIds.join(':')}`
    }
    return url
  },
}))

describe('WowheadItem', () => {
  it('should render item name', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Epic Sword of Testing',
      },
    })
    expect(wrapper.text()).toContain('Epic Sword of Testing')
  })

  it('should generate correct Wowhead URL', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
      },
    })
    const link = wrapper.find('a')
    expect(link.attributes('href')).toBe('https://www.wowhead.com/item=12345')
  })

  it('should include bonus IDs in URL', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        bonusIds: [1, 2, 3],
      },
    })
    const link = wrapper.find('a')
    expect(link.attributes('href')).toBe('https://www.wowhead.com/item=12345?bonus=1:2:3')
  })

  it('should apply purple color for epic quality (default)', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
      },
    })
    expect(wrapper.find('a').classes()).toContain('text-purple-400')
  })

  it('should apply legendary color for legendary quality', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        quality: 'legendary',
      },
    })
    expect(wrapper.find('a').classes()).toContain('text-orange-400')
  })

  it('should apply rare color for rare quality', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        quality: 'rare',
      },
    })
    expect(wrapper.find('a').classes()).toContain('text-blue-400')
  })

  it('should apply uncommon color for uncommon quality', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        quality: 'uncommon',
      },
    })
    expect(wrapper.find('a').classes()).toContain('text-green-400')
  })

  it('should apply common color for common quality', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        quality: 'common',
      },
    })
    expect(wrapper.find('a').classes()).toContain('text-white')
  })

  it('should apply poor color for poor quality', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        quality: 'poor',
      },
    })
    expect(wrapper.find('a').classes()).toContain('text-gray-500')
  })

  it('should apply artifact color for artifact quality', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        quality: 'artifact',
      },
    })
    expect(wrapper.find('a').classes()).toContain('text-yellow-300')
  })

  it('should apply heirloom color for heirloom quality', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        quality: 'heirloom',
      },
    })
    expect(wrapper.find('a').classes()).toContain('text-cyan-300')
  })

  it('should include data-wowhead attribute for tooltip', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
      },
    })
    const link = wrapper.find('a')
    expect(link.attributes('data-wowhead')).toBe('item=12345')
  })

  it('should include bonus IDs in data-wowhead attribute', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
        bonusIds: [1808, 6652],
      },
    })
    const link = wrapper.find('a')
    expect(link.attributes('data-wowhead')).toBe('item=12345&bonus=1808:6652')
  })

  it('should open link in new tab', () => {
    const wrapper = mount(WowheadItem, {
      props: {
        itemId: 12345,
        itemName: 'Test Item',
      },
    })
    const link = wrapper.find('a')
    expect(link.attributes('target')).toBe('_blank')
    expect(link.attributes('rel')).toBe('noopener noreferrer')
  })
})
