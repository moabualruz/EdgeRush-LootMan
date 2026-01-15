import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import SyncHistoryPage from './SyncHistoryPage.vue'
import { syncApi, type SyncRun, type PagedSyncRunResponse } from '@/api/sync'

// Mock the APIs
vi.mock('@/api/sync', () => ({
  syncApi: {
    getSyncRuns: vi.fn(),
    getSyncRunsBySource: vi.fn(),
    getSyncRunsByStatus: vi.fn(),
  },
}))

describe('SyncHistoryPage', () => {
  const mockSyncRuns: SyncRun[] = [
    {
      id: 1,
      source: 'WoWAudit',
      status: 'COMPLETED',
      startedAt: '2026-01-15T10:00:00Z',
      completedAt: '2026-01-15T10:02:30Z',
      message: 'Synced 150 characters and 500 loot awards',
    },
    {
      id: 2,
      source: 'WarcraftLogs',
      status: 'RUNNING',
      startedAt: '2026-01-15T10:30:00Z',
      completedAt: null,
      message: 'Processing performance data...',
    },
    {
      id: 3,
      source: 'WoWAudit',
      status: 'FAILED',
      startedAt: '2026-01-14T22:00:00Z',
      completedAt: '2026-01-14T22:00:05Z',
      message: 'Connection timeout',
    },
  ]

  const mockPagedResponse: PagedSyncRunResponse = {
    content: mockSyncRuns,
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

    return mount(SyncHistoryPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(syncApi.getSyncRuns).mockResolvedValue(mockPagedResponse)
    vi.mocked(syncApi.getSyncRunsBySource).mockResolvedValue(mockPagedResponse)
    vi.mocked(syncApi.getSyncRunsByStatus).mockResolvedValue(mockPagedResponse)
  })

  it('should render page title "Sync History"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Sync History')
  })

  it('should display subtitle description', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('View data synchronization history from external sources')
  })

  it('should display sync runs list', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('WoWAudit')
    expect(wrapper.text()).toContain('WarcraftLogs')
  })

  it('should display status badges for each sync run', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('COMPLETED')
    expect(wrapper.text()).toContain('RUNNING')
    expect(wrapper.text()).toContain('FAILED')
  })

  it('should display message for each sync run', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Synced 150 characters and 500 loot awards')
    expect(wrapper.text()).toContain('Processing performance data...')
    expect(wrapper.text()).toContain('Connection timeout')
  })

  it('should show source icons (WA, WL)', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('WA')
    expect(wrapper.text()).toContain('WL')
  })

  it('should display source filter buttons', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Source:')
    expect(wrapper.text()).toContain('All')
    const buttons = wrapper.findAll('button')
    const wowauditButton = buttons.find((b) => b.text() === 'WoWAudit')
    const warcraftlogsButton = buttons.find((b) => b.text() === 'WarcraftLogs')
    expect(wowauditButton).toBeDefined()
    expect(warcraftlogsButton).toBeDefined()
  })

  it('should display status filter buttons', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Status:')
    const buttons = wrapper.findAll('button')
    const runningButton = buttons.find((b) => b.text() === 'RUNNING')
    const completedButton = buttons.find((b) => b.text() === 'COMPLETED')
    const failedButton = buttons.find((b) => b.text() === 'FAILED')
    expect(runningButton).toBeDefined()
    expect(completedButton).toBeDefined()
    expect(failedButton).toBeDefined()
  })

  it('should filter by source when clicking source button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const buttons = wrapper.findAll('button')
    const wowauditButton = buttons.find((b) => b.text() === 'WoWAudit')
    await wowauditButton?.trigger('click')
    await flushPromises()

    expect(syncApi.getSyncRunsBySource).toHaveBeenCalledWith('WoWAudit', 0, 20)
  })

  it('should filter by status when clicking status button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const buttons = wrapper.findAll('button')
    const failedButton = buttons.find((b) => b.text() === 'FAILED')
    await failedButton?.trigger('click')
    await flushPromises()

    expect(syncApi.getSyncRunsByStatus).toHaveBeenCalledWith('FAILED', 0, 20)
  })

  it('should show Refresh button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Refresh')
  })

  it('should show total count of sync runs', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Showing 3 of 3 sync runs')
  })

  it('should show loading state', () => {
    vi.mocked(syncApi.getSyncRuns).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(syncApi.getSyncRuns).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load sync history')
  })

  it('should show empty state when no sync runs', async () => {
    vi.mocked(syncApi.getSyncRuns).mockResolvedValue({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No sync runs found')
  })

  it('should display started timestamp', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Started:')
  })

  it('should display duration for completed runs', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Duration:')
  })

  it('should show "In progress..." for running syncs', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('In progress...')
  })

  it('should use green color for COMPLETED status', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const completedBadge = wrapper.find('.bg-green-600')
    expect(completedBadge.exists()).toBe(true)
  })

  it('should use blue color for RUNNING status', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const runningBadge = wrapper.find('.bg-blue-600')
    expect(runningBadge.exists()).toBe(true)
  })

  it('should use red color for FAILED status', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const failedBadge = wrapper.find('.bg-red-600')
    expect(failedBadge.exists()).toBe(true)
  })

  it('should show pagination when multiple pages exist', async () => {
    vi.mocked(syncApi.getSyncRuns).mockResolvedValue({
      content: mockSyncRuns,
      page: 0,
      size: 20,
      totalElements: 100,
      totalPages: 5,
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Previous')
    expect(wrapper.text()).toContain('Next')
  })

  it('should not show pagination when only one page', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // With only 1 page (totalPages: 1), pagination should not be shown
    const buttons = wrapper.findAll('button')
    const prevButton = buttons.find((b) => b.text() === 'Previous')
    expect(prevButton).toBeUndefined()
  })
})
