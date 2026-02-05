import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'
import TopGearPage from './TopGearPage.vue'

// Mock the API
vi.mock('@/api/simulation', () => ({
  simulationApi: {
    submitSimulation: vi.fn(),
    getSimulationResults: vi.fn(),
  },
}))

vi.mock('@/api/gear', () => ({
  gearApi: {
    getCharacterGear: vi.fn(),
  },
}))

const mockGear = ref({
  equipped: [
    { slot: 'head', itemId: 1001, itemName: 'Helm of Power', itemLevel: 639 },
    { slot: 'chest', itemId: 1002, itemName: 'Chestplate of Might', itemLevel: 636 },
  ],
  bags: [
    { slot: 'head', itemId: 1003, itemName: 'Better Helm', itemLevel: 645 },
  ],
})

vi.mock('@tanstack/vue-query', () => ({
  useQuery: vi.fn(() => ({
    data: mockGear,
    isLoading: ref(false),
    isError: ref(false),
  })),
  useMutation: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: ref(false),
  })),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    currentGuildId: 'guild-123',
  }),
}))

describe('TopGearPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render the page title', () => {
    const wrapper = mount(TopGearPage, {
      global: {
        stubs: ['RouterLink'],
      },
    })

    expect(wrapper.text()).toContain('Top Gear')
  })

  it('should have profile selector', () => {
    const wrapper = mount(TopGearPage, {
      global: { stubs: ['RouterLink'] },
    })

    expect(wrapper.find('[data-testid="profile-selector"]').exists()).toBe(true)
  })

  it('should display profile options', () => {
    const wrapper = mount(TopGearPage, {
      global: { stubs: ['RouterLink'] },
    })

    expect(wrapper.text()).toContain('Single Target')
    expect(wrapper.text()).toContain('AoE')
  })

  it('should show current gear section', () => {
    const wrapper = mount(TopGearPage, {
      global: { stubs: ['RouterLink'] },
    })

    expect(wrapper.find('[data-testid="current-gear"]').exists()).toBe(true)
  })

  it('should show optimal gear section', () => {
    const wrapper = mount(TopGearPage, {
      global: { stubs: ['RouterLink'] },
    })

    expect(wrapper.find('[data-testid="optimal-gear"]').exists()).toBe(true)
  })

  it('should have calculate button', () => {
    const wrapper = mount(TopGearPage, {
      global: { stubs: ['RouterLink'] },
    })

    const button = wrapper.find('[data-testid="calculate-btn"]')
    expect(button.exists()).toBe(true)
    expect(button.text()).toContain('Calculate')
  })

  it('should show tier set tracking', () => {
    const wrapper = mount(TopGearPage, {
      global: { stubs: ['RouterLink'] },
    })

    expect(wrapper.find('[data-testid="tier-set-tracker"]').exists()).toBe(true)
  })
})
