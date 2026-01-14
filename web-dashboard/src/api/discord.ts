import { api } from './client'

export interface DiscordNotificationConfig {
  id: number
  guildId: string
  discordServerId: string
  notificationType: string
  channelId: string
  enabled: boolean
  mentionRoleId: string | null
  createdAt: string
  updatedAt: string | null
}

export interface GuildNotificationConfigsResponse {
  guildId: string
  configs: DiscordNotificationConfig[]
  availableTypes: string[]
}

export interface UpsertNotificationConfigRequest {
  discordServerId: string
  notificationType: string
  channelId: string
  enabled?: boolean
  mentionRoleId?: string | null
}

export interface UpdateNotificationConfigRequest {
  channelId?: string
  enabled?: boolean
  mentionRoleId?: string | null
}

export interface TestNotificationResponse {
  success: boolean
  message: string
}

export const discordApi = {
  async getConfigs(guildId: string): Promise<GuildNotificationConfigsResponse> {
    const response = await api.get<GuildNotificationConfigsResponse>(
      `/api/v1/guilds/${guildId}/discord/config`
    )
    return response.data
  },

  async getConfigByType(
    guildId: string,
    type: string
  ): Promise<DiscordNotificationConfig | null> {
    try {
      const response = await api.get<DiscordNotificationConfig>(
        `/api/v1/guilds/${guildId}/discord/config/${type}`
      )
      return response.data
    } catch (error: unknown) {
      // 204 No Content means no config exists
      if ((error as { response?: { status?: number } }).response?.status === 204) {
        return null
      }
      throw error
    }
  },

  async upsertConfig(
    guildId: string,
    request: UpsertNotificationConfigRequest
  ): Promise<DiscordNotificationConfig> {
    const response = await api.put<DiscordNotificationConfig>(
      `/api/v1/guilds/${guildId}/discord/config`,
      request
    )
    return response.data
  },

  async updateConfig(
    guildId: string,
    configId: number,
    request: UpdateNotificationConfigRequest
  ): Promise<DiscordNotificationConfig> {
    const response = await api.patch<DiscordNotificationConfig>(
      `/api/v1/guilds/${guildId}/discord/config/${configId}`,
      request
    )
    return response.data
  },

  async deleteConfig(guildId: string, configId: number): Promise<void> {
    await api.delete(`/api/v1/guilds/${guildId}/discord/config/${configId}`)
  },

  async testNotification(guildId: string, type: string): Promise<TestNotificationResponse> {
    const response = await api.post<TestNotificationResponse>(
      `/api/v1/guilds/${guildId}/discord/config/test/${type}`
    )
    return response.data
  },
}
