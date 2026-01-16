import { api } from './client'
import type { GuildContext, GuildPermission, PermissionTypeInfo } from '@/types'

/**
 * Fetch all guild contexts for the current user.
 * Returns a list of guilds the user has characters in.
 */
export async function fetchUserGuilds(): Promise<GuildContext[]> {
  const response = await api.get<GuildContext[]>('/v1/user/guilds')
  return response.data
}

/**
 * Fetch the active guild context for the current user.
 * Returns null if no active character is set.
 */
export async function fetchActiveGuildContext(): Promise<GuildContext | null> {
  const response = await api.get<GuildContext | null>('/v1/user/guilds/active')
  return response.data
}

/**
 * Set the active character for the current user.
 */
export async function setActiveCharacter(characterMappingId: number): Promise<GuildContext> {
  const response = await api.put<GuildContext>('/v1/user/guilds/active', { characterMappingId })
  return response.data
}

/**
 * Fetch guild permissions configuration.
 */
export async function fetchGuildPermissions(guildId: string): Promise<GuildPermission[]> {
  const response = await api.get<GuildPermission[]>(`/v1/guilds/${guildId}/permissions`)
  return response.data
}

/**
 * Add a permission for a rank in a guild.
 */
export async function addGuildPermission(
  guildId: string,
  rankName: string,
  permissionType: string
): Promise<GuildPermission> {
  const response = await api.post<GuildPermission>(`/v1/guilds/${guildId}/permissions`, {
    rankName,
    permissionType,
  })
  return response.data
}

/**
 * Remove a guild permission.
 */
export async function removeGuildPermission(guildId: string, permissionId: number): Promise<void> {
  await api.delete(`/v1/guilds/${guildId}/permissions/${permissionId}`)
}

/**
 * Fetch available permission types.
 */
export async function fetchPermissionTypes(): Promise<PermissionTypeInfo[]> {
  const response = await api.get<PermissionTypeInfo[]>('/v1/guilds/default/permissions/types')
  return response.data
}

/**
 * Fetch distinct rank names with permissions in a guild.
 */
export async function fetchRanksWithPermissions(guildId: string): Promise<string[]> {
  const response = await api.get<string[]>(`/v1/guilds/${guildId}/permissions/ranks`)
  return response.data
}
