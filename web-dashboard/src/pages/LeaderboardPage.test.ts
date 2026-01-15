import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { createPinia, setActivePinia } from 'pinia'
import LeaderboardPage from './LeaderboardPage.vue'
import { flpsApi } from '@/api/flps'
import { useAuthStore } from '@/stores/auth'
import type { LeaderboardResponse, LeaderboardEntry, User } from '@/types'

// Mock the APIs
vi.mock('@/api/flps', () => ({
  flpsApi: {
    getLeaderboard: vi.fn(),
  },
}))

describe('LeaderboardPage', () => {
  const mockEntries: LeaderboardEntry[] = [
    {
      rank: 1,
      raiderId: 1,
      characterName: 'TopPlayer',
      characterClass: 'WARRIOR',
      role: 'TANK',
      flps: 0.95,
      eligible: true,
    },
    {
      rank: 2,
      raiderId: 2,
      characterName: 'SecondPlace',
      characterClass: 'PRIEST',
      role: 'HEALER',
      flps: 0.88,
      eligible: true,
    },
    {
      rank: 3,
      raiderId: 3,
      characterName: 'BronzeMedal',
      characterClass: 'MAGE',
      role: 'DPS',
      flps: 0.72,
      eligible: true,
    },
    {
      rank: 4,
      raiderId: 4,
      characterName: 'IneligibleRaider',
      characterClass: 'ROGUE',
      role: 'DPS',
      flps: 0.45,
      eligible: false,
    },
  ]

  const mockLeaderboardData: LeaderboardResponse = {
    guildId: 'test-guild',
    entries: mockEntries,
    totalRaiders: 4,
  }

  const mockUser: User = {
    id: 1,
    username: 'TestUser',
    role: 'RAIDER',
    linkedCharacters: [
      { characterName: 'SecondPlace', realm: 'Test-Realm', isPrimary: true },
    ],
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(LeaderboardPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
    vi.mocked(flpsApi.getLeaderboard).mockResolvedValue(mockLeaderboardData)
  })

  it('should render page title "FLPS Leaderboard"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('FLPS Leaderboard')
  })

  it('should display leaderboard table with raiders', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('table').exists()).toBe(true)
    expect(wrapper.text()).toContain('TopPlayer')
    expect(wrapper.text()).toContain('SecondPlace')
    expect(wrapper.text()).toContain('BronzeMedal')
    expect(wrapper.text()).toContain('IneligibleRaider')
  })

  it('should display FLPS scores sorted by default', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Check that API was called with default params (no role filter)
    expect(flpsApi.getLeaderboard).toHaveBeenCalledWith('default', undefined, 50)
  })

  it('should filter by role when selecting Tank', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const select = wrapper.find('select')
    await select.setValue('TANK')
    await flushPromises()

    expect(flpsApi.getLeaderboard).toHaveBeenCalledWith('default', 'TANK', 50)
  })

  it('should filter by role when selecting Healer', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const select = wrapper.find('select')
    await select.setValue('HEALER')
    await flushPromises()

    expect(flpsApi.getLeaderboard).toHaveBeenCalledWith('default', 'HEALER', 50)
  })

  it('should filter by role when selecting DPS', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const select = wrapper.find('select')
    await select.setValue('DPS')
    await flushPromises()

    expect(flpsApi.getLeaderboard).toHaveBeenCalledWith('default', 'DPS', 50)
  })

  it('should highlight current user row', async () => {
    const authStore = useAuthStore()
    authStore.user = mockUser

    const wrapper = mountComponent()
    await flushPromises()

    // Find the row for SecondPlace (current user's linked character)
    const rows = wrapper.findAll('tbody tr')
    const userRow = rows.find((row) => row.text().includes('SecondPlace'))

    expect(userRow?.classes()).toContain('bg-primary-900/30')
    expect(userRow?.text()).toContain('(You)')
  })

  it('should show rank numbers with medals for top 3', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const rows = wrapper.findAll('tbody tr')

    // First place should have gold medal emoji
    expect(rows[0].text()).toContain('🥇')

    // Second place should have silver medal emoji
    expect(rows[1].text()).toContain('🥈')

    // Third place should have bronze medal emoji
    expect(rows[2].text()).toContain('🥉')

    // Fourth place should have # notation
    expect(rows[3].text()).toContain('#4')
  })

  it('should display eligibility status', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Check for eligible indicator (✓) and ineligible indicator (✗)
    expect(wrapper.text()).toContain('✓')
    expect(wrapper.text()).toContain('✗')
  })

  it('should show loading state', () => {
    // Make API never resolve to keep loading state
    vi.mocked(flpsApi.getLeaderboard).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(flpsApi.getLeaderboard).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load leaderboard')
  })

  it('should show empty state when no raiders found', async () => {
    vi.mocked(flpsApi.getLeaderboard).mockResolvedValue({
      guildId: 'test-guild',
      entries: [],
      totalRaiders: 0,
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No raiders found for the selected filter')
  })
})
