import { api } from './client'
import type { Role } from '@/types'

export interface Raid {
  id: number
  teamId: number
  teamName: string
  instanceName: string
  difficulty: 'NORMAL' | 'HEROIC' | 'MYTHIC'
  scheduledAt: string
  endedAt?: string
  status: RaidStatus
  signupCount: number
  maxPlayers: number
  description?: string
}

export type RaidStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export interface RaidEncounter {
  id: number
  raidId: number
  encounterId: number
  encounterName: string
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'KILLED' | 'WIPED'
  pullCount: number
  killedAt?: string
  duration?: number
}

export interface RaidSignup {
  id: number
  raidId: number
  raiderId: number
  characterName: string
  role: Role
  status: SignupStatus
  signedUpAt: string
  notes?: string
}

export type SignupStatus = 'CONFIRMED' | 'TENTATIVE' | 'DECLINED' | 'STANDBY'

export interface RaidDetail extends Raid {
  encounters: RaidEncounter[]
  signups: RaidSignup[]
}

export interface CreateRaidSignup {
  raiderId: number
  role: Role
  status: SignupStatus
  notes?: string
}

export const raidsApi = {
  async getRaids(teamId?: number): Promise<Raid[]> {
    const params = teamId ? `?teamId=${teamId}` : ''
    const response = await api.get<Raid[]>(`/v1/raids${params}`)
    return response.data
  },

  async getUpcomingRaids(guildId: string, limit = 10): Promise<Raid[]> {
    const response = await api.get<Raid[]>(`/v1/raids/guilds/${guildId}/upcoming?limit=${limit}`)
    return response.data
  },

  async getPastRaids(guildId: string, limit = 10): Promise<Raid[]> {
    const response = await api.get<Raid[]>(`/v1/raids/guilds/${guildId}/past?limit=${limit}`)
    return response.data
  },

  async getRaidById(raidId: number): Promise<RaidDetail> {
    const response = await api.get<RaidDetail>(`/v1/raids/${raidId}`)
    return response.data
  },

  async getRaidEncounters(raidId: number): Promise<RaidEncounter[]> {
    const response = await api.get<{ content: RaidEncounter[] }>(`/v1/raid-encounters/raid/${raidId}`)
    return response.data.content
  },

  async getRaidSignups(raidId: number): Promise<RaidSignup[]> {
    const response = await api.get<{ content: RaidSignup[] }>(`/v1/raid-signups/raid/${raidId}`)
    return response.data.content
  },

  async createSignup(raidId: number, signup: CreateRaidSignup): Promise<RaidSignup> {
    const response = await api.post<RaidSignup>(`/v1/raid-signups`, {
      raidId,
      ...signup,
    })
    return response.data
  },

  async updateSignup(signupId: number, update: Partial<CreateRaidSignup>): Promise<RaidSignup> {
    const response = await api.put<RaidSignup>(`/v1/raid-signups/${signupId}`, update)
    return response.data
  },

  async deleteSignup(signupId: number): Promise<void> {
    await api.delete(`/v1/raid-signups/${signupId}`)
  },
}
