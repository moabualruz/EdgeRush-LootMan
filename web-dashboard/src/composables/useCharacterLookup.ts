/**
 * Character Lookup Composable.
 *
 * Provides reactive character data fetching from Raider.IO and Warcraft Logs
 * with debouncing support for auto-complete style lookups.
 */

import { ref, computed, readonly } from 'vue'
import { recruitmentApi, type CharacterFullLookupResponse } from '@/api/recruitment'

export interface UseCharacterLookupOptions {
  /** Minimum character name length before fetching (default: 2) */
  minNameLength?: number
  /** Debounce delay in milliseconds (default: 500) */
  debounceDelay?: number
}

/**
 * Composable for fetching character data from external APIs.
 *
 * Provides both immediate lookup and debounced lookup for typing scenarios.
 *
 * @example
 * ```vue
 * <script setup>
 * import { useCharacterLookup } from '@/composables/useCharacterLookup'
 *
 * const { lookupCharacter, characterData, isLoading, error } = useCharacterLookup()
 *
 * async function fetchCharacter() {
 *   await lookupCharacter('us', 'Illidan', 'Arthas')
 *   if (characterData.value) {
 *     console.log('Found:', characterData.value.name)
 *   }
 * }
 * </script>
 * ```
 */
export function useCharacterLookup(options: UseCharacterLookupOptions = {}) {
  const { minNameLength = 2, debounceDelay = 500 } = options

  // State
  const isLoading = ref(false)
  const characterData = ref<CharacterFullLookupResponse | null>(null)
  const error = ref<string | null>(null)
  const hasSearched = ref(false)

  // Debounce timer
  let debounceTimer: ReturnType<typeof setTimeout> | null = null

  /**
   * Lookup character data immediately (no debouncing).
   */
  async function lookupCharacter(
    region: string,
    realm: string,
    name: string
  ): Promise<CharacterFullLookupResponse | null> {
    // Clear previous state
    error.value = null
    isLoading.value = true
    characterData.value = null

    try {
      const data = await recruitmentApi.lookupCharacterFull(region, realm, name)

      if (data) {
        characterData.value = data
        hasSearched.value = true
        return data
      } else {
        error.value = 'Character not found'
        hasSearched.value = true
        return null
      }
    } catch (e) {
      error.value = 'Failed to fetch character data'
      hasSearched.value = true
      return null
    } finally {
      isLoading.value = false
    }
  }

  /**
   * Lookup character data with debouncing.
   * Good for auto-complete style inputs where the user is typing.
   */
  function debouncedLookup(region: string, realm: string, name: string): void {
    // Clear existing timer
    if (debounceTimer) {
      clearTimeout(debounceTimer)
      debounceTimer = null
    }

    // Skip if name is too short
    if (!name || name.length < minNameLength) {
      return
    }

    // Set new timer
    debounceTimer = setTimeout(() => {
      lookupCharacter(region, realm, name)
    }, debounceDelay)
  }

  /**
   * Reset all state to initial values.
   */
  function reset(): void {
    isLoading.value = false
    characterData.value = null
    error.value = null
    hasSearched.value = false

    if (debounceTimer) {
      clearTimeout(debounceTimer)
      debounceTimer = null
    }
  }

  // Computed properties
  const hasRaiderIOData = computed(() => {
    return characterData.value !== null && characterData.value.itemLevel !== null
  })

  const hasWarcraftLogsData = computed(() => {
    return characterData.value !== null && characterData.value.bestParseAverage !== null
  })

  return {
    // State (readonly)
    isLoading: readonly(isLoading),
    characterData: readonly(characterData),
    error: readonly(error),
    hasSearched: readonly(hasSearched),

    // Computed
    hasRaiderIOData,
    hasWarcraftLogsData,

    // Methods
    lookupCharacter,
    debouncedLookup,
    reset,
  }
}

export default useCharacterLookup
