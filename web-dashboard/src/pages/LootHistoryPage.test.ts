import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import LootHistoryPage from './LootHistoryPage.vue'
import { lootApi } from '@/api/loot'
import type { LootHistoryResponse, LootAward } from '@/types'

// Mock the APIs
vi.mock('@/api/loot', () => ({
  lootApi: {
    getMyLootHistory: vi.fn(),
  },
}))

// Mock composables that interact with DOM
vi.mock('@/composables/useWowhead', () => ({
  useWowhead: vi.fn(),
}))

describe('LootHistoryPage', () => {
  const mockAwards: LootAward[] = [
    {
      id: 1,
      itemId: 12345,
      itemName: 'Sword of Glory',
      raiderId: 1,
      characterName: 'TestRaider',
      awardedAt: '2026-01-10T20:00:00Z',
      flpsAtAward: 0.85,
      rdfExpired: true,
    },
    {
      id: 2,
      itemId: 12346,
      itemName: 'Helm of Victory',
      raiderId: 1,
      characterName: 'TestRaider',
      awardedAt: '2026-01-08T20:00:00Z',
      flpsAtAward: 0.78,
      rdfExpired: false,
      rdfExpiresAt: '2026-02-08T20:00:00Z',
    },
    {
      id: 3,
      itemId: 12347,
      itemName: 'Ring of Power',
      raiderId: 1,
      characterName: 'TestRaider',
      awardedAt: '2025-12-15T20:00:00Z',
      flpsAtAward: 0.72,
      rdfExpired: true,
    },
  ]

  const mockLootData: LootHistoryResponse = {
    raiderId: 1,
    characterName: 'TestRaider',
    awards: mockAwards,
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(LootHistoryPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          WowheadItem: {
            template: '<span class="wowhead-item">{{ itemName }}</span>',
            props: ['itemId', 'itemName', 'quality'],
          },
          DonutChart: true,
          BarChart: true,
          SkeletonCard: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(lootApi.getMyLootHistory).mockResolvedValue(mockLootData)
  })

  it('should render page title "Loot History"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Loot History')
  })

  it('should display loot awards in list', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Sword of Glory')
    expect(wrapper.text()).toContain('Helm of Victory')
    expect(wrapper.text()).toContain('Ring of Power')
  })

  it('should show item names with WowheadItem component', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const wowheadItems = wrapper.findAll('.wowhead-item')
    expect(wowheadItems.length).toBe(3)
  })

  it('should display RDF status indicators', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Should show RDF Expired for items with rdfExpired: true
    expect(wrapper.text()).toContain('RDF Expired')

    // Should show RDF: <time> for items with rdfExpired: false
    expect(wrapper.text()).toContain('RDF:')
  })

  it('should show FLPS at award time for each item', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('0.850')
    expect(wrapper.text()).toContain('0.780')
    expect(wrapper.text()).toContain('0.720')
  })

  it('should handle loading state with skeleton', () => {
    // Make API never resolve to keep loading state
    vi.mocked(lootApi.getMyLootHistory).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.findComponent({ name: 'SkeletonCard' }).exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(lootApi.getMyLootHistory).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load loot history')
  })

  it('should show empty state when no loot data', async () => {
    vi.mocked(lootApi.getMyLootHistory).mockResolvedValue({
      ...mockLootData,
      awards: [],
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No loot history found')
  })

  it('should display total items count in stats', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('3')
    expect(wrapper.text()).toContain('Total Items')
  })

  it('should display average FLPS at award in stats', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Average of 0.85 + 0.78 + 0.72 = 2.35 / 3 = 0.783
    expect(wrapper.text()).toContain('0.783')
    expect(wrapper.text()).toContain('Avg FLPS at Award')
  })

  it('should display RDF cleared count in stats', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // 2 items have rdfExpired: true
    expect(wrapper.text()).toContain('2')
    expect(wrapper.text()).toContain('RDF Cleared')
  })
})
