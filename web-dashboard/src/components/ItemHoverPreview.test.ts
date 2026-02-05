import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ItemHoverPreview from './ItemHoverPreview.vue'

// Mock useWowhead composable
vi.mock('@/composables/useWowhead', () => ({
  getWowheadItemUrl: vi.fn((itemId: number) => `https://www.wowhead.com/item=${itemId}`),
}))

describe('ItemHoverPreview', () => {
  const defaultProps = {
    itemId: 12345,
  }

  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('should render the wrapper element', () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: defaultProps,
      slots: {
        default: '<span>Test Item</span>'
      }
    })
    expect(wrapper.find('.item-hover-preview-wrapper').exists()).toBe(true)
  })

  it('should render slot content', () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: defaultProps,
      slots: {
        default: '<span class="test-slot">Test Item</span>'
      }
    })
    expect(wrapper.find('.test-slot').exists()).toBe(true)
    expect(wrapper.text()).toContain('Test Item')
  })

  it('should not show preview initially', () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: defaultProps,
      slots: { default: '<span>Test</span>' }
    })
    expect(wrapper.find('.item-preview-panel').exists()).toBe(false)
  })

  it('should show preview after hover delay', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, showDelay: 100 },
      slots: { default: '<span>Test</span>' }
    })

    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    
    // Preview should not show immediately
    expect(wrapper.find('.item-preview-panel').exists()).toBe(false)

    // Advance timers past the delay
    vi.advanceTimersByTime(150)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.item-preview-panel').exists()).toBe(true)
  })

  it('should hide preview on mouse leave', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, showDelay: 50 },
      slots: { default: '<span>Test</span>' }
    })

    // Show preview
    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    vi.advanceTimersByTime(100)
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.item-preview-panel').exists()).toBe(true)

    // Hide preview
    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseleave')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.item-preview-panel').exists()).toBe(false)
  })

  it('should not show preview if mouse leaves before delay', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, showDelay: 200 },
      slots: { default: '<span>Test</span>' }
    })

    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    vi.advanceTimersByTime(50) // Only 50ms, not enough
    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseleave')
    vi.advanceTimersByTime(200)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.item-preview-panel').exists()).toBe(false)
  })

  it('should show item ID in preview', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, showDelay: 0 },
      slots: { default: '<span>Test</span>' }
    })

    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    vi.advanceTimersByTime(10)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Item ID: 12345')
  })

  it('should include WoWHead link', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, showDelay: 0 },
      slots: { default: '<span>Test</span>' }
    })

    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    vi.advanceTimersByTime(10)
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('View on WoWHead')
    const link = wrapper.find('a[href*="wowhead"]')
    expect(link.exists()).toBe(true)
  })

  it('should apply correct position class for right position', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, position: 'right', showDelay: 0 },
      slots: { default: '<span>Test</span>' }
    })

    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    vi.advanceTimersByTime(10)
    await wrapper.vm.$nextTick()

    const panel = wrapper.find('.item-preview-panel')
    expect(panel.classes()).toContain('left-full')
  })

  it('should apply correct position class for top position', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, position: 'top', showDelay: 0 },
      slots: { default: '<span>Test</span>' }
    })

    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    vi.advanceTimersByTime(10)
    await wrapper.vm.$nextTick()

    const panel = wrapper.find('.item-preview-panel')
    expect(panel.classes()).toContain('bottom-full')
  })

  it('should show icon when showIcon is true', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, showIcon: true, showDelay: 0 },
      slots: { default: '<span>Test</span>' }
    })

    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    vi.advanceTimersByTime(10)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.item-icon').exists()).toBe(true)
  })

  it('should hide icon when showIcon is false', async () => {
    const wrapper = mount(ItemHoverPreview, { 
      props: { ...defaultProps, showIcon: false, showDelay: 0 },
      slots: { default: '<span>Test</span>' }
    })

    await wrapper.find('.item-hover-preview-wrapper').trigger('mouseenter')
    vi.advanceTimersByTime(10)
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.item-icon').exists()).toBe(false)
  })
})
