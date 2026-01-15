import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useCharacterLookup } from './useCharacterLookup'
import { recruitmentApi } from '@/api/recruitment'
import { flushPromises } from '@vue/test-utils'

vi.mock('@/api/recruitment', () => ({
  recruitmentApi: {
    lookupCharacterFull: vi.fn(),
  },
}))

describe('useCharacterLookup', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('initial state', () => {
    it('should have default initial state', () => {
      const { isLoading, characterData, error, hasSearched } = useCharacterLookup()

      expect(isLoading.value).toBe(false)
      expect(characterData.value).toBe(null)
      expect(error.value).toBe(null)
      expect(hasSearched.value).toBe(false)
    })
  })

  describe('lookupCharacter', () => {
    it('should fetch character data successfully', async () => {
      const mockData = {
        name: 'Arthas',
        realm: 'Illidan',
        region: 'us',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        role: 'DPS',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        bestParseAverage: 85.5,
        medianParseAverage: 78.2,
        profileUrl: 'https://raider.io/characters/us/illidan/Arthas',
      }
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(mockData)

      const { lookupCharacter, characterData, isLoading, hasSearched } = useCharacterLookup()

      // Start the lookup
      const promise = lookupCharacter('us', 'Illidan', 'Arthas')

      // Should be loading
      expect(isLoading.value).toBe(true)

      // Wait for completion
      await promise

      // Should have data
      expect(isLoading.value).toBe(false)
      expect(characterData.value).toEqual(mockData)
      expect(hasSearched.value).toBe(true)
      expect(recruitmentApi.lookupCharacterFull).toHaveBeenCalledWith('us', 'Illidan', 'Arthas')
    })

    it('should handle character not found', async () => {
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(null)

      const { lookupCharacter, characterData, error, hasSearched } = useCharacterLookup()

      await lookupCharacter('us', 'Illidan', 'NonExistent')

      expect(characterData.value).toBe(null)
      expect(error.value).toBe('Character not found')
      expect(hasSearched.value).toBe(true)
    })

    it('should handle API errors', async () => {
      vi.mocked(recruitmentApi.lookupCharacterFull).mockRejectedValue(new Error('API error'))

      const { lookupCharacter, error, isLoading } = useCharacterLookup()

      await lookupCharacter('us', 'Illidan', 'Arthas')

      expect(isLoading.value).toBe(false)
      expect(error.value).toBe('Failed to fetch character data')
    })

    it('should clear previous data on new search', async () => {
      const mockData1 = {
        name: 'Arthas',
        realm: 'Illidan',
        region: 'us',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        role: 'DPS',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        bestParseAverage: 85.5,
        medianParseAverage: 78.2,
        profileUrl: 'https://raider.io/characters/us/illidan/Arthas',
      }
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(mockData1)

      const { lookupCharacter, characterData, error } = useCharacterLookup()

      // First search
      await lookupCharacter('us', 'Illidan', 'Arthas')
      expect(characterData.value).toEqual(mockData1)

      // Set up mock for not found
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(null)

      // Second search should clear previous data
      await lookupCharacter('us', 'Illidan', 'NonExistent')
      expect(characterData.value).toBe(null)
      expect(error.value).toBe('Character not found')
    })
  })

  describe('debounced lookup', () => {
    it('should debounce rapid lookups', async () => {
      const mockData = {
        name: 'Arthas',
        realm: 'Illidan',
        region: 'us',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        role: 'DPS',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        bestParseAverage: 85.5,
        medianParseAverage: 78.2,
        profileUrl: 'https://raider.io/characters/us/illidan/Arthas',
      }
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(mockData)

      const { debouncedLookup } = useCharacterLookup()

      // Call multiple times rapidly
      debouncedLookup('us', 'Illidan', 'Art')
      debouncedLookup('us', 'Illidan', 'Arth')
      debouncedLookup('us', 'Illidan', 'Artha')
      debouncedLookup('us', 'Illidan', 'Arthas')

      // API should not have been called yet
      expect(recruitmentApi.lookupCharacterFull).not.toHaveBeenCalled()

      // Advance timers past debounce delay
      vi.advanceTimersByTime(500)
      await flushPromises()

      // Should only have been called once with the last value
      expect(recruitmentApi.lookupCharacterFull).toHaveBeenCalledTimes(1)
      expect(recruitmentApi.lookupCharacterFull).toHaveBeenCalledWith('us', 'Illidan', 'Arthas')
    })

    it('should not call API for empty name', async () => {
      const { debouncedLookup } = useCharacterLookup()

      debouncedLookup('us', 'Illidan', '')
      vi.advanceTimersByTime(500)
      await flushPromises()

      expect(recruitmentApi.lookupCharacterFull).not.toHaveBeenCalled()
    })

    it('should not call API for name shorter than minimum length', async () => {
      const { debouncedLookup } = useCharacterLookup({ minNameLength: 3 })

      debouncedLookup('us', 'Illidan', 'Ar')
      vi.advanceTimersByTime(500)
      await flushPromises()

      expect(recruitmentApi.lookupCharacterFull).not.toHaveBeenCalled()
    })
  })

  describe('reset', () => {
    it('should reset all state', async () => {
      const mockData = {
        name: 'Arthas',
        realm: 'Illidan',
        region: 'us',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        role: 'DPS',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        bestParseAverage: 85.5,
        medianParseAverage: 78.2,
        profileUrl: 'https://raider.io/characters/us/illidan/Arthas',
      }
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(mockData)

      const { lookupCharacter, characterData, hasSearched, error, reset } = useCharacterLookup()

      await lookupCharacter('us', 'Illidan', 'Arthas')
      expect(characterData.value).not.toBe(null)
      expect(hasSearched.value).toBe(true)

      reset()

      expect(characterData.value).toBe(null)
      expect(hasSearched.value).toBe(false)
      expect(error.value).toBe(null)
    })
  })

  describe('computed properties', () => {
    it('should compute hasRaiderIOData correctly', async () => {
      const mockData = {
        name: 'Arthas',
        realm: 'Illidan',
        region: 'us',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        role: 'DPS',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        bestParseAverage: null,
        medianParseAverage: null,
        profileUrl: 'https://raider.io/characters/us/illidan/Arthas',
      }
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(mockData)

      const { lookupCharacter, hasRaiderIOData } = useCharacterLookup()

      await lookupCharacter('us', 'Illidan', 'Arthas')

      expect(hasRaiderIOData.value).toBe(true)
    })

    it('should compute hasWarcraftLogsData correctly', async () => {
      const mockData = {
        name: 'Arthas',
        realm: 'Illidan',
        region: 'us',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        role: 'DPS',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        bestParseAverage: 85.5,
        medianParseAverage: 78.2,
        profileUrl: 'https://raider.io/characters/us/illidan/Arthas',
      }
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(mockData)

      const { lookupCharacter, hasWarcraftLogsData } = useCharacterLookup()

      await lookupCharacter('us', 'Illidan', 'Arthas')

      expect(hasWarcraftLogsData.value).toBe(true)
    })

    it('should return false for hasWarcraftLogsData when no parse data', async () => {
      const mockData = {
        name: 'Arthas',
        realm: 'Illidan',
        region: 'us',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        role: 'DPS',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        bestParseAverage: null,
        medianParseAverage: null,
        profileUrl: 'https://raider.io/characters/us/illidan/Arthas',
      }
      vi.mocked(recruitmentApi.lookupCharacterFull).mockResolvedValue(mockData)

      const { lookupCharacter, hasWarcraftLogsData } = useCharacterLookup()

      await lookupCharacter('us', 'Illidan', 'Arthas')

      expect(hasWarcraftLogsData.value).toBe(false)
    })
  })
})
