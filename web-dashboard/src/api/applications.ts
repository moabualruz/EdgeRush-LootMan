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

export const applicationsApi = {
  async getApplications(page = 0, size = 20): Promise<PagedResponse<Application>> {
    const response = await api.get<PagedResponse<Application>>(
      `/api/applications?page=${page}&size=${size}`
    )
    return response.data
  },

  async getApplication(id: number): Promise<Application> {
    const response = await api.get<Application>(`/api/applications/${id}`)
    return response.data
  },

  async getApplicationsByStatus(
    status: string,
    page = 0,
    size = 20
  ): Promise<PagedResponse<Application>> {
    const response = await api.get<PagedResponse<Application>>(
      `/api/applications/status/${status}?page=${page}&size=${size}`
    )
    return response.data
  },

  async updateApplication(id: number, request: UpdateApplicationRequest): Promise<Application> {
    const response = await api.put<Application>(`/api/applications/${id}`, request)
    return response.data
  },

  async deleteApplication(id: number): Promise<void> {
    await api.delete(`/api/applications/${id}`)
  },
}
