import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useSyncTrigger } from './useSyncTrigger'
import { syncApi } from '@/api/sync'
import { useToast } from '@/composables/useToast'

vi.mock('@/api/sync', () => ({
  syncApi: {
    triggerSync: vi.fn(),
  },
}))

vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn(() => ({
    success: vi.fn(),
    error: vi.fn(),
  })),
}))

vi.mock('@tanstack/vue-query', () => ({
  useMutation: vi.fn(({ mutationFn, onSuccess, onError }) => ({
    mutate: vi.fn(async () => {
      try {
        const result = await mutationFn()
        onSuccess?.(result)
        return result
      } catch (e) {
        onError?.(e)
        throw e
      }
    }),
    mutateAsync: vi.fn(async () => {
      try {
        const result = await mutationFn()
        onSuccess?.(result)
        return result
      } catch (e) {
        onError?.(e)
        throw e
      }
    }),
    isPending: ref(false),
    isError: ref(false),
    error: ref(null),
  })),
  useQueryClient: vi.fn(() => ({
    invalidateQueries: vi.fn(),
  })),
}))

describe('useSyncTrigger', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call syncApi.triggerSync with correct source', async () => {
    const mockSyncRun = {
      id: 1,
      source: 'WoWAudit',
      status: 'RUNNING',
      startedAt: '2026-02-05T10:00:00Z',
      completedAt: null,
      message: null,
    }
    vi.mocked(syncApi.triggerSync).mockResolvedValue(mockSyncRun)

    const { mutate } = useSyncTrigger('WoWAudit')
    await mutate()

    expect(syncApi.triggerSync).toHaveBeenCalledWith('WoWAudit')
  })

  it('should show success toast on successful sync trigger', async () => {
    const mockSuccess = vi.fn()
    vi.mocked(useToast).mockReturnValue({
      success: mockSuccess,
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn(),
      show: vi.fn(),
      dismiss: vi.fn(),
      dismissAll: vi.fn(),
      toasts: ref([]),
    })

    const mockSyncRun = {
      id: 1,
      source: 'WarcraftLogs',
      status: 'RUNNING',
      startedAt: '2026-02-05T10:00:00Z',
      completedAt: null,
      message: null,
    }
    vi.mocked(syncApi.triggerSync).mockResolvedValue(mockSyncRun)

    const { mutate } = useSyncTrigger('WarcraftLogs')
    await mutate()

    expect(mockSuccess).toHaveBeenCalledWith('Sync Started', expect.any(String))
  })

  it('should show error toast on failed sync trigger', async () => {
    const mockError = vi.fn()
    vi.mocked(useToast).mockReturnValue({
      success: vi.fn(),
      error: mockError,
      warning: vi.fn(),
      info: vi.fn(),
      show: vi.fn(),
      dismiss: vi.fn(),
      dismissAll: vi.fn(),
      toasts: ref([]),
    })

    vi.mocked(syncApi.triggerSync).mockRejectedValue(new Error('Sync failed'))

    const { mutate } = useSyncTrigger('WoWAudit')

    await expect(mutate()).rejects.toThrow()

    expect(mockError).toHaveBeenCalledWith('Sync Failed', expect.any(String))
  })
})
