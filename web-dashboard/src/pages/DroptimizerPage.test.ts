import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'
import DroptimizerPage from './DroptimizerPage.vue'

// Mock the API
vi.mock('@/api/simulation', () => ({
  simulationApi: {
    submitSimulation: vi.fn(),
    getSimulationResults: vi.fn(),
    getSimulationStatus: vi.fn(),
  },
}))

// Mock useQuery
const mockResults = ref({
  results: [
    {
      itemId: 12345,
      itemName: 'Thunderfury',
      slot: 'main_hand',
      dpsGain: 1500,
      percentGain: 3.5,
      isUpgrade: true,
      normalizedValue: 0.75,
      simulatedAt: '2026-02-05T12:00:00Z',
    },
  ],
  characterName: 'TestChar',
  characterRealm: 'TestRealm',
})

vi.mock('@tanstack/vue-query', () => ({
  useQuery: vi.fn(() => ({
    data: mockResults,
    isLoading: ref(false),
    isError: ref(false),
    error: ref(null),
    refetch: vi.fn(),
  })),
  useMutation: vi.fn(() => ({
    mutate: vi.fn(),
    isPending: ref(false),
    isError: ref(false),
  })),
}))

// Mock auth store
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    currentGuildId: 'guild-123',
  }),
}))

describe('DroptimizerPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render the page title', () => {
    const wrapper = mount(DroptimizerPage, {
      global: {
        stubs: ['SimulationResults', 'RouterLink'],
      },
    })

    expect(wrapper.text()).toContain('Droptimizer')
  })

  it('should have a character selector', () => {
    const wrapper = mount(DroptimizerPage, {
      global: {
        stubs: ['SimulationResults', 'RouterLink'],
      },
    })

    const selector = wrapper.find('[data-testid="character-selector"]')
    expect(selector.exists()).toBe(true)
  })

  it('should have source filter options', () => {
    const wrapper = mount(DroptimizerPage, {
      global: {
        stubs: ['SimulationResults', 'RouterLink'],
      },
    })

    expect(wrapper.text()).toContain('Raid')
    expect(wrapper.text()).toContain('M+')
    expect(wrapper.text()).toContain('Vault')
  })

  it('should display SimulationResults component', async () => {
    const wrapper = mount(DroptimizerPage, {
      global: {
        stubs: ['RouterLink'],
        components: {
          SimulationResults: {
            template: '<div data-testid="sim-results">Mock Results</div>',
          },
        },
      },
    })

    await flushPromises()
    expect(wrapper.find('[data-testid="sim-results"]').exists()).toBe(true)
  })

  it('should have a run simulation button', () => {
    const wrapper = mount(DroptimizerPage, {
      global: {
        stubs: ['SimulationResults', 'RouterLink'],
      },
    })

    const button = wrapper.find('[data-testid="run-simulation-btn"]')
    expect(button.exists()).toBe(true)
    expect(button.text()).toContain('Run Simulation')
  })

  it('should show loading state when simulation is running', async () => {
    const wrapper = mount(DroptimizerPage, {
      global: {
        stubs: ['SimulationResults', 'RouterLink'],
      },
    })

    // Trigger simulation
    await wrapper.find('[data-testid="run-simulation-btn"]').trigger('click')

    // Check for loading indicator
    expect(wrapper.find('[data-testid="simulation-progress"]').exists()).toBe(true)
  })

  it('should display slot filter dropdown', () => {
    const wrapper = mount(DroptimizerPage, {
      global: {
        stubs: ['SimulationResults', 'RouterLink'],
      },
    })

    const slotFilter = wrapper.find('[data-testid="slot-filter"]')
    expect(slotFilter.exists()).toBe(true)
  })

  it('should show upgrade priority chart when results exist', async () => {
    const wrapper = mount(DroptimizerPage, {
      global: {
        stubs: ['SimulationResults', 'RouterLink'],
      },
    })

    await flushPromises()
    expect(wrapper.find('[data-testid="upgrade-chart"]').exists()).toBe(true)
  })
})
