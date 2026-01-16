import { api } from './client'

// === Types ===

export type DamageLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'

// === Response Interfaces ===

export interface Cooldown {
  id: string
  name: string
  spellId: number
  duration: number
  cooldownTime: number
  icon?: string
}

export interface BossAbility {
  id: string
  name: string
  time: number
  damage: DamageLevel
  requiresCooldown: boolean
}

export interface CooldownAssignment {
  id?: string
  playerId: number
  playerName: string
  cooldownId: string
  cooldownName: string
  abilityId: string
  abilityName: string
  time: number
}

export interface CooldownPlan {
  id: string
  guildId: string
  encounterId: number
  encounterName: string
  name: string
  assignments: CooldownAssignment[]
  createdBy?: number
  createdAt?: string
  updatedAt?: string
}

export interface PagedCooldownPlans {
  content: CooldownPlan[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// === Request Interfaces ===

export interface CreateCooldownPlanRequest {
  guildId: string
  encounterId: number
  encounterName: string
  name: string
}

export interface UpdateCooldownPlanRequest {
  name?: string
}

export interface AddAssignmentRequest {
  playerId: number
  playerName: string
  cooldownId: string
  cooldownName: string
  abilityId: string
  abilityName: string
  time: number
}

// === API Client ===

export const cooldownsApi = {
  /**
   * Get a cooldown plan by ID
   */
  async getCooldownPlan(id: string): Promise<CooldownPlan> {
    const response = await api.get<CooldownPlan>(`/v1/cooldown-plans/${id}`)
    return response.data
  },

  /**
   * Get paginated cooldown plans for a guild
   */
  async getCooldownPlansByGuild(
    guildId: string,
    page = 0,
    size?: number
  ): Promise<PagedCooldownPlans> {
    const params = new URLSearchParams()
    params.append('page', page.toString())
    if (size) params.append('size', size.toString())

    const response = await api.get<PagedCooldownPlans>(
      `/v1/cooldown-plans/guild/${guildId}?${params.toString()}`
    )
    return response.data
  },

  /**
   * Get cooldown plans for a specific encounter
   */
  async getCooldownPlansByEncounter(
    guildId: string,
    encounterId: number
  ): Promise<CooldownPlan[]> {
    const response = await api.get<CooldownPlan[]>(
      `/v1/cooldown-plans/guild/${guildId}/encounter/${encounterId}`
    )
    return response.data
  },

  /**
   * Create a new cooldown plan
   */
  async createCooldownPlan(request: CreateCooldownPlanRequest): Promise<CooldownPlan> {
    const response = await api.post<CooldownPlan>('/v1/cooldown-plans', request)
    return response.data
  },

  /**
   * Update a cooldown plan
   */
  async updateCooldownPlan(
    id: string,
    request: UpdateCooldownPlanRequest
  ): Promise<CooldownPlan> {
    const response = await api.put<CooldownPlan>(`/v1/cooldown-plans/${id}`, request)
    return response.data
  },

  /**
   * Delete a cooldown plan
   */
  async deleteCooldownPlan(id: string): Promise<void> {
    await api.delete(`/v1/cooldown-plans/${id}`)
  },

  // === Assignment Management ===

  /**
   * Add an assignment to a cooldown plan
   */
  async addAssignment(
    planId: string,
    assignment: AddAssignmentRequest
  ): Promise<CooldownPlan> {
    const response = await api.post<CooldownPlan>(
      `/v1/cooldown-plans/${planId}/assignments`,
      assignment
    )
    return response.data
  },

  /**
   * Remove an assignment from a cooldown plan
   */
  async removeAssignment(planId: string, assignmentId: string): Promise<CooldownPlan> {
    const response = await api.delete<CooldownPlan>(
      `/v1/cooldown-plans/${planId}/assignments/${assignmentId}`
    )
    return response.data
  },

  // === Export Functions ===

  /**
   * Export cooldown plan to MRT note format
   */
  async exportToMRT(planId: string): Promise<string> {
    const response = await api.get<{ note: string }>(
      `/v1/cooldown-plans/${planId}/export/mrt`
    )
    return response.data.note
  },

  /**
   * Export cooldown plan to WeakAura format
   */
  async exportToWeakAura(planId: string): Promise<string> {
    const response = await api.get<{ data: string }>(
      `/v1/cooldown-plans/${planId}/export/weakaura`
    )
    return response.data.data
  },

  // === Reference Data ===

  /**
   * Get available cooldowns grouped by class
   */
  async getAvailableCooldowns(): Promise<Record<string, Cooldown[]>> {
    const response = await api.get<Record<string, Cooldown[]>>('/v1/cooldowns/available')
    return response.data
  },

  /**
   * Get boss abilities for a specific encounter
   */
  async getBossAbilities(encounterId: number): Promise<BossAbility[]> {
    const response = await api.get<BossAbility[]>(
      `/v1/cooldowns/encounters/${encounterId}/abilities`
    )
    return response.data
  },
}
