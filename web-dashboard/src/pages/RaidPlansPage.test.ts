import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import RaidPlansPage from './RaidPlansPage.vue'
import { raidPlanApi, type RaidPlan, type PagedRaidPlans } from '@/api/raidplan'

// Mock the API
vi.mock('@/api/raidplan', () => ({
  raidPlanApi: {
    getPlansByGuild: vi.fn(),
    createPlan: vi.fn(),
    deletePlan: vi.fn(),
  },
}))

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}))

// Mock auth store
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    guildId: 'test-guild',
    user: { id: 1 },
  }),
}))

describe('RaidPlansPage', () => {
  const mockPlans: RaidPlan[] = [
    {
      id: 'plan-1',
      guildId: 'test-guild',
      encounterId: 2902,
      encounterName: 'Queen Ansurek',
      name: 'Phase 1 Positions',
      steps: [{ order: 0, notes: 'Pull positions', markers: [], shapes: [] }],
      visibility: 'GUILD',
      shareToken: undefined,
      createdBy: 1,
      createdAt: '2026-01-15T10:00:00Z',
      updatedAt: '2026-01-15T12:00:00Z',
    },
    {
      id: 'plan-2',
      guildId: 'test-guild',
      encounterId: 2901,
      encounterName: 'The Silken Court',
      name: 'Web Phase Strategy',
      steps: [{ order: 0, notes: undefined, markers: [], shapes: [] }],
      visibility: 'PRIVATE',
      shareToken: undefined,
      createdBy: 1,
      createdAt: '2026-01-14T10:00:00Z',
      updatedAt: '2026-01-14T12:00:00Z',
    },
  ]

  const mockPagedPlans: PagedRaidPlans = {
    content: mockPlans,
    page: 0,
    size: 20,
    totalElements: 2,
    totalPages: 1,
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(RaidPlansPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          Skeleton: true,
          SkeletonCard: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockClear()
    vi.mocked(raidPlanApi.getPlansByGuild).mockResolvedValue(mockPagedPlans)
  })

  describe('Rendering', () => {
    it('should render page title "Raid Plans"', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('h1').text()).toBe('Raid Plans')
    })

    it('should display list of plans', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.text()).toContain('Phase 1 Positions')
      expect(wrapper.text()).toContain('Web Phase Strategy')
    })

    it('should show encounter names', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.text()).toContain('Queen Ansurek')
      expect(wrapper.text()).toContain('The Silken Court')
    })

    it('should show visibility badges', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.text()).toContain('Guild')
      expect(wrapper.text()).toContain('Private')
    })
  })

  describe('Loading State', () => {
    it('should show loading skeleton when fetching plans', () => {
      vi.mocked(raidPlanApi.getPlansByGuild).mockImplementation(() => new Promise(() => {}))

      const wrapper = mountComponent()

      expect(wrapper.findComponent({ name: 'SkeletonCard' }).exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('should show empty message when no plans exist', async () => {
      vi.mocked(raidPlanApi.getPlansByGuild).mockResolvedValue({
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      })

      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.text()).toContain('No raid plans yet')
    })
  })

  describe('Navigation', () => {
    it('should navigate to plan editor when plan clicked', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      const planCards = wrapper.findAll('[data-testid="plan-card"]')
      await planCards[0].trigger('click')

      expect(mockPush).toHaveBeenCalledWith('/raid-plans/plan-1')
    })
  })

  describe('Create Plan', () => {
    it('should show create plan button', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="create-plan-button"]').exists()).toBe(true)
    })

    it('should show create plan modal when button clicked', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      await wrapper.find('[data-testid="create-plan-button"]').trigger('click')

      expect(wrapper.find('[data-testid="create-plan-modal"]').exists()).toBe(true)
    })
  })

  describe('Pagination', () => {
    it('should show pagination when multiple pages exist', async () => {
      vi.mocked(raidPlanApi.getPlansByGuild).mockResolvedValue({
        ...mockPagedPlans,
        totalElements: 50,
        totalPages: 3,
      })

      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="pagination"]').exists()).toBe(true)
    })

    it('should hide pagination when single page', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="pagination"]').exists()).toBe(false)
    })
  })
})
