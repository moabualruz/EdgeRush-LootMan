import { api } from './client'
import type { PerformanceMetrics } from '@/types'

export interface WarcraftLogsReport {
  raiderId: number
  characterName: string
  reports: WarcraftLogsEntry[]
}

export interface WarcraftLogsEntry {
  reportId: string
  encounterId: number
  encounterName: string
  difficulty: string
  date: string
  dps?: number
  hps?: number
  ilvl: number
  spec: string
  percentile: number
  deaths: number
}

export const performanceApi = {
  async getMyPerformance(guildId: string): Promise<PerformanceMetrics> {
    const response = await api.get<PerformanceMetrics>(`/v1/performance/guilds/${guildId}/me`)
    return response.data
  },

  async getPerformance(guildId: string, raiderId: number): Promise<PerformanceMetrics> {
    const response = await api.get<PerformanceMetrics>(`/v1/performance/guilds/${guildId}/raiders/${raiderId}`)
    return response.data
  },

  async getWarcraftLogsReports(guildId: string, raiderId: number, limit = 20): Promise<WarcraftLogsReport> {
    const response = await api.get<WarcraftLogsReport>(
      `/v1/warcraftlogs/guilds/${guildId}/raiders/${raiderId}/reports?limit=${limit}`
    )
    return response.data
  },
}
