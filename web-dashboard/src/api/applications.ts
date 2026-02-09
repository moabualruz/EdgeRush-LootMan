import { api } from './client'

export interface Application {
  applicationId: number
  appliedAt: string | null
  status: string | null
  role: string | null
  age: number | null
  country: string | null
  battletag: string | null
  discordId: string | null
  mainCharacterName: string | null
  mainCharacterRealm: string | null
  mainCharacterClass: string | null
  mainCharacterRole: string | null
  mainCharacterRace: string | null
  mainCharacterFaction: string | null
  mainCharacterLevel: number | null
  mainCharacterRegion: string | null
  syncedAt: string
  // Extended fields for full application
  timezone?: string | null
  raidAvailability?: string | null
  stableInternet?: boolean | null
  previousGuild?: string | null
  reasonForLeaving?: string | null
  whyThisGuild?: string | null
  whatYouBring?: string | null
  goals?: string | null
  additionalLogs?: string | null
  performanceData?: ApplicationPerformanceData | null
  notes?: ApplicationNote[]
}

export interface ApplicationPerformanceData {
  itemLevel?: number
  mythicPlusScore?: number
  averageParse?: number
  bestParse?: number
  deathsPerPull?: number
  progressionHistory?: string[]
}

export interface ApplicationNote {
  id: number
  authorId: number
  authorName: string
  content: string
  createdAt: string
  isPrivate: boolean
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface UpdateApplicationRequest {
  status?: string
  role?: string
  age?: number
  country?: string
  battletag?: string
  discordId?: string
}

export interface SubmitApplicationRequest {
  name: string
  age: number
  timezone: string
  raidAvailability: string
  stableInternet: boolean
  characterName: string
  realm: string
  region: string
  characterClass: string
  specialization?: string
  previousGuild: string
  reasonForLeaving: string
  additionalLogs?: string
  whyThisGuild: string
  whatYouBring: string
  goals: string
}

export interface AddNoteRequest {
  content: string
  isPrivate?: boolean
}

export const applicationsApi = {
  async getApplications(page = 0, size = 20): Promise<PagedResponse<Application>> {
    const response = await api.get<PagedResponse<Application>>(
      `/applications?page=${page}&size=${size}`
    )
    return response.data
  },

  async getApplication(id: number): Promise<Application> {
    const response = await api.get<Application>(`/applications/${id}`)
    return response.data
  },

  async getApplicationsByStatus(
    status: string,
    page = 0,
    size = 20
  ): Promise<PagedResponse<Application>> {
    const response = await api.get<PagedResponse<Application>>(
      `/applications/status/${status}?page=${page}&size=${size}`
    )
    return response.data
  },

  async updateApplication(id: number, request: UpdateApplicationRequest): Promise<Application> {
    const response = await api.put<Application>(`/applications/${id}`, request)
    return response.data
  },

  async deleteApplication(id: number): Promise<void> {
    await api.delete(`/applications/${id}`)
  },

  // Public submission endpoint
  async submitApplication(request: SubmitApplicationRequest): Promise<Application> {
    const response = await api.post<Application>('/applications', request)
    return response.data
  },

  // Get my application (for applicants to check status)
  async getMyApplication(): Promise<Application | null> {
    try {
      const response = await api.get<Application>('/applications/me')
      return response.data
    } catch {
      return null
    }
  },

  // Officer actions
  async approveApplication(id: number, notes?: string): Promise<Application> {
    const response = await api.post<Application>(`/applications/${id}/approve`, { notes })
    return response.data
  },

  async declineApplication(id: number, reason: string): Promise<Application> {
    const response = await api.post<Application>(`/applications/${id}/decline`, { reason })
    return response.data
  },

  async requestInfo(id: number, message: string): Promise<Application> {
    const response = await api.post<Application>(`/applications/${id}/request-info`, { message })
    return response.data
  },

  // Notes management
  async addNote(applicationId: number, request: AddNoteRequest): Promise<ApplicationNote> {
    const response = await api.post<ApplicationNote>(
      `/applications/${applicationId}/notes`,
      request
    )
    return response.data
  },

  async getNotes(applicationId: number): Promise<ApplicationNote[]> {
    const response = await api.get<ApplicationNote[]>(`/applications/${applicationId}/notes`)
    return response.data
  },
}
