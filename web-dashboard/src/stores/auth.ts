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

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN' || user.value?.role === 'OFFICER')
  const guildId = computed(() => user.value?.guildId)

  async function fetchUser() {
    if (!token.value) return

    try {
      const response = await api.get<User>('/api/v1/auth/me')
      user.value = response.data
    } catch {
      logout()
    }
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function logout() {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
  }

  async function register(username: string, email: string, password: string): Promise<void> {
    const response = await api.post<TokenResponse>('/api/v1/auth/register', {
      username,
      email,
      password,
    })
    setToken(response.data.accessToken)
    await fetchUser()
  }

  async function loginLocal(usernameOrEmail: string, password: string): Promise<void> {
    const response = await api.post<TokenResponse>('/api/v1/auth/login', {
      usernameOrEmail,
      password,
    })
    setToken(response.data.accessToken)
    await fetchUser()
  }

  async function loginWithDiscord(code: string): Promise<void> {
    const response = await api.post<TokenResponse>('/api/v1/auth/discord/callback', { code })
    setToken(response.data.accessToken)
    await fetchUser()
  }

  async function loginWithBattlenet(code: string): Promise<void> {
    const response = await api.post<TokenResponse>('/api/v1/auth/battlenet/callback', { code })
    setToken(response.data.accessToken)
    await fetchUser()
  }

  async function linkDiscord(code: string): Promise<void> {
    const response = await api.post<User>('/api/v1/auth/link/discord', { code })
    user.value = response.data
  }

  async function linkBattlenet(code: string): Promise<void> {
    const response = await api.post<User>('/api/v1/auth/link/battlenet', { code })
    user.value = response.data
  }

  // Initialize: fetch user if token exists
  if (token.value) {
    fetchUser()
  }

  return {
    user,
    token,
    isAuthenticated,
    isAdmin,
    guildId,
    fetchUser,
    setToken,
    logout,
    register,
    loginLocal,
    loginWithDiscord,
    loginWithBattlenet,
    linkDiscord,
    linkBattlenet,
  }
})
