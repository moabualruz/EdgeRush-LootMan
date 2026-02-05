import { api } from './client'

export interface SyncRun {
  id: number
  source: string
  status: string
  startedAt: string
  completedAt: string | null
  message: string | null
}

export interface PagedSyncRunResponse {
  content: SyncRun[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface SyncLog {
  timestamp: string
  level: 'INFO' | 'WARN' | 'ERROR'
  message: string
}

export const syncApi = {
  async getSyncRuns(page = 0, size = 20): Promise<PagedSyncRunResponse> {
    const response = await api.get<PagedSyncRunResponse>('/api/sync-runs', {
      params: { page, size },
    })
    return response.data
  },

  async getSyncRunsBySource(source: string, page = 0, size = 20): Promise<PagedSyncRunResponse> {
    const response = await api.get<PagedSyncRunResponse>(`/api/sync-runs/source/${source}`, {
      params: { page, size },
    })
    return response.data
  },

  async getSyncRunsByStatus(status: string, page = 0, size = 20): Promise<PagedSyncRunResponse> {
    const response = await api.get<PagedSyncRunResponse>(`/api/sync-runs/status/${status}`, {
      params: { page, size },
    })
    return response.data
  },

  async getSyncRunById(id: number): Promise<SyncRun> {
    const response = await api.get<SyncRun>(`/api/sync-runs/${id}`)
    return response.data
  },

  async getCountBySource(source: string): Promise<{ count: number }> {
    const response = await api.get<{ count: number }>(`/api/sync-runs/source/${source}/count`)
    return response.data
  },

  async triggerSync(source: 'WoWAudit' | 'WarcraftLogs'): Promise<SyncRun> {
    const response = await api.post<SyncRun>(`/api/sync/trigger/${source}`)
    return response.data
  },

  async getSyncLogs(syncRunId: number): Promise<SyncLog[]> {
    const response = await api.get<SyncLog[]>(`/api/sync-runs/${syncRunId}/logs`)
    return response.data
  },
}

