import { describe, it, expect, vi, beforeEach } from 'vitest'
import { cooldownsApi } from './cooldowns'
import { api } from './client'

vi.mock('./client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('cooldownsApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getCooldownPlan', () => {
    it('should fetch cooldown plan by ID', async () => {
      const mockPlan = {
        id: 'plan-1',
        guildId: 'test-guild',
        encounterId: 2902,
        encounterName: 'Queen Ansurek',
        assignments: [],
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockPlan })

      const result = await cooldownsApi.getCooldownPlan('plan-1')

      expect(api.get).toHaveBeenCalledWith('/api/v1/cooldown-plans/plan-1')
      expect(result).toEqual(mockPlan)
    })
  })

  describe('getCooldownPlansByGuild', () => {
    it('should fetch paginated cooldown plans for guild', async () => {
      const mockResponse = {
        content: [],
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      const result = await cooldownsApi.getCooldownPlansByGuild('test-guild')

      expect(api.get).toHaveBeenCalledWith('/api/v1/cooldown-plans/guild/test-guild?page=0')
      expect(result).toEqual(mockResponse)
    })

    it('should include page and size parameters', async () => {
      const mockResponse = { content: [], page: 1, size: 10, totalElements: 15, totalPages: 2 }
      vi.mocked(api.get).mockResolvedValue({ data: mockResponse })

      await cooldownsApi.getCooldownPlansByGuild('test-guild', 1, 10)

      expect(api.get).toHaveBeenCalledWith('/api/v1/cooldown-plans/guild/test-guild?page=1&size=10')
    })
  })

  describe('getCooldownPlansByEncounter', () => {
    it('should fetch cooldown plans for a specific encounter', async () => {
      const mockPlans = [{ id: 'plan-1' }]
      vi.mocked(api.get).mockResolvedValue({ data: mockPlans })

      const result = await cooldownsApi.getCooldownPlansByEncounter('test-guild', 2902)

      expect(api.get).toHaveBeenCalledWith('/api/v1/cooldown-plans/guild/test-guild/encounter/2902')
      expect(result).toEqual(mockPlans)
    })
  })

  describe('createCooldownPlan', () => {
    it('should create a new cooldown plan', async () => {
      const request = {
        guildId: 'test-guild',
        encounterId: 2902,
        encounterName: 'Queen Ansurek',
        name: 'Test Plan',
      }
      const mockPlan = { id: 'new-plan', ...request, assignments: [] }
      vi.mocked(api.post).mockResolvedValue({ data: mockPlan })

      const result = await cooldownsApi.createCooldownPlan(request)

      expect(api.post).toHaveBeenCalledWith('/api/v1/cooldown-plans', request)
      expect(result).toEqual(mockPlan)
    })
  })

  describe('updateCooldownPlan', () => {
    it('should update a cooldown plan', async () => {
      const request = { name: 'Updated Name' }
      const mockPlan = { id: 'plan-1', name: 'Updated Name' }
      vi.mocked(api.put).mockResolvedValue({ data: mockPlan })

      const result = await cooldownsApi.updateCooldownPlan('plan-1', request)

      expect(api.put).toHaveBeenCalledWith('/api/v1/cooldown-plans/plan-1', request)
      expect(result).toEqual(mockPlan)
    })
  })

  describe('deleteCooldownPlan', () => {
    it('should delete a cooldown plan', async () => {
      vi.mocked(api.delete).mockResolvedValue({})

      await cooldownsApi.deleteCooldownPlan('plan-1')

      expect(api.delete).toHaveBeenCalledWith('/api/v1/cooldown-plans/plan-1')
    })
  })

  describe('addAssignment', () => {
    it('should add a cooldown assignment', async () => {
      const assignment = {
        playerId: 1,
        playerName: 'Healbot',
        cooldownId: 'divine-hymn',
        cooldownName: 'Divine Hymn',
        abilityId: 'ability-1',
        abilityName: 'Silken Tomb',
        time: 25,
      }
      const mockPlan = { id: 'plan-1', assignments: [assignment] }
      vi.mocked(api.post).mockResolvedValue({ data: mockPlan })

      const result = await cooldownsApi.addAssignment('plan-1', assignment)

      expect(api.post).toHaveBeenCalledWith('/api/v1/cooldown-plans/plan-1/assignments', assignment)
      expect(result).toEqual(mockPlan)
    })
  })

  describe('removeAssignment', () => {
    it('should remove a cooldown assignment', async () => {
      const mockPlan = { id: 'plan-1', assignments: [] }
      vi.mocked(api.delete).mockResolvedValue({ data: mockPlan })

      const result = await cooldownsApi.removeAssignment('plan-1', 'assignment-1')

      expect(api.delete).toHaveBeenCalledWith('/api/v1/cooldown-plans/plan-1/assignments/assignment-1')
      expect(result).toEqual(mockPlan)
    })
  })

  describe('exportToMRT', () => {
    it('should export plan to MRT note format', async () => {
      const mockNote = '|cFFFFFF00Divine Hymn|r - 0:25'
      vi.mocked(api.get).mockResolvedValue({ data: { note: mockNote } })

      const result = await cooldownsApi.exportToMRT('plan-1')

      expect(api.get).toHaveBeenCalledWith('/api/v1/cooldown-plans/plan-1/export/mrt')
      expect(result).toBe(mockNote)
    })
  })

  describe('exportToWeakAura', () => {
    it('should export plan to WeakAura format', async () => {
      const mockData = '!WA:2!...'
      vi.mocked(api.get).mockResolvedValue({ data: { data: mockData } })

      const result = await cooldownsApi.exportToWeakAura('plan-1')

      expect(api.get).toHaveBeenCalledWith('/api/v1/cooldown-plans/plan-1/export/weakaura')
      expect(result).toBe(mockData)
    })
  })

  describe('getAvailableCooldowns', () => {
    it('should fetch available cooldowns by class', async () => {
      const mockCooldowns = {
        PRIEST: [
          { id: 'divine-hymn', name: 'Divine Hymn', spellId: 64843, duration: 8, cooldownTime: 180 },
        ],
      }
      vi.mocked(api.get).mockResolvedValue({ data: mockCooldowns })

      const result = await cooldownsApi.getAvailableCooldowns()

      expect(api.get).toHaveBeenCalledWith('/api/v1/cooldowns/available')
      expect(result).toEqual(mockCooldowns)
    })
  })

  describe('getBossAbilities', () => {
    it('should fetch boss abilities for an encounter', async () => {
      const mockAbilities = [
        { id: 'ability-1', name: 'Silken Tomb', time: 25, damage: 'HIGH', requiresCooldown: true },
      ]
      vi.mocked(api.get).mockResolvedValue({ data: mockAbilities })

      const result = await cooldownsApi.getBossAbilities(2902)

      expect(api.get).toHaveBeenCalledWith('/api/v1/cooldowns/encounters/2902/abilities')
      expect(result).toEqual(mockAbilities)
    })
  })
})
