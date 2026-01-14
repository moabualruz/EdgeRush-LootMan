import { api } from './client'
import type { LootHistoryResponse, LootAward } from '@/types'

export const lootApi = {
  async getMyLootHistory(guildId: string, limit = 20): Promise<LootHistoryResponse> {
    const response = await api.get<LootHistoryResponse>(
      `/api/v1/loot/guilds/${guildId}/me/history?limit=${limit}`
    )
    return response.data
  },

  async getLootHistory(guildId: string, raiderId: number, limit = 20): Promise<LootHistoryResponse> {
    const response = await api.get<LootHistoryResponse>(
      `/api/v1/loot/guilds/${guildId}/raiders/${raiderId}/history?limit=${limit}`
    )
    return response.data
  },

  async getGuildLootHistory(guildId: string, limit = 50): Promise<LootAward[]> {
    const response = await api.get<LootAward[]>(`/api/v1/loot/guilds/${guildId}/history?limit=${limit}`)
    return response.data
  },
}
