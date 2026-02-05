import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'
import SyncLogViewer from './SyncLogViewer.vue'
import { syncApi } from '@/api/sync'

vi.mock('@/api/sync', () => ({
  syncApi: {
    getSyncLogs: vi.fn(),
  },
}))

vi.mock('@tanstack/vue-query', () => ({
  useQuery: vi.fn(({ queryFn, enabled }) => {
    const data = ref<unknown>(null)
    const isLoading = ref(false)
    const error = ref<Error | null>(null)

    if (enabled?.value) {
      isLoading.value = true
      queryFn().then((result: unknown) => {
        data.value = result
        isLoading.value = false
      }).catch((e: Error) => {
        error.value = e
        isLoading.value = false
      })
    }

    return { data, isLoading, error }
  }),
}))

describe('SyncLogViewer', () => {
  const mockLogs = [
    { timestamp: '2026-02-05T10:00:00Z', level: 'INFO' as const, message: 'Sync started' },
    { timestamp: '2026-02-05T10:00:05Z', level: 'WARN' as const, message: 'Rate limited' },
    { timestamp: '2026-02-05T10:00:10Z', level: 'ERROR' as const, message: 'Connection failed' },
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(syncApi.getSyncLogs).mockResolvedValue(mockLogs)
  })

  it('should render when isOpen is true', () => {
    const wrapper = mount(SyncLogViewer, {
      props: {
        isOpen: true,
        syncRunId: 1,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    expect(wrapper.text()).toContain('Sync Logs')
  })

  it('should not render when isOpen is false', () => {
    const wrapper = mount(SyncLogViewer, {
      props: {
        isOpen: false,
        syncRunId: 1,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    expect(wrapper.text()).not.toContain('Sync Logs')
  })

  it('should emit close when backdrop is clicked', async () => {
    const wrapper = mount(SyncLogViewer, {
      props: {
        isOpen: true,
        syncRunId: 1,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const backdrop = wrapper.find('[data-testid="backdrop"]')
    await backdrop.trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('should display log entries with correct level colors', async () => {
    vi.mocked(syncApi.getSyncLogs).mockResolvedValue(mockLogs)

    const wrapper = mount(SyncLogViewer, {
      props: {
        isOpen: true,
        syncRunId: 1,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('Sync started')
    expect(wrapper.text()).toContain('Rate limited')
    expect(wrapper.text()).toContain('Connection failed')
  })

  it('should call syncApi.getSyncLogs with the syncRunId', async () => {
    mount(SyncLogViewer, {
      props: {
        isOpen: true,
        syncRunId: 42,
      },
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await flushPromises()

    expect(syncApi.getSyncLogs).toHaveBeenCalledWith(42)
  })
})
