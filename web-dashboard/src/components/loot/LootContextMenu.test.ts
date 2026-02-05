import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import LootContextMenu from './LootContextMenu.vue'

describe('LootContextMenu', () => {
  const defaultProps = {
    isOpen: true,
    position: { x: 100, y: 200 },
    awardId: 1,
    itemName: 'Test Item',
  }

  const mountOptions = {
    global: {
      stubs: {
        Teleport: true,
      },
    },
  }

  it('should render menu when isOpen is true', () => {
    const wrapper = mount(LootContextMenu, {
      props: defaultProps,
      ...mountOptions,
    })

    expect(wrapper.find('[data-testid="context-menu"]').exists()).toBe(true)
  })

  it('should not render menu when isOpen is false', () => {
    const wrapper = mount(LootContextMenu, {
      props: { ...defaultProps, isOpen: false },
      ...mountOptions,
    })

    expect(wrapper.find('[data-testid="context-menu"]').exists()).toBe(false)
  })

  it('should emit edit event when Edit is clicked', async () => {
    const wrapper = mount(LootContextMenu, {
      props: defaultProps,
      ...mountOptions,
    })

    await wrapper.find('[data-testid="edit-button"]').trigger('click')

    expect(wrapper.emitted('edit')).toBeTruthy()
    expect(wrapper.emitted('edit')![0]).toEqual([1])
  })

  it('should emit revoke event when Revoke is clicked', async () => {
    const wrapper = mount(LootContextMenu, {
      props: defaultProps,
      ...mountOptions,
    })

    await wrapper.find('[data-testid="revoke-button"]').trigger('click')

    expect(wrapper.emitted('revoke')).toBeTruthy()
    expect(wrapper.emitted('revoke')![0]).toEqual([1])
  })

  it('should emit close event when clicking backdrop', async () => {
    const wrapper = mount(LootContextMenu, {
      props: defaultProps,
      ...mountOptions,
    })

    await wrapper.find('[data-testid="backdrop"]').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('should position menu at provided coordinates', () => {
    const wrapper = mount(LootContextMenu, {
      props: { ...defaultProps, position: { x: 150, y: 300 } },
      ...mountOptions,
    })

    const menu = wrapper.find('[data-testid="context-menu"]')
    expect(menu.attributes('style')).toContain('left: 150px')
    expect(menu.attributes('style')).toContain('top: 300px')
  })
})
