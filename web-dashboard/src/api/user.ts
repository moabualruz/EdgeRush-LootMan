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

export interface LinkageValidationResult {
  userId: number
  isValid: boolean
  issues: string[]
  orphanedMappingIds: number[]
  unmatchedCharacterIds: number[]
  potentialMatches: number
  hasPrimaryCharacter: boolean
  hasValidPreferences: boolean
}

export interface LinkageRefreshResult {
  userId: number
  orphanedMappingsRemoved: number
  charactersAutoLinked: number
  preferencesFixed: boolean
  primaryCharacterSet: boolean
  issues: string[]
  summary: string
}

export const validateUserLinkages = async (): Promise<LinkageValidationResult> => {
  const response = await api.get<LinkageValidationResult>('/v1/user/linkage/validate')
  return response.data
}

export const refreshUserLinkages = async (): Promise<LinkageRefreshResult> => {
  const response = await api.post<LinkageRefreshResult>('/v1/user/linkage/refresh')
  return response.data
}
