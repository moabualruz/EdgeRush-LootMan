import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import RaidDetailPage from './RaidDetailPage.vue'
import { raidsApi, type RaidDetail, type RaidEncounter, type RaidSignup } from '@/api/raids'
import { flpsApi } from '@/api/flps'
import type { FlpsScore } from '@/types'

// Mock the APIs
vi.mock('@/api/raids', () => ({
  raidsApi: {
    getRaidById: vi.fn(),
    createSignup: vi.fn(),
    updateSignup: vi.fn(),
    deleteSignup: vi.fn(),
  },
}))

vi.mock('@/api/flps', () => ({
  flpsApi: {
    getMyFlps: vi.fn(),
  },
}))

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: () => ({
    params: { id: '1' },
  }),
  useRouter: () => ({
    push: mockPush,
  }),
}))

describe('RaidDetailPage', () => {
  const mockEncounters: RaidEncounter[] = [
    {
      id: 1,
      raidId: 1,
      encounterId: 101,
      encounterName: 'Ulgrax the Devourer',
      status: 'KILLED',
      pullCount: 5,
      killedAt: '2026-01-13T21:00:00Z',
      duration: 320,
    },
    {
      id: 2,
      raidId: 1,
      encounterId: 102,
      encounterName: 'The Bloodbound Horror',
      status: 'IN_PROGRESS',
      pullCount: 12,
    },
    {
      id: 3,
      raidId: 1,
      encounterId: 103,
      encounterName: 'Sikran',
      status: 'NOT_STARTED',
      pullCount: 0,
    },
  ]

  const mockSignups: RaidSignup[] = [
    { id: 1, raidId: 1, raiderId: 1, characterName: 'TankOne', role: 'TANK', status: 'CONFIRMED', signedUpAt: '2026-01-12T10:00:00Z' },
    { id: 2, raidId: 1, raiderId: 2, characterName: 'TankTwo', role: 'TANK', status: 'CONFIRMED', signedUpAt: '2026-01-12T10:05:00Z' },
    { id: 3, raidId: 1, raiderId: 3, characterName: 'HealerOne', role: 'HEALER', status: 'CONFIRMED', signedUpAt: '2026-01-12T10:10:00Z' },
    { id: 4, raidId: 1, raiderId: 4, characterName: 'HealerTwo', role: 'HEALER', status: 'TENTATIVE', signedUpAt: '2026-01-12T10:15:00Z' },
    { id: 5, raidId: 1, raiderId: 5, characterName: 'DpsOne', role: 'DPS', status: 'CONFIRMED', signedUpAt: '2026-01-12T10:20:00Z' },
    { id: 6, raidId: 1, raiderId: 6, characterName: 'DpsTwo', role: 'DPS', status: 'STANDBY', signedUpAt: '2026-01-12T10:25:00Z' },
    { id: 7, raidId: 1, raiderId: 7, characterName: 'DpsThree', role: 'DPS', status: 'DECLINED', signedUpAt: '2026-01-12T10:30:00Z' },
  ]

  const mockRaid: RaidDetail = {
    id: 1,
    teamId: 1,
    teamName: 'Mythic Team',
    instanceName: 'Nerub-ar Palace',
    difficulty: 'MYTHIC',
    scheduledAt: '2026-01-20T20:00:00Z',
    status: 'SCHEDULED',
    signupCount: 5,
    maxPlayers: 20,
    description: 'Week 3 progression',
    encounters: mockEncounters,
    signups: mockSignups,
  }

  const mockFlpsData: FlpsScore = {
    raiderId: 10,
    characterName: 'MyCharacter',
    characterClass: 'WARRIOR',
    role: 'DPS',
    flps: 0.85,
    rms: { value: 0.88, acs: 0.95, mas: 0.85, eps: 0.82 },
    ipi: { value: 0.8, uv: 0.75, tierBonus: 0.1, roleMultiplier: 1.0 },
    rdf: 0.95,
    eligible: true,
    rank: 5,
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(RaidDetailPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockClear()
    vi.mocked(raidsApi.getRaidById).mockResolvedValue(mockRaid)
    vi.mocked(flpsApi.getMyFlps).mockResolvedValue(mockFlpsData)
  })

  it('should render raid title and instance name', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Nerub-ar Palace')
  })

  it('should display raid date and time', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Should show formatted date
    expect(wrapper.text()).toContain('Jan')
    expect(wrapper.text()).toContain('2026')
  })

  it('should show difficulty badge with correct color', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('MYTHIC')
    expect(wrapper.html()).toContain('text-purple-400')
  })

  it('should display encounter list', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Encounters')
    expect(wrapper.text()).toContain('Ulgrax the Devourer')
    expect(wrapper.text()).toContain('The Bloodbound Horror')
    expect(wrapper.text()).toContain('Sikran')
  })

  it('should show encounter status icons', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Killed should show checkmark
    expect(wrapper.text()).toContain('✓')
    // In progress should show spinner icon
    expect(wrapper.text()).toContain('⟳')
    // Not started should show circle
    expect(wrapper.text()).toContain('○')
  })

  it('should display signup roster by role', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Signups')
    expect(wrapper.text()).toContain('Confirmed')
    expect(wrapper.text()).toContain('Tentative')
    expect(wrapper.text()).toContain('Standby')
    expect(wrapper.text()).toContain('Declined')
  })

  it('should display role composition counts', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Confirmed Roster')
    expect(wrapper.text()).toContain('Tanks')
    expect(wrapper.text()).toContain('Healers')
    expect(wrapper.text()).toContain('DPS')
  })

  it('should show loading state', () => {
    // Make API never resolve to keep loading state
    vi.mocked(raidsApi.getRaidById).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(raidsApi.getRaidById).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load raid details')
  })

  it('should show Sign Up button when user is not signed up', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Sign Up')
  })

  it('should show Edit Signup button when user is already signed up', async () => {
    // Set user's raiderId to match one of the signups
    vi.mocked(flpsApi.getMyFlps).mockResolvedValue({
      ...mockFlpsData,
      raiderId: 1, // Matches TankOne signup
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Edit Signup')
    expect(wrapper.text()).toContain('Cancel Signup')
  })

  it('should navigate back to raids list when clicking back button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const backButton = wrapper.findAll('button').find((b) => b.text().includes('Back to Raids'))
    await backButton?.trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/raids')
  })

  it('should display team name', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Mythic Team')
  })

  it('should show empty signups message when no signups', async () => {
    vi.mocked(raidsApi.getRaidById).mockResolvedValue({
      ...mockRaid,
      signups: [],
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No signups yet')
  })
})
