import { api } from './client'

// === Types ===

export type MarkerType =
  | 'SKULL'
  | 'CROSS'
  | 'SQUARE'
  | 'MOON'
  | 'TRIANGLE'
  | 'DIAMOND'
  | 'CIRCLE'
  | 'STAR'
  | 'TANK'
  | 'HEALER'
  | 'DPS'
  | 'PLAYER'

export type ShapeType = 'CIRCLE' | 'LINE' | 'ARROW' | 'RECTANGLE'

export type PlanVisibility = 'PRIVATE' | 'GUILD' | 'PUBLIC'

// === Response Interfaces ===

export interface PlanMarker {
  type: MarkerType
  x: number
  y: number
  label?: string
  color?: string
}

export interface PlanShape {
  shapeType: ShapeType
  x1: number
  y1: number
  x2?: number
  y2?: number
  radius?: number
  color?: string
  strokeWidth: number
}

export interface PlanStep {
  order: number
  notes?: string
  markers: PlanMarker[]
  shapes: PlanShape[]
}

export interface RaidPlan {
  id: string
  guildId: string
  encounterId: number
  encounterName: string
  name: string
  steps: PlanStep[]
  visibility: PlanVisibility
  shareToken?: string
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface PagedRaidPlans {
  content: RaidPlan[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// === Request Interfaces ===

export interface CreateRaidPlanRequest {
  guildId: string
  encounterId: number
  encounterName: string
  name: string
  createdBy: number
  visibility?: PlanVisibility
}

export interface UpdateRaidPlanRequest {
  name?: string
  visibility?: PlanVisibility
  steps?: PlanStep[]
}

export interface AddStepRequest {
  notes?: string
}

export interface UpdateStepRequest {
  notes?: string
}

export interface AddMarkerRequest {
  type: MarkerType
  x: number
  y: number
  label?: string
  color?: string
}

export interface AddShapeRequest {
  shapeType: ShapeType
  x1: number
  y1: number
  x2?: number
  y2?: number
  radius?: number
  color?: string
  strokeWidth?: number
}

// === API Client ===

export const raidPlanApi = {
  /**
   * Create a new raid plan
   */
  async createPlan(request: CreateRaidPlanRequest): Promise<RaidPlan> {
    const response = await api.post<RaidPlan>('/v1/raid-plans', request)
    return response.data
  },

  /**
   * Get a raid plan by ID
   */
  async getPlan(id: string): Promise<RaidPlan> {
    const response = await api.get<RaidPlan>(`/v1/raid-plans/${id}`)
    return response.data
  },

  /**
   * Get a raid plan by share token (public access)
   */
  async getPlanByShareToken(shareToken: string): Promise<RaidPlan> {
    const response = await api.get<RaidPlan>(`/v1/raid-plans/shared/${shareToken}`)
    return response.data
  },

  /**
   * Get paginated raid plans for a guild
   */
  async getPlansByGuild(
    guildId: string,
    page = 0,
    size?: number
  ): Promise<PagedRaidPlans> {
    const params = new URLSearchParams()
    params.append('page', page.toString())
    if (size) params.append('size', size.toString())

    const response = await api.get<PagedRaidPlans>(
      `/v1/raid-plans/guild/${guildId}?${params.toString()}`
    )
    return response.data
  },

  /**
   * Get raid plans for a specific encounter
   */
  async getPlansByEncounter(guildId: string, encounterId: number): Promise<RaidPlan[]> {
    const response = await api.get<RaidPlan[]>(
      `/v1/raid-plans/guild/${guildId}/encounter/${encounterId}`
    )
    return response.data
  },

  /**
   * Update a raid plan
   */
  async updatePlan(id: string, request: UpdateRaidPlanRequest): Promise<RaidPlan> {
    const response = await api.put<RaidPlan>(`/v1/raid-plans/${id}`, request)
    return response.data
  },

  /**
   * Delete a raid plan
   */
  async deletePlan(id: string): Promise<void> {
    await api.delete(`/v1/raid-plans/${id}`)
  },

  // === Step Management ===

  /**
   * Add a step to a plan
   */
  async addStep(planId: string, request: AddStepRequest): Promise<RaidPlan> {
    const response = await api.post<RaidPlan>(
      `/v1/raid-plans/${planId}/steps`,
      request
    )
    return response.data
  },

  /**
   * Update a step's notes
   */
  async updateStep(
    planId: string,
    stepOrder: number,
    request: UpdateStepRequest
  ): Promise<RaidPlan> {
    const response = await api.put<RaidPlan>(
      `/v1/raid-plans/${planId}/steps/${stepOrder}`,
      request
    )
    return response.data
  },

  /**
   * Remove a step from a plan
   */
  async removeStep(planId: string, stepOrder: number): Promise<RaidPlan> {
    const response = await api.delete<RaidPlan>(
      `/v1/raid-plans/${planId}/steps/${stepOrder}`
    )
    return response.data
  },

  // === Share Token Management ===

  /**
   * Generate a share token for a plan
   */
  async generateShareToken(planId: string): Promise<{ shareToken: string }> {
    const response = await api.post<{ shareToken: string }>(
      `/v1/raid-plans/${planId}/share`
    )
    return response.data
  },

  /**
   * Revoke a plan's share token
   */
  async revokeShareToken(planId: string): Promise<void> {
    await api.delete(`/v1/raid-plans/${planId}/share`)
  },
}
