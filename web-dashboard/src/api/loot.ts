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
    const response = await api.get<{ awards: any[] } | any[]>(`/v1/loot/guilds/${guildId}/history?limit=${limit}`)
    // Backend returns { awards: [...] } wrapper with different field names
    const raw = response.data
    const rawAwards: any[] = Array.isArray(raw) ? raw : (raw as any).awards ?? []
    
    // Map backend LootAwardDto fields to frontend LootAward type
    // Backend: id, itemId, raiderId, guildId, awardedAt, flpsScore, tier, isActive
    // Frontend: id, itemId, itemName, raiderId, characterName, awardedAt, flpsAtAward, rdfExpired, rdfExpiresAt, notes
    return rawAwards.map((a: any) => ({
      id: a.id ? Number(a.id) : 0,
      itemId: a.itemId ?? 0,
      itemName: a.itemName ?? `Item #${a.itemId ?? 'Unknown'}`,
      raiderId: a.raiderId ? Number(a.raiderId) : 0,
      characterName: a.characterName ?? a.raiderName ?? `Raider #${a.raiderId ?? '?'}`,
      awardedAt: a.awardedAt ?? new Date().toISOString(),
      flpsAtAward: a.flpsAtAward ?? a.flpsScore ?? 0,
      rdfExpired: a.rdfExpired ?? (a.isActive === false),
      rdfExpiresAt: a.rdfExpiresAt,
      notes: a.notes ?? a.tier ?? undefined,
    }))
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
