import { api } from './client'
import type { BehavioralAction, LootBan } from '@/types'

export const adminApi = {
  // Behavioral Actions
  async getBehavioralActions(guildId: string): Promise<BehavioralAction[]> {
    const response = await api.get<BehavioralAction[]>(`/api/v1/admin/guilds/${guildId}/behavioral-actions`)
    return response.data
  },

  async createBehavioralAction(
    guildId: string,
    action: Omit<BehavioralAction, 'id' | 'createdBy' | 'active'>
  ): Promise<BehavioralAction> {
    const response = await api.post<BehavioralAction>(
      `/api/v1/admin/guilds/${guildId}/behavioral-actions`,
      action
    )
    return response.data
  },

  async updateBehavioralAction(
    guildId: string,
    actionId: number,
    action: Partial<BehavioralAction>
  ): Promise<BehavioralAction> {
    const response = await api.put<BehavioralAction>(
      `/api/v1/admin/guilds/${guildId}/behavioral-actions/${actionId}`,
      action
    )
    return response.data
  },

  async deleteBehavioralAction(guildId: string, actionId: number): Promise<void> {
    await api.delete(`/api/v1/admin/guilds/${guildId}/behavioral-actions/${actionId}`)
  },

  // Loot Bans
  async getLootBans(guildId: string): Promise<LootBan[]> {
    const response = await api.get<LootBan[]>(`/api/v1/admin/guilds/${guildId}/loot-bans`)
    return response.data
  },

  async createLootBan(guildId: string, ban: Omit<LootBan, 'id' | 'createdBy' | 'active'>): Promise<LootBan> {
    const response = await api.post<LootBan>(`/api/v1/admin/guilds/${guildId}/loot-bans`, ban)
    return response.data
  },

  async updateLootBan(guildId: string, banId: number, ban: Partial<LootBan>): Promise<LootBan> {
    const response = await api.put<LootBan>(`/api/v1/admin/guilds/${guildId}/loot-bans/${banId}`, ban)
    return response.data
  },

  async deleteLootBan(guildId: string, banId: number): Promise<void> {
    await api.delete(`/api/v1/admin/guilds/${guildId}/loot-bans/${banId}`)
  },
}
