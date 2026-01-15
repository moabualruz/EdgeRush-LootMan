import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import DashboardPage from './DashboardPage.vue'
import { flpsApi } from '@/api/flps'
import { lootApi } from '@/api/loot'
import type { FlpsScore, LootAward } from '@/types'

// Mock the APIs
vi.mock('@/api/flps', () => ({
  flpsApi: {
    getMyFlps: vi.fn(),
  },
}))

vi.mock('@/api/loot', () => ({
  lootApi: {
    getMyLootHistory: vi.fn(),
  },
}))

describe('DashboardPage', () => {
  const mockFlpsData: FlpsScore = {
    raiderId: 1,
    characterName: 'TestRaider',
    characterClass: 'WARRIOR',
    role: 'DPS',
    flps: 0.85,
    rms: { value: 0.9, acs: 0.95, mas: 0.85, eps: 0.9 },
    ipi: { value: 0.8, uv: 0.75, tierBonus: 0.1, roleMultiplier: 1.0 },
    rdf: 0.95,
    eligible: true,
    rank: 5,
  }

  const mockLootData = {
    raiderId: 1,
    characterName: 'TestRaider',
    awards: [
      {
        id: 1,
        itemId: 12345,
        itemName: 'Sword of Testing',
        raiderId: 1,
        characterName: 'TestRaider',
        awardedAt: '2026-01-10T20:00:00Z',
        flpsAtAward: 0.82,
        rdfExpired: false,
      },
      {
        id: 2,
        itemId: 12346,
        itemName: 'Helm of Trials',
        raiderId: 1,
        characterName: 'TestRaider',
        awardedAt: '2026-01-08T20:00:00Z',
        flpsAtAward: 0.78,
        rdfExpired: true,
      },
    ] as LootAward[],
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(DashboardPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          ScoreCard: true,
          ScoreBreakdown: true,
          FlpsVisualization: true,
          RecentLoot: true,
          SkeletonCard: true,
          SkeletonProfile: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(flpsApi.getMyFlps).mockResolvedValue(mockFlpsData)
    vi.mocked(lootApi.getMyLootHistory).mockResolvedValue(mockLootData)
  })

  it('should render page title "Dashboard"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Dashboard')
  })

  it('should display ScoreCard component when data loads', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.findComponent({ name: 'ScoreCard' }).exists()).toBe(true)
  })

  it('should display ScoreBreakdown component in simple view', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.findComponent({ name: 'ScoreBreakdown' }).exists()).toBe(true)
  })

  it('should display RecentLoot component when loot data loads', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.findComponent({ name: 'RecentLoot' }).exists()).toBe(true)
  })

  it('should show loading skeleton when data is loading', () => {
    // Make API never resolve to keep loading state
    vi.mocked(flpsApi.getMyFlps).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.findComponent({ name: 'SkeletonProfile' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'SkeletonCard' }).exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(flpsApi.getMyFlps).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load FLPS data')
  })

  it('should toggle between simple and detailed view', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Initially in simple view - ScoreBreakdown visible
    expect(wrapper.findComponent({ name: 'ScoreBreakdown' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'FlpsVisualization' }).exists()).toBe(false)

    // Click toggle button
    const toggleButton = wrapper.find('button')
    expect(toggleButton.text()).toContain('Detailed View')
    await toggleButton.trigger('click')

    // Now in detailed view - FlpsVisualization visible
    expect(wrapper.findComponent({ name: 'FlpsVisualization' }).exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'ScoreBreakdown' }).exists()).toBe(false)

    // Button text should change
    expect(toggleButton.text()).toContain('Simple View')
  })

  it('should show eligibility warning when raider is ineligible', async () => {
    vi.mocked(flpsApi.getMyFlps).mockResolvedValue({
      ...mockFlpsData,
      eligible: false,
      ineligibilityReasons: ['Attendance below threshold', 'Missing enchants'],
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Eligibility Issues')
    expect(wrapper.text()).toContain('Attendance below threshold')
    expect(wrapper.text()).toContain('Missing enchants')
  })

  it('should not show eligibility warning when raider is eligible', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).not.toContain('Eligibility Issues')
  })

  it('should show "No loot history found" when no loot data', async () => {
    // Return null/undefined to trigger the empty state
    vi.mocked(lootApi.getMyLootHistory).mockResolvedValue(null as any)

    const wrapper = mountComponent()
    await flushPromises()

    // When lootData is falsy, the fallback text is shown
    expect(wrapper.text()).toContain('No loot history found')
  })
})
