import { api } from './client'

export interface UserCharacter {
  id: number
  userId: number
  name: string
  realm: string
  className: string  // Class name from Blizzard API (e.g., "Death Knight", "Mage")
  classId: number | null
  specId: number | null
  level: number
  race: string
  faction: string
  blizzardId: number | null
  lastSyncedAt: string
}

export const fetchUserCharacters = async (): Promise<UserCharacter[]> => {
  const response = await api.get<UserCharacter[]>('/v1/user/characters')
  return response.data
}
