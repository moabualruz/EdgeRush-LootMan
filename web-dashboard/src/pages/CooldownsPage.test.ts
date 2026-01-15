import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import CooldownsPage from './CooldownsPage.vue'

// Mock vue-router
const mockRoute = {
  params: { encounterId: '2902' },
  query: { planId: 'test-plan' },
}
vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => ({
    push: vi.fn(),
    back: vi.fn(),
  }),
}))

// Mock auth store
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    guildId: 'test-guild',
  }),
}))

describe('CooldownsPage', () => {
  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(CooldownsPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          CooldownGrid: true,
          Skeleton: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    // Mock clipboard API (not available in JSDOM)
    Object.assign(navigator, {
      clipboard: {
        writeText: vi.fn().mockResolvedValue(undefined),
      },
    })
  })

  describe('Rendering', () => {
    it('should render page title "Cooldown Assignments"', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('h1').text()).toContain('Cooldown')
    })

    it('should render CooldownGrid component', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.findComponent({ name: 'CooldownGrid' }).exists()).toBe(true)
    })

    it('should show encounter selector', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="encounter-selector"]').exists()).toBe(true)
    })
  })

  describe('Encounter Selection', () => {
    it('should display available encounters', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      const selector = wrapper.find('[data-testid="encounter-selector"]')
      expect(selector.exists()).toBe(true)
    })
  })

  describe('Assignment Actions', () => {
    it('should show save button', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="save-button"]').exists()).toBe(true)
    })

    it('should show reset button', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="reset-button"]').exists()).toBe(true)
    })
  })

  describe('Export Integration', () => {
    it('should show copy notification on export', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      // Trigger an export from the grid
      const grid = wrapper.findComponent({ name: 'CooldownGrid' })
      await grid.vm.$emit('export-mrt', 'test note')

      // Should show a toast/notification
      expect(wrapper.find('[data-testid="toast"]').exists() || wrapper.text().includes('Copied')).toBeTruthy()
    })
  })
})
