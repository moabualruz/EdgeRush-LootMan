import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'
import { api } from '@/api/client'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(localStorage.getItem('token'))

  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN' || user.value?.role === 'OFFICER')
  const guildId = computed(() => user.value?.guildId)

  async function fetchUser() {
    if (!token.value) return

    try {
      const response = await api.get<User>('/api/auth/me')
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

  async function loginWithDiscord(code: string): Promise<void> {
    const response = await api.post<{ token: string; user: User }>('/api/auth/discord/callback', { code })
    setToken(response.data.token)
    user.value = response.data.user
  }

  async function loginWithBattlenet(code: string): Promise<void> {
    const response = await api.post<{ token: string; user: User }>('/api/auth/battlenet/callback', { code })
    setToken(response.data.token)
    user.value = response.data.user
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
    loginWithDiscord,
    loginWithBattlenet,
  }
})
