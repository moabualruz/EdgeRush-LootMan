import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { api } from '@/api/client'

interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN' || user.value?.role === 'OFFICER')
  const guildId = computed(() => user.value?.guildId)
  const currentGuildId = guildId // Alias for backward compatibility

  async function fetchUser() {
    if (!token.value) return

    try {
      const response = await api.get<User>('/v1/auth/me')
      user.value = response.data
    } catch {
      // Don't logout immediately on fetch failure, let interceptor handle 401
      // But if it's a hard failure (e.g. 404), maybe we should.
      // For now, assume interceptor handles auth failures.
    }
  }

  function setTokens(newToken: string, newRefreshToken: string) {
    token.value = newToken
    refreshToken.value = newRefreshToken
    localStorage.setItem('token', newToken)
    localStorage.setItem('refreshToken', newRefreshToken)
  }

  function logout() {
    // Optional: Call backend logout
    if (token.value) {
        api.post('/v1/auth/logout').catch(() => {})
    }
    
    user.value = null
    token.value = null
    refreshToken.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  async function register(username: string, email: string, password: string, role: string = 'RAIDER'): Promise<void> {
    const response = await api.post<TokenResponse>('/v1/auth/register', {
      username,
      email,
      password,
      role,
    })
    setTokens(response.data.accessToken, response.data.refreshToken)
    await fetchUser()
  }

  async function loginLocal(usernameOrEmail: string, password: string): Promise<void> {
    const response = await api.post<TokenResponse>('/v1/auth/login', {
      usernameOrEmail,
      password,
    })
    setTokens(response.data.accessToken, response.data.refreshToken)
    await fetchUser()
  }

  async function loginWithDiscord(code: string): Promise<void> {
    const response = await api.post<TokenResponse>('/v1/auth/discord/callback', { code })
    setTokens(response.data.accessToken, response.data.refreshToken)
    await fetchUser()
  }

  async function getBattlenetAuthUrl(): Promise<string> {
    const response = await api.get<{ url: string }>('/v1/auth/battlenet/url')
    return response.data.url
  }

  async function getDiscordAuthUrl(): Promise<string> {
    const response = await api.get<{ url: string }>('/v1/auth/discord/url')
    return response.data.url
  }

  async function loginWithBattlenet(code: string): Promise<void> {
    const response = await api.post<TokenResponse>('/v1/auth/battlenet/callback', { code })
    setTokens(response.data.accessToken, response.data.refreshToken)
    await fetchUser()
  }

  async function linkDiscord(code: string): Promise<void> {
    const response = await api.post<User>('/v1/auth/link/discord', { code })
    user.value = response.data
  }

  async function linkBattlenet(code: string): Promise<void> {
    const response = await api.post<User>('/v1/auth/link/battlenet', { code })
    user.value = response.data
  }

  // Initialize: fetch user if token exists
  if (token.value) {
    fetchUser()
  }

  return {
    user,
    token,
    refreshToken,
    isAuthenticated,
    isAdmin,
    guildId,
    currentGuildId,
    fetchUser,
    setTokens,
    logout,
    register,
    loginLocal,
    loginWithDiscord,
    loginWithBattlenet,
    getDiscordAuthUrl,
    getBattlenetAuthUrl,
    linkDiscord,
    linkBattlenet,
  }
})
