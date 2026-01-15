import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import ApplicationsPage from './ApplicationsPage.vue'
import {
  applicationsApi,
  type Application,
  type PagedResponse,
} from '@/api/applications'

// Mock the APIs
vi.mock('@/api/applications', () => ({
  applicationsApi: {
    getApplications: vi.fn(),
    getApplicationsByStatus: vi.fn(),
    approveApplication: vi.fn(),
    declineApplication: vi.fn(),
    requestInfo: vi.fn(),
    addNote: vi.fn(),
    getNotes: vi.fn(),
    deleteApplication: vi.fn(),
  },
}))

// Mock composables
vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
  }),
}))

describe('ApplicationsPage', () => {
  const mockApplications: Application[] = [
    {
      applicationId: 1,
      appliedAt: '2026-01-10T10:00:00Z',
      status: 'pending',
      role: 'DPS',
      age: 25,
      country: 'USA',
      battletag: 'Player#1234',
      discordId: 'player123',
      mainCharacterName: 'TestPlayer',
      mainCharacterRealm: 'Illidan',
      mainCharacterClass: 'Warrior',
      mainCharacterRole: 'DPS',
      mainCharacterRace: 'Human',
      mainCharacterFaction: 'Alliance',
      mainCharacterLevel: 80,
      mainCharacterRegion: 'US',
      syncedAt: '2026-01-10T12:00:00Z',
      performanceData: {
        itemLevel: 630,
        mythicPlusScore: 2800,
        averageParse: 85,
        bestParse: 95,
        deathsPerPull: 0.5,
      },
    },
    {
      applicationId: 2,
      appliedAt: '2026-01-08T14:00:00Z',
      status: 'approved',
      role: 'Healer',
      age: 30,
      country: 'UK',
      battletag: 'Healer#5678',
      discordId: 'healer456',
      mainCharacterName: 'HolyPriest',
      mainCharacterRealm: 'Stormrage',
      mainCharacterClass: 'Priest',
      mainCharacterRole: 'Healer',
      mainCharacterRace: 'Night Elf',
      mainCharacterFaction: 'Alliance',
      mainCharacterLevel: 80,
      mainCharacterRegion: 'EU',
      syncedAt: '2026-01-08T16:00:00Z',
    },
    {
      applicationId: 3,
      appliedAt: '2026-01-05T09:00:00Z',
      status: 'rejected',
      role: 'Tank',
      age: 22,
      country: 'Canada',
      battletag: 'Tank#9999',
      discordId: 'tank789',
      mainCharacterName: 'BigShield',
      mainCharacterRealm: 'Sargeras',
      mainCharacterClass: 'Paladin',
      mainCharacterRole: 'Tank',
      mainCharacterRace: 'Dwarf',
      mainCharacterFaction: 'Alliance',
      mainCharacterLevel: 80,
      mainCharacterRegion: 'US',
      syncedAt: '2026-01-05T11:00:00Z',
    },
  ]

  const mockPagedResponse: PagedResponse<Application> = {
    content: mockApplications,
    page: 0,
    size: 20,
    totalElements: 3,
    totalPages: 1,
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(ApplicationsPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          SkeletonCard: true,
          SkeletonTable: true,
          ProgressBar: true,
          BarChart: true,
          Teleport: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(applicationsApi.getApplications).mockResolvedValue(mockPagedResponse)
    vi.mocked(applicationsApi.getApplicationsByStatus).mockResolvedValue(mockPagedResponse)
    vi.mocked(applicationsApi.getNotes).mockResolvedValue([])
  })

  it('should render page title "Guild Applications"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Guild Applications')
  })

  it('should display application list with filtering', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('table').exists()).toBe(true)
    expect(wrapper.text()).toContain('TestPlayer')
    expect(wrapper.text()).toContain('HolyPriest')
    expect(wrapper.text()).toContain('BigShield')
  })

  it('should show status filter dropdown', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const select = wrapper.find('select')
    expect(select.exists()).toBe(true)
    expect(wrapper.text()).toContain('All Applications')
    expect(wrapper.text()).toContain('Pending')
    expect(wrapper.text()).toContain('Approved')
  })

  it('should filter by status when selecting pending', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const select = wrapper.find('select')
    await select.setValue('pending')
    await flushPromises()

    expect(applicationsApi.getApplicationsByStatus).toHaveBeenCalledWith('pending', 0, 20)
  })

  it('should display character name and realm', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('TestPlayer')
    expect(wrapper.text()).toContain('Illidan')
  })

  it('should display application status badges', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('pending')
    expect(wrapper.text()).toContain('approved')
    expect(wrapper.text()).toContain('rejected')
  })

  it('should display performance data when available', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('iLvl:')
    expect(wrapper.text()).toContain('630')
    expect(wrapper.text()).toContain('Parse:')
    expect(wrapper.text()).toContain('85%')
  })

  it('should show loading state', () => {
    vi.mocked(applicationsApi.getApplications).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.findComponent({ name: 'SkeletonTable' }).exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(applicationsApi.getApplications).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load applications')
  })

  it('should show empty state when no applications', async () => {
    vi.mocked(applicationsApi.getApplications).mockResolvedValue({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No applications found')
  })

  it('should display refresh button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Refresh')
  })

  it('should display View action button for each application', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const viewButtons = wrapper.findAll('button').filter((b) => b.text() === 'View')
    expect(viewButtons.length).toBeGreaterThan(0)
  })

  it('should display Approve button for pending applications', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Approve')
  })

  it('should display class/role column', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Warrior')
    expect(wrapper.text()).toContain('Priest')
    expect(wrapper.text()).toContain('Paladin')
  })
})
