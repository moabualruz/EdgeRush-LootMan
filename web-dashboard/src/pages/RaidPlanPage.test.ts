import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import RaidPlanPage from './RaidPlanPage.vue'
import { raidPlanApi, type RaidPlan } from '@/api/raidplan'

// Mock the API
vi.mock('@/api/raidplan', () => ({
  raidPlanApi: {
    getPlan: vi.fn(),
    updatePlan: vi.fn(),
    addStep: vi.fn(),
    removeStep: vi.fn(),
    updateStep: vi.fn(),
  },
}))

// Mock vue-router
const mockRoute = {
  params: { id: 'test-plan-123' },
}
const mockRouter = {
  push: vi.fn(),
  back: vi.fn(),
}
vi.mock('vue-router', () => ({
  useRoute: () => mockRoute,
  useRouter: () => mockRouter,
}))

describe('RaidPlanPage', () => {
  const mockPlan: RaidPlan = {
    id: 'test-plan-123',
    guildId: 'test-guild',
    encounterId: 2902,
    encounterName: 'Queen Ansurek',
    name: 'Phase 1 Positions',
    steps: [
      {
        order: 0,
        notes: 'Initial pull positions',
        markers: [
          { type: 'SKULL', x: 50, y: 50 },
          { type: 'TANK', x: 30, y: 30, label: 'MT' },
        ],
        shapes: [
          { shapeType: 'CIRCLE', x1: 50, y1: 50, radius: 15, strokeWidth: 2, color: '#ff0000' },
        ],
      },
      {
        order: 1,
        notes: 'Phase transition',
        markers: [],
        shapes: [],
      },
    ],
    visibility: 'GUILD',
    shareToken: null,
    createdBy: 1,
    createdAt: '2026-01-15T10:00:00Z',
    updatedAt: '2026-01-15T12:00:00Z',
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(RaidPlanPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          PlanCanvas: true,
          MarkerPalette: true,
          StepTimeline: true,
          Skeleton: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(raidPlanApi.getPlan).mockResolvedValue(mockPlan)
    vi.mocked(raidPlanApi.updatePlan).mockResolvedValue(mockPlan)
    vi.mocked(raidPlanApi.addStep).mockResolvedValue({
      ...mockPlan,
      steps: [...mockPlan.steps, { order: 2, notes: null, markers: [], shapes: [] }],
    })
  })

  describe('Rendering', () => {
    it('should render page with encounter name', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.text()).toContain('Queen Ansurek')
    })

    it('should render plan name', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.text()).toContain('Phase 1 Positions')
    })

    it('should render PlanCanvas component', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.findComponent({ name: 'PlanCanvas' }).exists()).toBe(true)
    })

    it('should render MarkerPalette component', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.findComponent({ name: 'MarkerPalette' }).exists()).toBe(true)
    })

    it('should render StepTimeline component', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.findComponent({ name: 'StepTimeline' }).exists()).toBe(true)
    })
  })

  describe('Loading State', () => {
    it('should show loading skeleton when fetching plan', () => {
      vi.mocked(raidPlanApi.getPlan).mockImplementation(() => new Promise(() => {}))

      const wrapper = mountComponent()

      expect(wrapper.findComponent({ name: 'Skeleton' }).exists()).toBe(true)
    })
  })

  describe('Error State', () => {
    it('should show error message when plan fails to load', async () => {
      vi.mocked(raidPlanApi.getPlan).mockRejectedValue(new Error('Not found'))

      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.text()).toContain('Failed to load plan')
    })

    it('should show back button on error', async () => {
      vi.mocked(raidPlanApi.getPlan).mockRejectedValue(new Error('Not found'))

      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="back-button"]').exists()).toBe(true)
    })
  })

  describe('Canvas Integration', () => {
    it('should pass current step markers to canvas', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      const canvas = wrapper.findComponent({ name: 'PlanCanvas' })
      expect(canvas.props('markers')).toEqual(mockPlan.steps[0].markers)
    })

    it('should pass current step shapes to canvas', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      const canvas = wrapper.findComponent({ name: 'PlanCanvas' })
      expect(canvas.props('shapes')).toEqual(mockPlan.steps[0].shapes)
    })
  })

  describe('Step Navigation', () => {
    it('should pass steps to timeline', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      const timeline = wrapper.findComponent({ name: 'StepTimeline' })
      expect(timeline.props('steps')).toEqual(mockPlan.steps)
    })

    it('should update current step on timeline step-change', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      const timeline = wrapper.findComponent({ name: 'StepTimeline' })
      await timeline.vm.$emit('step-change', 1)

      expect(wrapper.vm.currentStep).toBe(1)
    })
  })

  describe('Tool Selection', () => {
    it('should pass selected tool to canvas', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      const canvas = wrapper.findComponent({ name: 'PlanCanvas' })
      expect(canvas.props('currentTool')).toBeDefined()
    })

    it('should update tool on palette tool-select', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      const palette = wrapper.findComponent({ name: 'MarkerPalette' })
      await palette.vm.$emit('tool-select', 'pan')

      const canvas = wrapper.findComponent({ name: 'PlanCanvas' })
      expect(canvas.props('currentTool')).toBe('pan')
    })
  })

  describe('Header Actions', () => {
    it('should show save button', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="save-button"]').exists()).toBe(true)
    })

    it('should show share button', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="share-button"]').exists()).toBe(true)
    })

    it('should show back button', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      expect(wrapper.find('[data-testid="back-button"]').exists()).toBe(true)
    })

    it('should navigate back when back button clicked', async () => {
      const wrapper = mountComponent()
      await flushPromises()

      await wrapper.find('[data-testid="back-button"]').trigger('click')

      expect(mockRouter.back).toHaveBeenCalled()
    })
  })
})
