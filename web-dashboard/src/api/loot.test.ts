import { describe, it, expect, vi, beforeEach } from 'vitest'
import { lootApi } from './loot'
import { api } from './client'

vi.mock('./client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('lootApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getMyLootHistory', () => {
    it('should fetch loot history for current user', async () => {
      const mockResponse = {
        raiderId: 1,
        characterName: 'TestRaider',
        awards: [],
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await lootApi.getMyLootHistory('test-guild')

      expect(api.get).toHaveBeenCalledWith('/v1/loot/guilds/test-guild/me/history?limit=20')
      expect(result).toEqual(mockResponse)
    })
  })

  describe('awardLoot', () => {
    it('should create a new loot award', async () => {
      const request = {
        raiderId: 1,
        itemId: 12345,
        itemName: 'Legendary Sword',
        notes: 'Best in slot',
      }
      const mockAward = {
        id: 100,
        ...request,
        characterName: 'TestRaider',
        awardedAt: '2026-02-05T10:00:00Z',
        flpsAtAward: 85.5,
        rdfExpired: false,
      }
      vi.mocked(api.post).mockResolvedValue({ data: mockAward })

      const result = await lootApi.awardLoot('test-guild', request)

      expect(api.post).toHaveBeenCalledWith('/v1/loot/guilds/test-guild/awards', request)
      expect(result).toEqual(mockAward)
      expect(result.id).toBe(100)
    })
  })

  describe('updateLoot', () => {
    it('should update an existing loot award', async () => {
      const request = {
        notes: 'Updated notes',
      }
      const mockAward = {
        id: 100,
        itemId: 12345,
        itemName: 'Legendary Sword',
        raiderId: 1,
        characterName: 'TestRaider',
        awardedAt: '2026-02-05T10:00:00Z',
        flpsAtAward: 85.5,
        rdfExpired: false,
        notes: 'Updated notes',
      }
      vi.mocked(api.patch).mockResolvedValue({ data: mockAward })

      const result = await lootApi.updateLoot(100, request)

      expect(api.patch).toHaveBeenCalledWith('/v1/loot/awards/100', request)
      expect(result).toEqual(mockAward)
    })
  })

  describe('revokeLoot', () => {
    it('should delete a loot award', async () => {
      vi.mocked(api.delete).mockResolvedValue({})

      await lootApi.revokeLoot(100)

      expect(api.delete).toHaveBeenCalledWith('/v1/loot/awards/100')
    })
  })

  describe('searchItems', () => {
    it('should search for items by query', async () => {
      const mockItems = [
        { id: 12345, name: 'Legendary Sword', quality: 'LEGENDARY' },
        { id: 12346, name: 'Legendary Shield', quality: 'LEGENDARY' },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockItems })

      const result = await lootApi.searchItems('Legendary')

      expect(api.get).toHaveBeenCalledWith('/v1/game-data/items/search', {
        params: { q: 'Legendary', limit: 20 },
      })
      expect(result).toHaveLength(2)
      expect(result[0].name).toBe('Legendary Sword')
    })

    it('should respect custom limit parameter', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: [] })

      await lootApi.searchItems('Sword', 10)

      expect(api.get).toHaveBeenCalledWith('/v1/game-data/items/search', {
        params: { q: 'Sword', limit: 10 },
      })
    })
  })
})
