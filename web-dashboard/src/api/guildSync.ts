import { api } from './client'

export interface GuildSyncConfig {
  guildId: string
  guildName: string
  // WoWAudit config
  wowauditGuildUri: string | null
  wowauditBaseUrl: string
  wowauditApiKeyConfigured: boolean
  syncEnabled: boolean
  lastSyncAt: string | null
  lastSyncStatus: string | null
  lastSyncError: string | null
  // Battle.net config
  bnetRealmSlug: string | null
  bnetGuildNameSlug: string | null
  bnetRegion: string
  bnetSyncEnabled: boolean
  bnetLastSyncAt: string | null
  bnetLastSyncStatus: string | null
  bnetLastSyncError: string | null
}

export interface UpdateGuildSyncConfigRequest {
  wowauditGuildUri?: string
  wowauditApiKey?: string
  syncEnabled?: boolean
  bnetRealmSlug?: string
  bnetGuildNameSlug?: string
  bnetRegion?: string
  bnetSyncEnabled?: boolean
}

export interface GuildSyncTriggerResponse {
  success: boolean
  message: string
  result?: {
    created: number
    updated: number
    skipped: number
    total: number
  }
}

/**
 * Fetch sync configuration for a guild.
 */
/**
 * Fetch sync configuration for a guild.
 */
export const fetchGuildSyncConfig = async (guildId: string): Promise<GuildSyncConfig> => {
  const response = await api.get<GuildSyncConfig>(`/v1/guilds/${guildId}/sync/config`)
  return response.data
}

/**
 * Update sync configuration for a guild.
 */
export const updateGuildSyncConfig = async (
  guildId: string,
  config: UpdateGuildSyncConfigRequest
): Promise<GuildSyncConfig> => {
  const response = await api.put<GuildSyncConfig>(`/v1/guilds/${guildId}/sync/config`, config)
  return response.data
}

/**
 * Trigger Battle.net guild roster sync.
 */
export const triggerBnetSync = async (guildId: string): Promise<GuildSyncTriggerResponse> => {
  const response = await api.post<GuildSyncTriggerResponse>(`/v1/guilds/${guildId}/sync/bnet/trigger`)
  return response.data
}

/**
 * Trigger WoWAudit guild roster sync.
 */
export const triggerWowauditSync = async (guildId: string): Promise<GuildSyncTriggerResponse> => {
  const response = await api.post<GuildSyncTriggerResponse>(`/v1/guilds/${guildId}/sync/wowaudit/trigger`)
  return response.data
}

/**
 * Trigger Warcraft Logs guild roster sync.
 */
export const triggerWarcraftLogsSync = async (guildId: string): Promise<GuildSyncTriggerResponse> => {
  const response = await api.post<GuildSyncTriggerResponse>(`/v1/guilds/${guildId}/sync/warcraftlogs/trigger`)
  return response.data
}
