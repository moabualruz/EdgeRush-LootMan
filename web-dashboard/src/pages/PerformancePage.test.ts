import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import PerformancePage from './PerformancePage.vue'
import { performanceApi, type WarcraftLogsReport } from '@/api/performance'
import { flpsApi } from '@/api/flps'
import type { PerformanceMetrics, FlpsScore } from '@/types'
import { useGuildContextStore } from '@/stores/guildContext'
import { useAuthStore } from '@/stores/auth'

// Mock the APIs
vi.mock('@/api/performance', () => ({
  performanceApi: {
    getMyPerformance: vi.fn(),
    getWarcraftLogsReports: vi.fn(),
  },
}))

vi.mock('@/api/flps', () => ({
  flpsApi: {
    getMyFlps: vi.fn(),
  },
}))

describe('PerformancePage', () => {
  const mockPerformanceData: PerformanceMetrics = {
    raiderId: 1,
    characterName: 'TestRaider',
    dpa: 45000,
    adt: 0.92,
    specAverage: 42000,
    performanceTrend: [
      { date: '2026-01-01', dpa: 43000, adt: 0.90 },
      { date: '2026-01-08', dpa: 44500, adt: 0.91 },
      { date: '2026-01-15', dpa: 45000, adt: 0.92 },
    ],
    lastUpdated: '2026-01-15T12:00:00Z',
  }

  const mockFlpsData: FlpsScore = {
    raiderId: 1,
    characterName: 'TestRaider',
    characterClass: 'WARRIOR',
    role: 'DPS',
    flps: 0.85,
    rms: { value: 0.88, acs: 0.95, mas: 0.85, eps: 0.82 },
    ipi: { value: 0.8, uv: 0.75, tierBonus: 0.1, roleMultiplier: 1.0 },
    rdf: 0.95,
    eligible: true,
    rank: 5,
  }

  const mockWclReports: WarcraftLogsReport = {
    raiderId: 1,
    characterName: 'TestRaider',
    reports: [
      {
        reportId: 'abc123',
        encounterId: 1,
        encounterName: 'Ulgrax the Devourer',
        difficulty: 'Mythic',
        date: '2026-01-14T20:00:00Z',
        dps: 52000,
        ilvl: 630,
        spec: 'Arms',
        percentile: 85,
        deaths: 0,
      },
      {
        reportId: 'def456',
        encounterId: 2,
        encounterName: 'The Bloodbound Horror',
        difficulty: 'Mythic',
        date: '2026-01-14T21:00:00Z',
        dps: 48000,
        ilvl: 630,
        spec: 'Arms',
        percentile: 72,
        deaths: 1,
      },
    ],
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(PerformancePage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          SkeletonCard: true,
          SkeletonTable: true,
          LineChart: true,
          ProgressBar: true,
          DonutChart: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(performanceApi.getMyPerformance).mockResolvedValue(mockPerformanceData)
    vi.mocked(flpsApi.getMyFlps).mockResolvedValue(mockFlpsData)
    vi.mocked(performanceApi.getWarcraftLogsReports).mockResolvedValue(mockWclReports)

    // Set up store state
    const guildStore = useGuildContextStore()
    guildStore.activeGuild = {
      guildId: 'test-guild',
      guildName: 'Test Guild',
      characterName: 'TestChar',
      characterRealm: 'TestRealm',
      characterClass: 'WARRIOR',
      characterMappingId: 1,
      raiderId: 1,
      rank: 'Member',
      permissions: [],
      isActive: true,
    }

    const authStore = useAuthStore()
    authStore.user = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      guildId: 'test-guild',
      role: 'RAIDER',
      linkedCharacters: [],
    }
  })

  it('should render page title "Performance Metrics"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Performance Metrics')
  })

  it('should display character name', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('TestRaider')
  })

  it('should show MAS score with percentage', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // MAS is 0.85 = 85%
    expect(wrapper.text()).toContain('85%')
    expect(wrapper.text()).toContain('Overall MAS')
  })

  it('should display RMS breakdown with ACS, MAS, and EPS', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // ACS: 0.95 = 95%
    expect(wrapper.text()).toContain('95%')
    expect(wrapper.text()).toContain('ACS (Attendance)')

    // MAS: 0.85 = 85%
    expect(wrapper.text()).toContain('MAS (Mechanical)')

    // EPS: 0.82 = 82%
    expect(wrapper.text()).toContain('82%')
    expect(wrapper.text()).toContain('EPS (Equipment)')
  })

  it('should show Combined RMS value', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Combined RMS: 0.88 = 88.0%
    expect(wrapper.text()).toContain('88.0%')
    expect(wrapper.text()).toContain('Combined RMS')
  })

  it('should display DPA and ADT metrics via ProgressBar component', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // ProgressBar components should be rendered (stubbed)
    const progressBars = wrapper.findAllComponents({ name: 'ProgressBar' })
    expect(progressBars.length).toBeGreaterThan(0)

    // Spec average info is displayed
    expect(wrapper.text()).toContain('Spec average: 42.0K')
  })

  it('should handle loading state with skeletons', () => {
    // Make APIs never resolve to keep loading state
    vi.mocked(performanceApi.getMyPerformance).mockImplementation(() => new Promise(() => {}))
    vi.mocked(flpsApi.getMyFlps).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.findComponent({ name: 'SkeletonCard' }).exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(performanceApi.getMyPerformance).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load performance data')
  })

  it('should display Warcraft Logs reports table', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Recent Warcraft Logs Reports')
    expect(wrapper.text()).toContain('Ulgrax the Devourer')
    expect(wrapper.text()).toContain('The Bloodbound Horror')
    expect(wrapper.text()).toContain('Mythic')
  })

  it('should show percentile colors based on value', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // 85 percentile should be epic (purple)
    expect(wrapper.html()).toContain('text-purple-400')

    // 72 percentile should be blue
    expect(wrapper.html()).toContain('text-blue-400')
  })

  it('should show deaths with color coding', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // 0 deaths should be green
    expect(wrapper.html()).toContain('text-green-400')

    // 1 death should be red
    expect(wrapper.html()).toContain('text-red-400')
  })

  it('should display percentile legend', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Percentile Legend')
    expect(wrapper.text()).toContain('95%+ (Legendary)')
    expect(wrapper.text()).toContain('75-94% (Epic)')
    expect(wrapper.text()).toContain('50-74% (Rare)')
    expect(wrapper.text()).toContain('25-49% (Uncommon)')
    expect(wrapper.text()).toContain('<25% (Common)')
  })

  it('should show no Warcraft Logs message when empty', async () => {
    vi.mocked(performanceApi.getWarcraftLogsReports).mockResolvedValue({
      raiderId: 1,
      characterName: 'TestRaider',
      reports: [],
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No Warcraft Logs reports found')
  })
})
