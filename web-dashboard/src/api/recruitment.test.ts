import { describe, it, expect, vi, beforeEach } from 'vitest'
import { recruitmentApi } from './recruitment'
import { api } from './client'

vi.mock('./client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('recruitmentApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('lookupCharacter', () => {
    it('should fetch character data from Raider.IO', async () => {
      const mockResponse = {
        name: 'Arthas',
        realm: 'Illidan',
        region: 'us',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        role: 'DPS',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        profileUrl: 'https://raider.io/characters/us/illidan/Arthas',
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.lookupCharacter('us', 'Illidan', 'Arthas')

      expect(api.get).toHaveBeenCalledWith(
        '/api/v1/recruitment/applications/character-lookup',
        { params: { region: 'us', realm: 'Illidan', name: 'Arthas' } }
      )
      expect(result).toEqual(mockResponse)
    })

    it('should return null when character not found', async () => {
      vi.mocked(api.get).mockRejectedValue({ response: { status: 404 } })

      const result = await recruitmentApi.lookupCharacter('us', 'Illidan', 'NonExistent')

      expect(result).toBeNull()
    })
  })

  describe('lookupCharacterFull', () => {
    it('should fetch combined character data from Raider.IO and Warcraft Logs', async () => {
      const mockResponse = {
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
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.lookupCharacterFull('us', 'Illidan', 'Arthas')

      expect(api.get).toHaveBeenCalledWith(
        '/api/v1/recruitment/applications/character-lookup/full',
        { params: { region: 'us', realm: 'Illidan', name: 'Arthas' } }
      )
      expect(result).toEqual(mockResponse)
      expect(result?.bestParseAverage).toBe(85.5)
      expect(result?.medianParseAverage).toBe(78.2)
    })

    it('should return null when character not found', async () => {
      vi.mocked(api.get).mockRejectedValue({ response: { status: 404 } })

      const result = await recruitmentApi.lookupCharacterFull('us', 'Illidan', 'NonExistent')

      expect(result).toBeNull()
    })
  })

  describe('submitApplication', () => {
    it('should submit a new application', async () => {
      const mockRequest = {
        battleNetId: 'Player#1234',
        discordId: '123456789',
        email: 'player@example.com',
        characterName: 'Arthas',
        characterRealm: 'Illidan',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        itemLevel: 495.5,
        raiderIOScore: 2850.0,
        bestParseAverage: 85.5,
        age: 25,
        location: 'United States',
        timezone: 'EST',
        raidDaysAvailable: ['Tuesday', 'Thursday'],
        previousGuilds: 'Knights of the Frozen Throne',
        reasonForLeaving: 'Guild disbanded',
        whyThisGuild: 'Looking for a competitive guild',
      }
      const mockResponse = {
        id: 'app-123',
        ...mockRequest,
        guildId: 'guild-1',
        status: 'PENDING',
        createdAt: '2024-01-15T10:00:00Z',
        updatedAt: '2024-01-15T10:00:00Z',
      }
      vi.mocked(api.post).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.submitApplication('guild-1', mockRequest)

      expect(api.post).toHaveBeenCalledWith(
        '/api/v1/recruitment/applications/guilds/guild-1',
        mockRequest
      )
      expect(result).toEqual(mockResponse)
      expect(result.status).toBe('PENDING')
    })

    it('should throw error on conflict (duplicate application)', async () => {
      const mockRequest = {
        battleNetId: 'Player#1234',
        discordId: '123456789',
        email: 'player@example.com',
        characterName: 'Arthas',
        characterRealm: 'Illidan',
        characterClass: 'Death Knight',
        specialization: 'Frost',
        itemLevel: 495.5,
        raiderIOScore: null,
        bestParseAverage: null,
        age: 25,
        location: 'United States',
        timezone: 'EST',
        raidDaysAvailable: ['Tuesday'],
        previousGuilds: 'Old Guild',
        reasonForLeaving: 'Reason',
        whyThisGuild: 'Why',
      }
      vi.mocked(api.post).mockRejectedValue({ response: { status: 409 } })

      await expect(recruitmentApi.submitApplication('guild-1', mockRequest)).rejects.toThrow()
    })
  })

  describe('getApplicationsByGuild', () => {
    it('should fetch applications for a guild', async () => {
      const mockResponse = [
        { id: 'app-1', characterName: 'Arthas', status: 'PENDING' },
        { id: 'app-2', characterName: 'Jaina', status: 'APPROVED' },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.getApplicationsByGuild('guild-1')

      expect(api.get).toHaveBeenCalledWith('/api/v1/recruitment/applications/guilds/guild-1', {
        params: { offset: 0, limit: 50 },
      })
      expect(result).toHaveLength(2)
    })

    it('should fetch applications with status filter', async () => {
      const mockResponse = [{ id: 'app-1', characterName: 'Arthas', status: 'PENDING' }]
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.getApplicationsByGuild('guild-1', {
        status: 'PENDING',
        offset: 10,
        limit: 25,
      })

      expect(api.get).toHaveBeenCalledWith('/api/v1/recruitment/applications/guilds/guild-1', {
        params: { status: 'PENDING', offset: 10, limit: 25 },
      })
      expect(result).toHaveLength(1)
    })
  })

  describe('getPendingApplications', () => {
    it('should fetch pending applications for a guild', async () => {
      const mockResponse = [
        { id: 'app-1', characterName: 'Arthas', status: 'PENDING' },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.getPendingApplications('guild-1')

      expect(api.get).toHaveBeenCalledWith(
        '/api/v1/recruitment/applications/guilds/guild-1/pending',
        { params: { offset: 0, limit: 50 } }
      )
      expect(result).toHaveLength(1)
    })
  })

  describe('getApplicationById', () => {
    it('should fetch a single application by ID', async () => {
      const mockResponse = {
        id: 'app-123',
        characterName: 'Arthas',
        status: 'PENDING',
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.getApplicationById('app-123')

      expect(api.get).toHaveBeenCalledWith('/api/v1/recruitment/applications/app-123')
      expect(result).toEqual(mockResponse)
    })

    it('should return null when application not found', async () => {
      vi.mocked(api.get).mockRejectedValue({ response: { status: 404 } })

      const result = await recruitmentApi.getApplicationById('non-existent')

      expect(result).toBeNull()
    })
  })

  describe('startReview', () => {
    it('should start review of an application', async () => {
      const mockResponse = {
        id: 'app-123',
        status: 'UNDER_REVIEW',
        reviewedBy: 'reviewer-1',
      }
      vi.mocked(api.put).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.startReview('app-123', 'reviewer-1')

      expect(api.put).toHaveBeenCalledWith('/api/v1/recruitment/applications/app-123/review', {
        reviewerId: 'reviewer-1',
      })
      expect(result.status).toBe('UNDER_REVIEW')
    })
  })

  describe('approveApplication', () => {
    it('should approve an application', async () => {
      const mockResponse = {
        id: 'app-123',
        status: 'APPROVED',
        reviewedBy: 'reviewer-1',
      }
      vi.mocked(api.put).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.approveApplication('app-123', 'reviewer-1')

      expect(api.put).toHaveBeenCalledWith('/api/v1/recruitment/applications/app-123/approve', {
        reviewerId: 'reviewer-1',
      })
      expect(result.status).toBe('APPROVED')
    })
  })

  describe('rejectApplication', () => {
    it('should reject an application', async () => {
      const mockResponse = {
        id: 'app-123',
        status: 'REJECTED',
        reviewedBy: 'reviewer-1',
      }
      vi.mocked(api.put).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.rejectApplication('app-123', 'reviewer-1')

      expect(api.put).toHaveBeenCalledWith('/api/v1/recruitment/applications/app-123/reject', {
        reviewerId: 'reviewer-1',
      })
      expect(result.status).toBe('REJECTED')
    })
  })

  describe('withdrawApplication', () => {
    it('should withdraw an application', async () => {
      const mockResponse = {
        id: 'app-123',
        status: 'WITHDRAWN',
      }
      vi.mocked(api.put).mockResolvedValue({ data: mockResponse })

      const result = await recruitmentApi.withdrawApplication('app-123')

      expect(api.put).toHaveBeenCalledWith('/api/v1/recruitment/applications/app-123/withdraw')
      expect(result.status).toBe('WITHDRAWN')
    })
  })

  describe('countApplications', () => {
    it('should count applications for a guild', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: { count: 42 } })

      const result = await recruitmentApi.countApplications('guild-1')

      expect(api.get).toHaveBeenCalledWith(
        '/api/v1/recruitment/applications/guilds/guild-1/count',
        { params: {} }
      )
      expect(result).toBe(42)
    })

    it('should count applications with status filter', async () => {
      vi.mocked(api.get).mockResolvedValue({ data: { count: 5 } })

      const result = await recruitmentApi.countApplications('guild-1', 'PENDING')

      expect(api.get).toHaveBeenCalledWith(
        '/api/v1/recruitment/applications/guilds/guild-1/count',
        { params: { status: 'PENDING' } }
      )
      expect(result).toBe(5)
    })
  })
})
