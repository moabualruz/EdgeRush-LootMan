import { api } from './client'
import type { FlpsScore, FlpsReport, LeaderboardResponse, FlpsConfig } from '@/types'

export const flpsApi = {
  async getMyFlps(guildId: string): Promise<FlpsScore> {
    const response = await api.get<FlpsScore>(`/api/v1/flps/guilds/${guildId}/me`)
    return response.data
  },

  async getFlpsReport(guildId: string): Promise<FlpsReport> {
    const response = await api.get<FlpsReport>(`/api/v1/flps/guilds/${guildId}/report`)
    return response.data
  },

  async getLeaderboard(guildId: string, role?: string, limit = 50): Promise<LeaderboardResponse> {
    const params = new URLSearchParams()
    if (role) params.append('role', role)
    params.append('limit', limit.toString())

    const response = await api.get<LeaderboardResponse>(
      `/api/v1/flps/guilds/${guildId}/leaderboard?${params.toString()}`
    )
    return response.data
  },

  async getConfig(guildId: string): Promise<FlpsConfig> {
    const response = await api.get<FlpsConfig>(`/api/v1/flps/guilds/${guildId}/config`)
    return response.data
  },

  async previewConfig(guildId: string, config: Partial<FlpsConfig>) {
    const response = await api.post(`/api/v1/flps/guilds/${guildId}/config/preview`, config)
    return response.data
  },

  async updateConfig(guildId: string, config: Partial<FlpsConfig>): Promise<FlpsConfig> {
    const response = await api.put<FlpsConfig>(`/api/v1/flps/guilds/${guildId}/config`, config)
    return response.data
  },
}
