import { api } from './client'

export type ApplicationStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN'

export interface CharacterLookupResponse {
  name: string
  realm: string
  region: string
  characterClass: string
  specialization: string | null
  role: string | null
  itemLevel: number | null
  raiderIOScore: number | null
  profileUrl: string
}

export interface CharacterFullLookupResponse extends CharacterLookupResponse {
  bestParseAverage: number | null
  medianParseAverage: number | null
}

export interface SubmitApplicationRequest {
  battleNetId: string
  discordId: string
  email: string
  characterName: string
  characterRealm: string
  characterClass: string
  specialization: string
  itemLevel: number
  raiderIOScore: number | null
  bestParseAverage: number | null
  age: number
  location: string
  timezone: string
  raidDaysAvailable: string[]
  previousGuilds: string
  reasonForLeaving: string
  whyThisGuild: string
}

export interface ApplicationResponse {
  id: string
  guildId: string
  battleNetId: string
  discordId: string
  email: string
  characterName: string
  characterRealm: string
  characterClass: string
  specialization: string
  itemLevel: number
  raiderIOScore: number | null
  bestParseAverage: number | null
  age: number
  location: string
  timezone: string
  raidDaysAvailable: string[]
  previousGuilds: string
  reasonForLeaving: string
  whyThisGuild: string
  status: ApplicationStatus
  reviewedBy: string | null
  reviewedAt: string | null
  createdAt: string
  updatedAt: string
}

export interface GetApplicationsOptions {
  status?: ApplicationStatus
  offset?: number
  limit?: number
}

export const recruitmentApi = {
  /**
   * Fetch character data from Raider.IO only.
   */
  async lookupCharacter(
    region: string,
    realm: string,
    name: string
  ): Promise<CharacterLookupResponse | null> {
    try {
      const response = await api.get<CharacterLookupResponse>(
        '/v1/recruitment/applications/character-lookup',
        { params: { region, realm, name } }
      )
      return response.data
    } catch {
      return null
    }
  },

  /**
   * Fetch comprehensive character data from both Raider.IO and Warcraft Logs.
   */
  async lookupCharacterFull(
    region: string,
    realm: string,
    name: string
  ): Promise<CharacterFullLookupResponse | null> {
    try {
      const response = await api.get<CharacterFullLookupResponse>(
        '/v1/recruitment/applications/character-lookup/full',
        { params: { region, realm, name } }
      )
      return response.data
    } catch {
      return null
    }
  },

  /**
   * Submit a new guild application.
   */
  async submitApplication(
    guildId: string,
    request: SubmitApplicationRequest
  ): Promise<ApplicationResponse> {
    const response = await api.post<ApplicationResponse>(
      `/v1/recruitment/applications/guilds/${guildId}`,
      request
    )
    return response.data
  },

  /**
   * Get applications for a guild with optional filters.
   */
  async getApplicationsByGuild(
    guildId: string,
    options: GetApplicationsOptions = {}
  ): Promise<ApplicationResponse[]> {
    const { status, offset = 0, limit = 50 } = options
    const params: Record<string, unknown> = { offset, limit }
    if (status) {
      params.status = status
    }
    const response = await api.get<ApplicationResponse[]>(
      `/v1/recruitment/applications/guilds/${guildId}`,
      { params }
    )
    return response.data
  },

  /**
   * Get pending applications for a guild.
   */
  async getPendingApplications(
    guildId: string,
    offset = 0,
    limit = 50
  ): Promise<ApplicationResponse[]> {
    const response = await api.get<ApplicationResponse[]>(
      `/v1/recruitment/applications/guilds/${guildId}/pending`,
      { params: { offset, limit } }
    )
    return response.data
  },

  /**
   * Get a single application by ID.
   */
  async getApplicationById(applicationId: string): Promise<ApplicationResponse | null> {
    try {
      const response = await api.get<ApplicationResponse>(
        `/v1/recruitment/applications/${applicationId}`
      )
      return response.data
    } catch {
      return null
    }
  },

  /**
   * Start review of an application.
   */
  async startReview(applicationId: string, reviewerId: string): Promise<ApplicationResponse> {
    const response = await api.put<ApplicationResponse>(
      `/v1/recruitment/applications/${applicationId}/review`,
      { reviewerId }
    )
    return response.data
  },

  /**
   * Approve an application.
   */
  async approveApplication(applicationId: string, reviewerId: string): Promise<ApplicationResponse> {
    const response = await api.put<ApplicationResponse>(
      `/v1/recruitment/applications/${applicationId}/approve`,
      { reviewerId }
    )
    return response.data
  },

  /**
   * Reject an application.
   */
  async rejectApplication(applicationId: string, reviewerId: string): Promise<ApplicationResponse> {
    const response = await api.put<ApplicationResponse>(
      `/v1/recruitment/applications/${applicationId}/reject`,
      { reviewerId }
    )
    return response.data
  },

  /**
   * Withdraw an application.
   */
  async withdrawApplication(applicationId: string): Promise<ApplicationResponse> {
    const response = await api.put<ApplicationResponse>(
      `/v1/recruitment/applications/${applicationId}/withdraw`
    )
    return response.data
  },

  /**
   * Count applications for a guild with optional status filter.
   */
  async countApplications(guildId: string, status?: ApplicationStatus): Promise<number> {
    const params: Record<string, unknown> = {}
    if (status) {
      params.status = status
    }
    const response = await api.get<{ count: number }>(
      `/v1/recruitment/applications/guilds/${guildId}/count`,
      { params }
    )
    return response.data.count
  },
}
