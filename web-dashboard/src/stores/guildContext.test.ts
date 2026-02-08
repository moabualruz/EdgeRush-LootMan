import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useGuildContextStore } from './guildContext'
import type { GuildContext } from '@/types'

// Mock the API
vi.mock('@/api/guildContext', () => ({
  fetchUserGuilds: vi.fn(),
  fetchActiveGuildContext: vi.fn(),

  setActiveCharacter: vi.fn(),
}))

vi.mock('@/api/user', () => ({
  fetchUserCharacters: vi.fn().mockResolvedValue([]),
}))

import {
  fetchUserGuilds,
  setActiveCharacter as apiSetActiveCharacter,
} from '@/api/guildContext'

const mockFetchUserGuilds = fetchUserGuilds as ReturnType<typeof vi.fn>
const mockSetActiveCharacter = apiSetActiveCharacter as ReturnType<typeof vi.fn>

describe('guildContext store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // Helper function to create test guild contexts
  function createGuildContext(overrides: Partial<GuildContext> = {}): GuildContext {
    return {
      guildId: 'test-guild',
      guildName: 'Test Guild',
      characterName: 'TestChar',
      characterRealm: 'Test Realm',
      characterClass: 'Warrior',
      characterMappingId: 1,
      raiderId: 1,
      rank: 'Member',
      permissions: [],
      isActive: false,
      ...overrides,
    }
  }

  describe('initial state', () => {
    it('should initialize with empty guilds', () => {
      const store = useGuildContextStore()
      expect(store.guilds).toEqual([])
    })

    it('should initialize with null activeGuild', () => {
      const store = useGuildContextStore()
      expect(store.activeGuild).toBeNull()
    })

    it('should initialize with loading false', () => {
      const store = useGuildContextStore()
      expect(store.loading).toBe(false)
    })

    it('should initialize with no error', () => {
      const store = useGuildContextStore()
      expect(store.error).toBeNull()
    })
  })

  describe('computed properties', () => {
    it('should return false for hasMultipleGuilds when no guilds', () => {
      const store = useGuildContextStore()
      expect(store.hasMultipleGuilds).toBe(false)
    })

    it('should return false for hasMultipleGuilds when one guild', () => {
      const store = useGuildContextStore()
      store.guilds = [createGuildContext()]
      expect(store.hasMultipleGuilds).toBe(false)
    })

    it('should return true for hasMultipleGuilds when multiple guilds', () => {
      const store = useGuildContextStore()
      store.guilds = [
        createGuildContext({ guildId: 'guild-1' }),
        createGuildContext({ guildId: 'guild-2' }),
      ]
      expect(store.hasMultipleGuilds).toBe(true)
    })

    it('should return null for currentGuildId when no active guild', () => {
      const store = useGuildContextStore()
      expect(store.currentGuildId).toBeNull()
    })

    it('should return guildId for currentGuildId when active guild exists', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ guildId: 'my-guild' })
      expect(store.currentGuildId).toBe('my-guild')
    })
  })

  describe('permission computed properties', () => {
    it('should return false for canAccessSettings when no active guild', () => {
      const store = useGuildContextStore()
      expect(store.canAccessSettings).toBe(false)
    })

    it('should return false for canAccessSettings without permission', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ permissions: [] })
      expect(store.canAccessSettings).toBe(false)
    })

    it('should return true for canAccessSettings with permission', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ permissions: ['SETTINGS_ACCESS'] })
      expect(store.canAccessSettings).toBe(true)
    })

    it('should return true for canManageLoot with permission', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ permissions: ['LOOT_MANAGEMENT'] })
      expect(store.canManageLoot).toBe(true)
    })

    it('should return true for canManageMembers with permission', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ permissions: ['MEMBER_MANAGEMENT'] })
      expect(store.canManageMembers).toBe(true)
    })

    it('should return true for canViewAllScores with permission', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ permissions: ['VIEW_ALL_SCORES'] })
      expect(store.canViewAllScores).toBe(true)
    })
  })

  describe('hasPermission method', () => {
    it('should return false when no active guild', () => {
      const store = useGuildContextStore()
      expect(store.hasPermission('SETTINGS_ACCESS')).toBe(false)
    })

    it('should return false when permission not present', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ permissions: [] })
      expect(store.hasPermission('SETTINGS_ACCESS')).toBe(false)
    })

    it('should return true when permission present', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ permissions: ['SETTINGS_ACCESS', 'LOOT_MANAGEMENT'] })
      expect(store.hasPermission('SETTINGS_ACCESS')).toBe(true)
      expect(store.hasPermission('LOOT_MANAGEMENT')).toBe(true)
    })

    it('should return false for permission not in list', () => {
      const store = useGuildContextStore()
      store.activeGuild = createGuildContext({ permissions: ['SETTINGS_ACCESS'] })
      expect(store.hasPermission('VIEW_ALL_SCORES')).toBe(false)
    })
  })

  describe('fetchGuilds', () => {
    it('should set loading to true while fetching', async () => {
      const store = useGuildContextStore()
      mockFetchUserGuilds.mockImplementation(() => new Promise(() => {}))

      const promise = store.fetchGuilds()
      expect(store.loading).toBe(true)
      // Clean up
      vi.clearAllMocks()
    })

    it('should fetch and set guilds', async () => {
      const store = useGuildContextStore()
      const guilds = [
        createGuildContext({ guildId: 'guild-1', isActive: true }),
        createGuildContext({ guildId: 'guild-2', isActive: false }),
      ]
      mockFetchUserGuilds.mockResolvedValue(guilds)

      await store.fetchGuilds()

      expect(store.guilds).toEqual(guilds)
      expect(store.loading).toBe(false)
    })

    it('should set active guild from fetched data', async () => {
      const store = useGuildContextStore()
      const activeGuild = createGuildContext({ guildId: 'active-guild', isActive: true })
      const guilds = [
        activeGuild,
        createGuildContext({ guildId: 'inactive-guild', isActive: false }),
      ]
      mockFetchUserGuilds.mockResolvedValue(guilds)

      await store.fetchGuilds()

      expect(store.activeGuild).toEqual(activeGuild)
    })

    it('should auto-select first guild if none is active', async () => {
      const store = useGuildContextStore()
      const firstGuild = createGuildContext({ guildId: 'first-guild', isActive: false })
      const guilds = [
        firstGuild,
        createGuildContext({ guildId: 'second-guild', isActive: false }),
      ]
      mockFetchUserGuilds.mockResolvedValue(guilds)

      await store.fetchGuilds()

      expect(store.activeGuild).toEqual(firstGuild)
    })

    it('should handle empty guild list', async () => {
      const store = useGuildContextStore()
      mockFetchUserGuilds.mockResolvedValue([])

      await store.fetchGuilds()

      expect(store.guilds).toEqual([])
      expect(store.activeGuild).toBeNull()
    })

    it('should handle error and clear data', async () => {
      const store = useGuildContextStore()
      mockFetchUserGuilds.mockRejectedValue(new Error('Network error'))

      await store.fetchGuilds()

      expect(store.error).toBe('Network error')
      expect(store.guilds).toEqual([])
      expect(store.activeGuild).toBeNull()
      expect(store.loading).toBe(false)
    })

    it('should handle non-Error exception', async () => {
      const store = useGuildContextStore()
      mockFetchUserGuilds.mockRejectedValue('Something went wrong')

      await store.fetchGuilds()

      expect(store.error).toBe('Failed to fetch guilds')
    })

    it('should clear previous error on successful fetch', async () => {
      const store = useGuildContextStore()
      store.error = 'Previous error'
      mockFetchUserGuilds.mockResolvedValue([])

      await store.fetchGuilds()

      expect(store.error).toBeNull()
    })
  })

  describe('switchCharacter', () => {
    it('should set loading while switching', async () => {
      const store = useGuildContextStore()
      mockSetActiveCharacter.mockImplementation(() => new Promise(() => {}))

      const promise = store.switchCharacter(42)
      expect(store.loading).toBe(true)
      // Clean up
      vi.clearAllMocks()
    })

    it('should update active guild after switch', async () => {
      const store = useGuildContextStore()
      const newContext = createGuildContext({
        characterMappingId: 42,
        guildId: 'new-guild',
        isActive: true,
      })
      mockSetActiveCharacter.mockResolvedValue(newContext)

      store.guilds = [
        createGuildContext({ characterMappingId: 1, isActive: true }),
        createGuildContext({ characterMappingId: 42, isActive: false }),
      ]

      await store.switchCharacter(42)

      expect(store.activeGuild).toEqual(newContext)
    })

    it('should update isActive flags in guilds list', async () => {
      const store = useGuildContextStore()
      const newContext = createGuildContext({ characterMappingId: 42, isActive: true })
      mockSetActiveCharacter.mockResolvedValue(newContext)

      store.guilds = [
        createGuildContext({ characterMappingId: 1, isActive: true }),
        createGuildContext({ characterMappingId: 42, isActive: false }),
      ]

      await store.switchCharacter(42)

      expect(store.guilds[0].isActive).toBe(false)
      expect(store.guilds[1].isActive).toBe(true)
    })

    it('should handle error and rethrow', async () => {
      const store = useGuildContextStore()
      mockSetActiveCharacter.mockRejectedValue(new Error('Switch failed'))

      await expect(store.switchCharacter(42)).rejects.toThrow('Switch failed')
      expect(store.error).toBe('Switch failed')
      expect(store.loading).toBe(false)
    })

    it('should handle non-Error exception', async () => {
      const store = useGuildContextStore()
      mockSetActiveCharacter.mockRejectedValue('Something went wrong')

      await expect(store.switchCharacter(42)).rejects.toBe('Something went wrong')
      expect(store.error).toBe('Failed to switch character')
    })

    it('should clear previous error on successful switch', async () => {
      const store = useGuildContextStore()
      store.error = 'Previous error'
      mockSetActiveCharacter.mockResolvedValue(createGuildContext())

      await store.switchCharacter(42)

      expect(store.error).toBeNull()
    })
  })

  describe('clear', () => {
    it('should clear all state', () => {
      const store = useGuildContextStore()
      store.guilds = [createGuildContext()]
      store.activeGuild = createGuildContext()
      store.error = 'Some error'

      store.clear()

      expect(store.guilds).toEqual([])
      expect(store.activeGuild).toBeNull()
      expect(store.error).toBeNull()
    })
  })
})
