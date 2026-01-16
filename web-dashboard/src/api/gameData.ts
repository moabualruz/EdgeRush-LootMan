import { api } from './client'

export interface BlizzardRaid {
  id: number
  name: string
}

export interface BlizzardMap {
  id: number
  name: string
  description?: string
}

export const gameDataApi = {
  async getRaids(): Promise<BlizzardRaid[]> {
    const response = await api.get<BlizzardRaid[]>('/api/v1/game-data/raids')
    return response.data
  },

  async getRaidMaps(instanceId: number): Promise<BlizzardMap[]> {
    const response = await api.get<BlizzardMap[]>(`/api/v1/game-data/raids/${instanceId}/maps`)
    return response.data
  }
}
