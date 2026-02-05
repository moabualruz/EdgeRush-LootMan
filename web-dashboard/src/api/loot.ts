import { api } from './client'
import type {
  LootHistoryResponse,
  LootAward,
  AwardLootRequest,
  UpdateLootRequest,
  WowItem,
} from '@/types'

export const lootApi = {
  async getMyLootHistory(guildId: string, limit = 20): Promise<LootHistoryResponse> {
    const response = await api.get<LootHistoryResponse>(
      `/v1/loot/guilds/${guildId}/me/history?limit=${limit}`
    )
    return response.data
  },

  async getLootHistory(guildId: string, raiderId: number, limit = 20): Promise<LootHistoryResponse> {
    const response = await api.get<LootHistoryResponse>(
      `/v1/loot/guilds/${guildId}/raiders/${raiderId}/history?limit=${limit}`
    )
    return response.data
  },

  async getGuildLootHistory(guildId: string, limit = 50): Promise<LootAward[]> {
    const response = await api.get<LootAward[]>(`/v1/loot/guilds/${guildId}/history?limit=${limit}`)
    return response.data
  },

  async awardLoot(guildId: string, data: AwardLootRequest): Promise<LootAward> {
    const response = await api.post<LootAward>(
      `/v1/loot/guilds/${guildId}/awards`,
      data
    )
    return response.data
  },

  async updateLoot(awardId: number, data: UpdateLootRequest): Promise<LootAward> {
    const response = await api.patch<LootAward>(
      `/v1/loot/awards/${awardId}`,
      data
    )
    return response.data
  },

  async revokeLoot(awardId: number): Promise<void> {
    await api.delete(`/v1/loot/awards/${awardId}`)
  },

  async searchItems(query: string, limit = 20): Promise<WowItem[]> {
    const response = await api.get<WowItem[]>('/v1/game-data/items/search', {
      params: { q: query, limit },
    })
    return response.data
  },
}
