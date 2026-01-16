import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { GuildContext, GuildPermissionType } from '@/types'
import {
  fetchUserGuilds,
  fetchActiveGuildContext,
  setActiveCharacter as apiSetActiveCharacter,
} from '@/api/guildContext'

export const useGuildContextStore = defineStore('guildContext', () => {
  const guilds = ref<GuildContext[]>([])
  const activeGuild = ref<GuildContext | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Computed properties
  const hasMultipleGuilds = computed(() => guilds.value.length > 1)

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
   */
  async function fetchGuilds(): Promise<void> {
    loading.value = true
    error.value = null

    try {
      guilds.value = await fetchUserGuilds()

      // Find and set the active guild
      const active = guilds.value.find((g) => g.isActive)
      if (active) {
        activeGuild.value = active
      } else if (guilds.value.length > 0) {
        // Auto-select first guild if none is active
        activeGuild.value = guilds.value[0]
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch guilds'
      guilds.value = []
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
    activeGuild.value = null
    error.value = null
  }

  return {
    // State
    guilds,
    activeGuild,
    loading,
    error,

    // Computed
    hasMultipleGuilds,
    canAccessSettings,
    canManageLoot,
    canManageMembers,
    canViewAllScores,
    currentGuildId,

    // Methods
    hasPermission,
    fetchGuilds,
    switchCharacter,
    clear,
  }
})
