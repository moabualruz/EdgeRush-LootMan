import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  fetchUserGuilds,
  fetchActiveGuildContext,
  setActiveCharacter,
  fetchGuildPermissions,
  addGuildPermission,
  removeGuildPermission,
  fetchPermissionTypes,
  fetchRanksWithPermissions,
} from './guildContext'
import { api } from './client'

vi.mock('./client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('guildContext API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('fetchUserGuilds', () => {
    it('should fetch guilds for current user', async () => {
      const mockGuilds = [
        {
          guildId: 'guild-1',
          guildName: 'Test Guild',
          characterName: 'TestChar',
          characterRealm: 'Tarren Mill',
          characterClass: 'Warrior',
          characterMappingId: 1,
          raiderId: 10,
          rank: 'Officer',
          permissions: ['SETTINGS_ACCESS'],
          isActive: true,
        },
        {
          guildId: 'guild-2',
          guildName: 'Alt Guild',
          characterName: 'AltChar',
          characterRealm: 'Silvermoon',
          characterClass: 'Mage',
          characterMappingId: 2,
          raiderId: 20,
          rank: 'Member',
          permissions: [],
          isActive: false,
        },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockGuilds })

      const result = await fetchUserGuilds()

      expect(api.get).toHaveBeenCalledWith('/v1/user/guilds')
      expect(result).toEqual(mockGuilds)
      expect(result).toHaveLength(2)
    })

    it('should return empty array when user has no guilds', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: [] })

      const result = await fetchUserGuilds()

      expect(result).toEqual([])
    })

    it('should propagate API errors', async () => {
      vi.mocked(api.get).mockRejectedValue(new Error('Network error'))

      await expect(fetchUserGuilds()).rejects.toThrow('Network error')
    })
  })

  describe('fetchActiveGuildContext', () => {
    it('should fetch active guild context', async () => {
      const mockContext = {
        guildId: 'active-guild',
        guildName: 'Active Guild',
        characterName: 'MainChar',
        characterRealm: 'Tarren Mill',
        characterClass: 'Paladin',
        characterMappingId: 42,
        raiderId: 100,
        rank: 'Guild Master',
        permissions: ['SETTINGS_ACCESS', 'LOOT_MANAGEMENT'],
        isActive: true,
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockContext })

      const result = await fetchActiveGuildContext()

      expect(api.get).toHaveBeenCalledWith('/v1/user/guilds/active')
      expect(result).toEqual(mockContext)
    })

    it('should return null when no active context', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: null })

      const result = await fetchActiveGuildContext()

      expect(result).toBeNull()
    })
  })

  describe('setActiveCharacter', () => {
    it('should set active character and return new context', async () => {
      const mockContext = {
        guildId: 'new-guild',
        guildName: 'New Guild',
        characterName: 'NewActive',
        characterRealm: 'Test Realm',
        characterClass: 'Druid',
        characterMappingId: 99,
        raiderId: 200,
        rank: 'Raider',
        permissions: ['VIEW_ALL_SCORES'],
        isActive: true,
      }
      vi.mocked(api.put).mockResolvedValue({ data: mockContext })

      const result = await setActiveCharacter(99)

      expect(api.put).toHaveBeenCalledWith('/v1/user/guilds/active', { characterMappingId: 99 })
      expect(result).toEqual(mockContext)
      expect(result.characterMappingId).toBe(99)
    })

    it('should propagate error when invalid mapping ID', async () => {
      vi.mocked(api.put).mockRejectedValue({ response: { status: 400 } })

      await expect(setActiveCharacter(-1)).rejects.toEqual({ response: { status: 400 } })
    })
  })

  describe('fetchGuildPermissions', () => {
    it('should fetch permissions for a guild', async () => {
      const mockPermissions = [
        {
          id: 1,
          guildId: 'test-guild',
          rankName: 'Officer',
          permissionType: 'SETTINGS_ACCESS',
          createdAt: '2024-01-15T10:00:00Z',
        },
        {
          id: 2,
          guildId: 'test-guild',
          rankName: 'Officer',
          permissionType: 'LOOT_MANAGEMENT',
          createdAt: '2024-01-15T10:00:00Z',
        },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockPermissions })

      const result = await fetchGuildPermissions('test-guild')

      expect(api.get).toHaveBeenCalledWith('/v1/guilds/test-guild/permissions')
      expect(result).toEqual(mockPermissions)
      expect(result).toHaveLength(2)
    })

    it('should return empty array when no permissions', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: [] })

      const result = await fetchGuildPermissions('empty-guild')

      expect(result).toEqual([])
    })

    it('should handle URL encoding for guild ID', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: [] })

      await fetchGuildPermissions('guild-with-special-chars')

      expect(api.get).toHaveBeenCalledWith('/v1/guilds/guild-with-special-chars/permissions')
    })
  })

  describe('addGuildPermission', () => {
    it('should add a new permission', async () => {
      const mockPermission = {
        id: 3,
        guildId: 'test-guild',
        rankName: 'Raider',
        permissionType: 'VIEW_ALL_SCORES',
        createdAt: '2024-01-15T12:00:00Z',
      }
      vi.mocked(api.post).mockResolvedValue({ data: mockPermission })

      const result = await addGuildPermission('test-guild', 'Raider', 'VIEW_ALL_SCORES')

      expect(api.post).toHaveBeenCalledWith('/v1/guilds/test-guild/permissions', {
        rankName: 'Raider',
        permissionType: 'VIEW_ALL_SCORES',
      })
      expect(result).toEqual(mockPermission)
    })

    it('should propagate error for invalid permission type', async () => {
      vi.mocked(api.post).mockRejectedValue({ response: { status: 400 } })

      await expect(addGuildPermission('test-guild', 'Raider', 'INVALID_TYPE')).rejects.toEqual({
        response: { status: 400 },
      })
    })

    it('should propagate error when not authorized', async () => {
      vi.mocked(api.post).mockRejectedValue({ response: { status: 403 } })

      await expect(addGuildPermission('test-guild', 'Member', 'SETTINGS_ACCESS')).rejects.toEqual({
        response: { status: 403 },
      })
    })
  })

  describe('removeGuildPermission', () => {
    it('should remove a permission', async () => {
      vi.mocked(api.delete).mockResolvedValue({ data: undefined })

      await removeGuildPermission('test-guild', 123)

      expect(api.delete).toHaveBeenCalledWith('/v1/guilds/test-guild/permissions/123')
    })

    it('should propagate error when permission not found', async () => {
      vi.mocked(api.delete).mockRejectedValue({ response: { status: 404 } })

      await expect(removeGuildPermission('test-guild', 999)).rejects.toEqual({
        response: { status: 404 },
      })
    })

    it('should propagate error when not authorized', async () => {
      vi.mocked(api.delete).mockRejectedValue({ response: { status: 403 } })

      await expect(removeGuildPermission('test-guild', 1)).rejects.toEqual({
        response: { status: 403 },
      })
    })
  })

  describe('fetchPermissionTypes', () => {
    it('should fetch all permission types', async () => {
      const mockTypes = [
        { name: 'SETTINGS_ACCESS', description: 'Access to guild settings page' },
        { name: 'LOOT_MANAGEMENT', description: 'Manage loot distribution' },
        { name: 'MEMBER_MANAGEMENT', description: 'Manage guild members' },
        { name: 'VIEW_ALL_SCORES', description: 'View all member FLPS scores' },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockTypes })

      const result = await fetchPermissionTypes()

      expect(api.get).toHaveBeenCalledWith('/v1/guilds/default/permissions/types')
      expect(result).toEqual(mockTypes)
      expect(result).toHaveLength(4)
    })

    it('should return all permission type names', async () => {
      const mockTypes = [
        { name: 'SETTINGS_ACCESS', description: 'Access to guild settings page' },
        { name: 'LOOT_MANAGEMENT', description: 'Manage loot distribution' },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockTypes })

      const result = await fetchPermissionTypes()

      expect(result.map((t) => t.name)).toContain('SETTINGS_ACCESS')
      expect(result.map((t) => t.name)).toContain('LOOT_MANAGEMENT')
    })
  })

  describe('fetchRanksWithPermissions', () => {
    it('should fetch ranks with permissions for a guild', async () => {
      const mockRanks = ['Guild Master', 'Officer', 'Raider']
      vi.mocked(api.get).mockResolvedValue({ data: mockRanks })

      const result = await fetchRanksWithPermissions('test-guild')

      expect(api.get).toHaveBeenCalledWith('/v1/guilds/test-guild/permissions/ranks')
      expect(result).toEqual(mockRanks)
      expect(result).toHaveLength(3)
    })

    it('should return empty array when no ranks have permissions', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: [] })

      const result = await fetchRanksWithPermissions('new-guild')

      expect(result).toEqual([])
    })

    it('should propagate error when not authorized', async () => {
      vi.mocked(api.get).mockRejectedValue({ response: { status: 403 } })

      await expect(fetchRanksWithPermissions('test-guild')).rejects.toEqual({
        response: { status: 403 },
      })
    })
  })
})
