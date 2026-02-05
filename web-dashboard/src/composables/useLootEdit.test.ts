import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { useLootEdit } from './useLootEdit'
import { lootApi } from '@/api/loot'
import { useToast } from '@/composables/useToast'

vi.mock('@/api/loot', () => ({
  lootApi: {
    updateLoot: vi.fn(),
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

describe('useLootEdit', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call lootApi.updateLoot with correct parameters', async () => {
    const mockAward = {
      id: 100,
      raiderId: 10,
      itemId: 12345,
      itemName: 'Test Item',
      characterName: 'TestRaider',
      awardedAt: '2026-02-05T10:00:00Z',
      flpsAtAward: 85.5,
      rdfExpired: false,
      notes: 'Updated notes',
    }
    vi.mocked(lootApi.updateLoot).mockResolvedValue(mockAward)

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootEdit(guildId)

    await mutateAsync({ awardId: 100, notes: 'Updated notes' })

    expect(lootApi.updateLoot).toHaveBeenCalledWith(100, { notes: 'Updated notes' })
  })

  it('should show success toast on successful update', async () => {
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
      id: 100,
      raiderId: 10,
      itemId: 12345,
      itemName: 'Test Item',
      characterName: 'TestRaider',
      awardedAt: '2026-02-05T10:00:00Z',
      flpsAtAward: 85.5,
      rdfExpired: false,
      notes: 'Updated',
    }
    vi.mocked(lootApi.updateLoot).mockResolvedValue(mockAward)

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootEdit(guildId)

    await mutateAsync({ awardId: 100, notes: 'Updated' })

    expect(mockSuccess).toHaveBeenCalledWith('Loot Updated', expect.any(String))
  })

  it('should show error toast on failed update', async () => {
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

    vi.mocked(lootApi.updateLoot).mockRejectedValue(new Error('Failed'))

    const guildId = ref('test-guild')
    const { mutateAsync } = useLootEdit(guildId)

    await expect(mutateAsync({ awardId: 100, notes: 'Updated' })).rejects.toThrow()

    expect(mockError).toHaveBeenCalledWith('Update Failed', expect.any(String))
  })
})
