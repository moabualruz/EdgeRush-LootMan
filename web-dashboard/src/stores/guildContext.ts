import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { GuildContext, GuildPermissionType } from '@/types'
import {
  fetchUserGuilds,
  fetchActiveGuildContext,
  setActiveCharacter as apiSetActiveCharacter,
} from '@/api/guildContext'
import { fetchUserCharacters, type UserCharacter } from '@/api/user'

export const useGuildContextStore = defineStore('guildContext', () => {
  const guilds = ref<GuildContext[]>([])
  const activeGuild = ref<GuildContext | null>(null)
  const battlenetCharacters = ref<UserCharacter[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Computed properties
  const hasMultipleGuilds = computed(() => guilds.value.length > 1)
  const hasMultipleCharacters = computed(() => battlenetCharacters.value.length > 1)

  // Combined characters: guild contexts + unlinked Battle.net characters
  const allCharacters = computed(() => {
    // If we have guild contexts, use those
    if (guilds.value.length > 0) {
      return guilds.value
    }
    // Otherwise, convert Battle.net characters to a compatible format
    return battlenetCharacters.value.map((char): GuildContext => ({
      guildId: 'default',
      guildName: 'No Guild',
      characterName: char.name,
      characterRealm: char.realm,
      characterClass: char.className?.toUpperCase().replace(/ /g, '_') || 'WARRIOR',
      characterMappingId: char.id, // Use character ID as mapping ID
      raiderId: 0, // No raider linked
      rank: null,
      permissions: [],
      isActive: false,
    }))
  })

  const canAccessSettings = computed(
    () => activeGuild.value?.permissions.includes('SETTINGS_ACCESS') ?? false
  )

  const canManageLoot = computed(
    () => activeGuild.value?.permissions.includes('LOOT_MANAGEMENT') ?? false
  )

  const canManageMembers = computed(
    () => activeGuild.value?.permissions.includes('MEMBER_MANAGEMENT') ?? false
  )

  const canViewAllScores = computed(
    () => activeGuild.value?.permissions.includes('VIEW_ALL_SCORES') ?? false
  )

  const currentGuildId = computed(() => activeGuild.value?.guildId ?? null)

  /**
   * Check if the user has a specific permission in the active guild.
   */
  function hasPermission(permission: GuildPermissionType): boolean {
    return activeGuild.value?.permissions.includes(permission) ?? false
  }

  /**
   * Fetch all guilds for the current user.
   * Also fetches Battle.net characters as fallback.
   */
  async function fetchGuilds(): Promise<void> {
    loading.value = true
    error.value = null

    try {
      // Fetch both in parallel
      const [guildContexts, bnetChars] = await Promise.all([
        fetchUserGuilds(),
        fetchUserCharacters(),
      ])

      guilds.value = guildContexts
      battlenetCharacters.value = bnetChars

      // Find and set the active guild
      const active = guilds.value.find((g) => g.isActive)
      if (active) {
        activeGuild.value = active
      } else if (guilds.value.length > 0) {
        // Auto-select first guild if none is active
        activeGuild.value = guilds.value[0]
      } else if (bnetChars.length > 0) {
        // Fall back to first Battle.net character if no guild contexts
        activeGuild.value = {
          guildId: 'default',
          guildName: 'No Guild',
          characterName: bnetChars[0].name,
          characterRealm: bnetChars[0].realm,
          characterClass: bnetChars[0].className?.toUpperCase().replace(/ /g, '_') || 'WARRIOR',
          characterMappingId: bnetChars[0].id,
          raiderId: 0,
          rank: null,
          permissions: [],
          isActive: true,
        }
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch guilds'
      guilds.value = []
      battlenetCharacters.value = []
      activeGuild.value = null
    } finally {
      loading.value = false
    }
  }

  /**
   * Switch to a different character/guild.
   */
  async function switchCharacter(mappingId: number): Promise<void> {
    loading.value = true
    error.value = null

    try {
      const newContext = await apiSetActiveCharacter(mappingId)

      // Update guilds list to reflect new active state
      guilds.value = guilds.value.map((g) => ({
        ...g,
        isActive: g.characterMappingId === mappingId,
      }))

      activeGuild.value = newContext
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to switch character'
      throw e
    } finally {
      loading.value = false
    }
  }

  /**
   * Clear the guild context (on logout).
   */
  function clear(): void {
    guilds.value = []
    battlenetCharacters.value = []
    activeGuild.value = null
    error.value = null
  }

  /**
   * Select a Battle.net character (when no guild contexts exist).
   */
  function selectBattlenetCharacter(characterId: number): void {
    const char = battlenetCharacters.value.find((c) => c.id === characterId)
    if (char) {
      activeGuild.value = {
        guildId: 'default',
        guildName: 'No Guild',
        characterName: char.name,
        characterRealm: char.realm,
        characterClass: char.className?.toUpperCase().replace(/ /g, '_') || 'WARRIOR',
        characterMappingId: char.id,
        raiderId: 0,
        rank: null,
        permissions: [],
        isActive: true,
      }
    }
  }

  return {
    // State
    guilds,
    activeGuild,
    battlenetCharacters,
    loading,
    error,

    // Computed
    hasMultipleGuilds,
    hasMultipleCharacters,
    allCharacters,
    canAccessSettings,
    canManageLoot,
    canManageMembers,
    canViewAllScores,
    currentGuildId,

    // Methods
    hasPermission,
    fetchGuilds,
    switchCharacter,
    selectBattlenetCharacter,
    clear,
  }
})
