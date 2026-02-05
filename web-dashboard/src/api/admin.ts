import { api } from './client'
import type { BehavioralAction, LootBan } from '@/types'

export const adminApi = {
  // Behavioral Actions
  async getBehavioralActions(guildId: string): Promise<BehavioralAction[]> {
    const response = await api.get<BehavioralAction[]>(`/v1/admin/guilds/${guildId}/behavioral-actions`)
    return response.data
  },

  async createBehavioralAction(
    guildId: string,
    action: Omit<BehavioralAction, 'id' | 'createdBy' | 'active'>
  ): Promise<BehavioralAction> {
    const response = await api.post<BehavioralAction>(
      `/v1/admin/guilds/${guildId}/behavioral-actions`,
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
      `/v1/admin/guilds/${guildId}/behavioral-actions/${actionId}`,
      action
    )
    return response.data
  },

  async deleteBehavioralAction(guildId: string, actionId: number): Promise<void> {
    await api.delete(`/v1/admin/guilds/${guildId}/behavioral-actions/${actionId}`)
  },

  // Loot Bans
  async getLootBans(guildId: string): Promise<LootBan[]> {
    const response = await api.get<LootBan[]>(`/v1/admin/guilds/${guildId}/loot-bans`)
    return response.data
  },

  async createLootBan(guildId: string, ban: Omit<LootBan, 'id' | 'createdBy' | 'active'>): Promise<LootBan> {
    const response = await api.post<LootBan>(`/v1/admin/guilds/${guildId}/loot-bans`, ban)
    return response.data
  },

  async updateLootBan(guildId: string, banId: number, ban: Partial<LootBan>): Promise<LootBan> {
    const response = await api.put<LootBan>(`/v1/admin/guilds/${guildId}/loot-bans/${banId}`, ban)
    return response.data
  },

  async deleteLootBan(guildId: string, banId: number): Promise<void> {
    await api.delete(`/v1/admin/guilds/${guildId}/loot-bans/${banId}`)
  },

  // Raider Management
  async updateRaider(
    guildId: string,
    raiderId: number,
    data: { rank?: string; isActive?: boolean }
  ): Promise<{ raiderId: number; rank: string; isActive: boolean }> {
    const response = await api.patch<{ raiderId: number; rank: string; isActive: boolean }>(
      `/v1/admin/guilds/${guildId}/raiders/${raiderId}`,
      data
    )
    return response.data
  },
}
