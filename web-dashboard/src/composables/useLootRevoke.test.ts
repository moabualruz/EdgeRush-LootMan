import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useLootRevoke } from './useLootRevoke'
import { lootApi } from '@/api/loot'
import { useToast } from '@/composables/useToast'

vi.mock('@/api/loot', () => ({
  lootApi: {
    revokeLoot: vi.fn(),
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
    mutate: vi.fn(async (data) => {
      try {
        const result = await mutationFn(data)
        onSuccess?.(result)
        return result
      } catch (e) {
        onError?.(e)
        throw e
      }
    }),
    mutateAsync: vi.fn(async (data) => {
      try {
        const result = await mutationFn(data)
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
    cancelQueries: vi.fn(),
    getQueryData: vi.fn(),
    setQueryData: vi.fn(),
    invalidateQueries: vi.fn(),
  })),
}))

describe('useLootRevoke', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call lootApi.revokeLoot with correct award ID', async () => {
    vi.mocked(lootApi.revokeLoot).mockResolvedValue(undefined)

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootRevoke(guildId)

    await mutateAsync(100)

    expect(lootApi.revokeLoot).toHaveBeenCalledWith(100)
  })

  it('should show success toast on successful revoke', async () => {
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

    vi.mocked(lootApi.revokeLoot).mockResolvedValue(undefined)

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootRevoke(guildId)

    await mutateAsync(100)

    expect(mockSuccess).toHaveBeenCalledWith('Loot Revoked', expect.any(String))
  })

  it('should show error toast on failed revoke', async () => {
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

    vi.mocked(lootApi.revokeLoot).mockRejectedValue(new Error('Failed'))

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootRevoke(guildId)

    await expect(mutateAsync(100)).rejects.toThrow()

    expect(mockError).toHaveBeenCalledWith('Revoke Failed', expect.any(String))
  })
})
