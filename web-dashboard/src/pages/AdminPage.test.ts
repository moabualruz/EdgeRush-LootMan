import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import AdminPage from './AdminPage.vue'

describe('AdminPage', () => {
  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(AdminPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          ConfigEditor: true,
          BehavioralActionsPanel: true,
          LootBansPanel: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render page title "Admin Panel"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Admin Panel')
  })

  it('should display config editor section by default', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.findComponent({ name: 'ConfigEditor' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'BehavioralActionsPanel' }).exists()).toBe(false)
    expect(wrapper.findComponent({ name: 'LootBansPanel' }).exists()).toBe(false)
  })

  it('should display behavioral actions panel when clicking tab', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Click behavioral actions tab
    const actionsTab = wrapper.findAll('button').find((b) => b.text() === 'Behavioral Actions')
    await actionsTab?.trigger('click')

    expect(wrapper.findComponent({ name: 'BehavioralActionsPanel' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ConfigEditor' }).exists()).toBe(false)
  })

  it('should display loot bans panel when clicking tab', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Click loot bans tab
    const bansTab = wrapper.findAll('button').find((b) => b.text() === 'Loot Bans')
    await bansTab?.trigger('click')

    expect(wrapper.findComponent({ name: 'LootBansPanel' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ConfigEditor' }).exists()).toBe(false)
  })

  it('should show all tab navigation options', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('FLPS Configuration')
    expect(wrapper.text()).toContain('Behavioral Actions')
    expect(wrapper.text()).toContain('Loot Bans')
  })

  it('should highlight active tab with primary color', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Default tab should be active
    const configTab = wrapper.findAll('button').find((b) => b.text() === 'FLPS Configuration')
    expect(configTab?.classes()).toContain('border-primary-500')
    expect(configTab?.classes()).toContain('text-primary-400')
  })

  it('should switch between tabs correctly', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Start with config
    expect(wrapper.findComponent({ name: 'ConfigEditor' }).exists()).toBe(true)

    // Switch to actions
    const actionsTab = wrapper.findAll('button').find((b) => b.text() === 'Behavioral Actions')
    await actionsTab?.trigger('click')
    expect(wrapper.findComponent({ name: 'BehavioralActionsPanel' }).exists()).toBe(true)

    // Switch to bans
    const bansTab = wrapper.findAll('button').find((b) => b.text() === 'Loot Bans')
    await bansTab?.trigger('click')
    expect(wrapper.findComponent({ name: 'LootBansPanel' }).exists()).toBe(true)

    // Switch back to config
    const configTab = wrapper.findAll('button').find((b) => b.text() === 'FLPS Configuration')
    await configTab?.trigger('click')
    expect(wrapper.findComponent({ name: 'ConfigEditor' }).exists()).toBe(true)
  })
})
