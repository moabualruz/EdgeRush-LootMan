import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import WishlistPage from './WishlistPage.vue'
import { wishlistApi } from '@/api/wishlist'
import type { WishlistItem } from '@/types'

// Mock the API
vi.mock('@/api/wishlist', () => ({
  wishlistApi: {
    getMyWishlist: vi.fn(),
    getSimulationStatus: vi.fn(),
    triggerSimulation: vi.fn(),
  },
}))

// Mock date utils
vi.mock('@/utils/date', () => ({
  formatRelativeTime: (date: string) => {
    const d = new Date(date)
    return `${Math.floor((Date.now() - d.getTime()) / (1000 * 60 * 60))} hours ago`
  },
}))

describe('WishlistPage', () => {
  const mockWishlistItems: WishlistItem[] = [
    {
      itemId: 1,
      itemName: 'Void-Touched Blade',
      slot: 'Main Hand',
      upgradeValue: 12.5,
      simulationSource: 'RAIDBOTS',
      lastSimulatedAt: '2026-01-14T10:00:00Z',
      isStale: false,
    },
    {
      itemId: 2,
      itemName: 'Crown of Endless Fury',
      slot: 'Head',
      upgradeValue: 7.2,
      simulationSource: 'RAIDBOTS',
      lastSimulatedAt: '2026-01-14T10:00:00Z',
      isStale: false,
    },
    {
      itemId: 3,
      itemName: 'Ring of Dark Whispers',
      slot: 'Finger',
      upgradeValue: 3.1,
      simulationSource: 'WISHLIST_PERCENTAGE',
      lastSimulatedAt: '2026-01-10T10:00:00Z',
      isStale: true,
    },
    {
      itemId: 4,
      itemName: 'Boots of the Void',
      slot: 'Feet',
      upgradeValue: 1.5,
      simulationSource: 'RAIDBOTS',
      lastSimulatedAt: '2026-01-14T10:00:00Z',
      isStale: false,
    },
  ]

  const mockWishlistResponse = {
    raiderId: 1,
    characterName: 'TestRaider',
    items: mockWishlistItems,
    lastSimulatedAt: '2026-01-14T10:00:00Z',
  }

  const mockSimulationStatus = {
    raiderId: 1,
    status: 'COMPLETED' as const,
    progress: 1,
    lastRunAt: '2026-01-14T10:00:00Z',
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(WishlistPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(wishlistApi.getMyWishlist).mockResolvedValue(mockWishlistResponse)
    vi.mocked(wishlistApi.getSimulationStatus).mockResolvedValue(mockSimulationStatus)
    vi.mocked(wishlistApi.triggerSimulation).mockResolvedValue({
      ...mockSimulationStatus,
      status: 'QUEUED',
    })
  })

  it('should render page title', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Wishlist')
  })

  it('should display character name from wishlist data', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('TestRaider')
  })

  it('should render wishlist items in a table', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Check all items are rendered
    expect(wrapper.text()).toContain('Void-Touched Blade')
    expect(wrapper.text()).toContain('Crown of Endless Fury')
    expect(wrapper.text()).toContain('Ring of Dark Whispers')
    expect(wrapper.text()).toContain('Boots of the Void')
  })

  it('should display upgrade values with correct formatting', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('+12.5%')
    expect(wrapper.text()).toContain('+7.2%')
    expect(wrapper.text()).toContain('+3.1%')
    expect(wrapper.text()).toContain('+1.5%')
  })

  it('should display slot information', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Main Hand')
    expect(wrapper.text()).toContain('Head')
    expect(wrapper.text()).toContain('Finger')
    expect(wrapper.text()).toContain('Feet')
  })

  it('should show simulation status', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Simulation Status')
    expect(wrapper.text()).toContain('Up to date')
  })

  it('should have run simulation button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const button = wrapper.find('button')
    expect(button.text()).toContain('Run Simulation')
  })

  it('should show stale data warning when items are stale', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Stale Data Warning')
    expect(wrapper.text()).toContain('1 item(s) have outdated simulation data')
  })

  it('should not show stale warning when no stale items', async () => {
    vi.mocked(wishlistApi.getMyWishlist).mockResolvedValue({
      ...mockWishlistResponse,
      items: mockWishlistItems.filter((item) => !item.isStale),
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).not.toContain('Stale Data Warning')
  })

  it('should have slot filter dropdown', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const select = wrapper.find('select')
    expect(select.exists()).toBe(true)
    expect(wrapper.text()).toContain('All Slots')
  })

  it('should filter items by slot', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const select = wrapper.find('select')
    await select.setValue('Head')
    await flushPromises()

    // Should only show Head item
    expect(wrapper.text()).toContain('Crown of Endless Fury')
    expect(wrapper.text()).not.toContain('Void-Touched Blade')
  })

  it('should have sortable columns', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const headers = wrapper.findAll('th')
    expect(headers.length).toBeGreaterThan(0)

    // Item column should be sortable
    const itemHeader = headers.find((h) => h.text().includes('Item'))
    expect(itemHeader?.classes()).toBeDefined()
  })

  it('should sort by upgrade value by default (descending)', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const rows = wrapper.findAll('tbody tr')
    const upgradeValues = rows.map((row) => {
      const text = row.text()
      const match = text.match(/\+(\d+\.?\d*)%/)
      return match ? parseFloat(match[1]) : 0
    })

    // Should be sorted descending by upgrade value
    for (let i = 1; i < upgradeValues.length; i++) {
      expect(upgradeValues[i - 1]).toBeGreaterThanOrEqual(upgradeValues[i])
    }
  })

  it('should display source badge (Raidbots or Manual)', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Raidbots')
    expect(wrapper.text()).toContain('Manual')
  })

  it('should show item status (Current or Stale)', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Current')
    expect(wrapper.text()).toContain('Stale')
  })

  it('should display upgrade value legend', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Upgrade Value Legend')
    expect(wrapper.text()).toContain('10%+ (Best in Slot)')
    expect(wrapper.text()).toContain('5-10% (Major Upgrade)')
    expect(wrapper.text()).toContain('2-5% (Minor Upgrade)')
    expect(wrapper.text()).toContain('<2% (Sidegrade)')
  })

  it('should display item count', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('4 items')
  })

  it('should show filtered count when filtering', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const select = wrapper.find('select')
    await select.setValue('Head')
    await flushPromises()

    expect(wrapper.text()).toContain('1 items')
    expect(wrapper.text()).toContain('4 total')
  })

  it('should show loading state initially', () => {
    vi.mocked(wishlistApi.getMyWishlist).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    )

    const wrapper = mountComponent()

    // Should show loading spinner
    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(wishlistApi.getMyWishlist).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load wishlist data')
  })

  it('should show simulation running state', async () => {
    vi.mocked(wishlistApi.getSimulationStatus).mockResolvedValue({
      raiderId: 1,
      status: 'RUNNING',
      progress: 0.65,
      lastRunAt: '2026-01-14T10:00:00Z',
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Simulating...')
    expect(wrapper.text()).toContain('Progress')
    expect(wrapper.text()).toContain('65%')
  })

  it('should show simulation queued state', async () => {
    vi.mocked(wishlistApi.getSimulationStatus).mockResolvedValue({
      raiderId: 1,
      status: 'QUEUED',
      lastRunAt: '2026-01-14T10:00:00Z',
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Queued')
  })

  it('should show simulation failed state with error', async () => {
    vi.mocked(wishlistApi.getSimulationStatus).mockResolvedValue({
      raiderId: 1,
      status: 'FAILED',
      lastRunAt: '2026-01-14T10:00:00Z',
      error: 'Simulation timed out',
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed')
    expect(wrapper.text()).toContain('Simulation timed out')
  })

  it('should disable run simulation button when already running', async () => {
    vi.mocked(wishlistApi.getSimulationStatus).mockResolvedValue({
      raiderId: 1,
      status: 'RUNNING',
      progress: 0.5,
    })

    const wrapper = mountComponent()
    await flushPromises()

    const button = wrapper.find('button.btn-primary')
    expect(button.attributes('disabled')).toBeDefined()
  })

  it('should apply correct color classes for upgrade values', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // 12.5% should be purple (best in slot)
    expect(wrapper.html()).toContain('text-purple-400')
    // 7.2% should be blue (major upgrade)
    expect(wrapper.html()).toContain('text-blue-400')
    // 3.1% should be green (minor upgrade)
    expect(wrapper.html()).toContain('text-green-400')
    // 1.5% should be gray (sidegrade)
    expect(wrapper.html()).toContain('text-gray-400')
  })

  it('should show empty state when no items', async () => {
    vi.mocked(wishlistApi.getMyWishlist).mockResolvedValue({
      ...mockWishlistResponse,
      items: [],
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No items in wishlist')
  })
})
