import { api } from './client'
import type { WishlistItem } from '@/types'

export interface WishlistResponse {
  raiderId: number
  characterName: string
  items: WishlistItem[]
  lastSimulatedAt?: string
}

export interface SimulationStatus {
  raiderId: number
  status: 'IDLE' | 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED'
  progress?: number
  lastRunAt?: string
  nextScheduledAt?: string
  error?: string
  source?: 'LOCAL' | 'RAIDBOTS'
}

export const wishlistApi = {
  async getMyWishlist(guildId: string): Promise<WishlistResponse> {
    const response = await api.get<WishlistResponse>(`/v1/wishlists/guilds/${guildId}/me`)
    return response.data
  },

  async getWishlist(guildId: string, raiderId: number): Promise<WishlistResponse> {
    const response = await api.get<WishlistResponse>(`/v1/wishlists/raider/${raiderId}`)
    return response.data
  },

  async getSimulationStatus(guildId: string, raiderId: number): Promise<SimulationStatus> {
    const response = await api.get<SimulationStatus>(`/v1/simulations/guilds/${guildId}/raiders/${raiderId}/status`)
    return response.data
  },

  async triggerSimulation(guildId: string, raiderId: number): Promise<SimulationStatus> {
    const response = await api.post<SimulationStatus>(`/v1/simulations/guilds/${guildId}/raiders/${raiderId}/run`)
    return response.data
  },
}
