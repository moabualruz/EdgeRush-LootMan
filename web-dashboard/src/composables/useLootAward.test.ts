import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useLootAward } from './useLootAward'
import { lootApi } from '@/api/loot'
import { useToast } from '@/composables/useToast'

vi.mock('@/api/loot', () => ({
  lootApi: {
    awardLoot: vi.fn(),
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

describe('useLootAward', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call lootApi.awardLoot with correct parameters', async () => {
    const mockAward = {
      id: 1,
      raiderId: 10,
      itemId: 12345,
      itemName: 'Test Item',
      characterName: 'TestRaider',
      awardedAt: '2026-02-05T10:00:00Z',
      flpsAtAward: 85.5,
      rdfExpired: false,
    }
    vi.mocked(lootApi.awardLoot).mockResolvedValue(mockAward)

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootAward(guildId)

    const request = {
      raiderId: 10,
      itemId: 12345,
      itemName: 'Test Item',
    }

    await mutateAsync(request)

    expect(lootApi.awardLoot).toHaveBeenCalledWith('test-guild', request)
  })

  it('should show success toast on successful award', async () => {
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

    const mockAward = {
      id: 1,
      raiderId: 10,
      itemId: 12345,
      itemName: 'Test Item',
      characterName: 'TestRaider',
      awardedAt: '2026-02-05T10:00:00Z',
      flpsAtAward: 85.5,
      rdfExpired: false,
    }
    vi.mocked(lootApi.awardLoot).mockResolvedValue(mockAward)

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootAward(guildId)

    await mutateAsync({ raiderId: 10, itemId: 12345, itemName: 'Test Item' })

    expect(mockSuccess).toHaveBeenCalledWith('Loot Awarded', expect.any(String))
  })

  it('should show error toast on failed award', async () => {
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

    vi.mocked(lootApi.awardLoot).mockRejectedValue(new Error('Failed'))

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootAward(guildId)

    await expect(
      mutateAsync({ raiderId: 10, itemId: 12345, itemName: 'Test Item' })
    ).rejects.toThrow()

    expect(mockError).toHaveBeenCalledWith('Award Failed', expect.any(String))
  })
})
