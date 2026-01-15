import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import RaidsPage from './RaidsPage.vue'
import { raidsApi, type Raid } from '@/api/raids'

// Mock the APIs
vi.mock('@/api/raids', () => ({
  raidsApi: {
    getUpcomingRaids: vi.fn(),
    getPastRaids: vi.fn(),
  },
}))

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
}))

describe('RaidsPage', () => {
  const mockUpcomingRaids: Raid[] = [
    {
      id: 1,
      teamId: 1,
      teamName: 'Mythic Team',
      instanceName: 'Nerub-ar Palace',
      difficulty: 'MYTHIC',
      scheduledAt: '2026-01-20T20:00:00Z',
      status: 'SCHEDULED',
      signupCount: 18,
      maxPlayers: 20,
      description: 'Week 3 prog',
    },
    {
      id: 2,
      teamId: 1,
      teamName: 'Mythic Team',
      instanceName: 'Nerub-ar Palace',
      difficulty: 'HEROIC',
      scheduledAt: '2026-01-18T20:00:00Z',
      status: 'SCHEDULED',
      signupCount: 25,
      maxPlayers: 30,
    },
  ]

  const mockPastRaids: Raid[] = [
    {
      id: 3,
      teamId: 1,
      teamName: 'Mythic Team',
      instanceName: 'Nerub-ar Palace',
      difficulty: 'MYTHIC',
      scheduledAt: '2026-01-13T20:00:00Z',
      endedAt: '2026-01-13T23:30:00Z',
      status: 'COMPLETED',
      signupCount: 20,
      maxPlayers: 20,
    },
    {
      id: 4,
      teamId: 1,
      teamName: 'Mythic Team',
      instanceName: 'Nerub-ar Palace',
      difficulty: 'NORMAL',
      scheduledAt: '2026-01-11T20:00:00Z',
      endedAt: '2026-01-11T21:30:00Z',
      status: 'CANCELLED',
      signupCount: 10,
      maxPlayers: 30,
    },
  ]

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(RaidsPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          RaidCalendar: true,
          SkeletonCard: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    mockPush.mockClear()
    vi.mocked(raidsApi.getUpcomingRaids).mockResolvedValue(mockUpcomingRaids)
    vi.mocked(raidsApi.getPastRaids).mockResolvedValue(mockPastRaids)
  })

  it('should render page title "Raids"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Raids')
  })

  it('should toggle between list and calendar views', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Default is list view
    expect(wrapper.findComponent({ name: 'RaidCalendar' }).exists()).toBe(false)

    // Click calendar view button
    const calendarButton = wrapper.find('button[title="Calendar View"]')
    await calendarButton.trigger('click')

    // Now calendar should be visible
    expect(wrapper.findComponent({ name: 'RaidCalendar' }).exists()).toBe(true)
  })

  it('should display upcoming raids tab by default', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Check that upcoming raids are displayed
    expect(wrapper.text()).toContain('Nerub-ar Palace')
    expect(wrapper.text()).toContain('MYTHIC')
    expect(wrapper.text()).toContain('Week 3 prog')
  })

  it('should display past raids tab when clicked', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Click past raids tab
    const pastTab = wrapper.findAll('button').find((b) => b.text() === 'Past Raids')
    await pastTab?.trigger('click')
    await flushPromises()

    // Check that past raids are shown (will have COMPLETED status label)
    expect(wrapper.text()).toContain('Completed')
  })

  it('should show raid difficulty colors - Mythic is purple', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Find difficulty text with Mythic
    expect(wrapper.html()).toContain('text-purple-400')
  })

  it('should show raid difficulty colors - Heroic is orange', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.html()).toContain('text-orange-400')
  })

  it('should show raid difficulty colors - Normal is green', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Switch to past raids to see Normal difficulty
    const pastTab = wrapper.findAll('button').find((b) => b.text() === 'Past Raids')
    await pastTab?.trigger('click')
    await flushPromises()

    expect(wrapper.html()).toContain('text-green-400')
  })

  it('should show signup counts with progress bar', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Check for signup count display
    expect(wrapper.text()).toContain('18/20')
    expect(wrapper.text()).toContain('25/30')
    expect(wrapper.text()).toContain('signups')
  })

  it('should navigate to raid detail on click', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Find a raid card and click it
    const raidCards = wrapper.findAll('.card.cursor-pointer')
    await raidCards[0].trigger('click')

    expect(mockPush).toHaveBeenCalledWith('/raids/1')
  })

  it('should show empty state for upcoming raids when none exist', async () => {
    vi.mocked(raidsApi.getUpcomingRaids).mockResolvedValue([])

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No upcoming raids scheduled')
  })

  it('should show empty state for past raids when none exist', async () => {
    vi.mocked(raidsApi.getPastRaids).mockResolvedValue([])

    const wrapper = mountComponent()
    await flushPromises()

    // Switch to past raids tab
    const pastTab = wrapper.findAll('button').find((b) => b.text() === 'Past Raids')
    await pastTab?.trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('No past raids found')
  })

  it('should display RaidCalendar in calendar view', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Click calendar view button
    const calendarButton = wrapper.find('button[title="Calendar View"]')
    await calendarButton.trigger('click')

    expect(wrapper.findComponent({ name: 'RaidCalendar' }).exists()).toBe(true)
  })

  it('should show loading skeleton when data is loading', () => {
    // Make API never resolve to keep loading state
    vi.mocked(raidsApi.getUpcomingRaids).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.findComponent({ name: 'SkeletonCard' }).exists()).toBe(true)
  })
})
