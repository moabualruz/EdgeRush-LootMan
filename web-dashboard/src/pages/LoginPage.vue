<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isLoading = ref(false)
const error = ref<string | null>(null)

const DISCORD_CLIENT_ID = import.meta.env.VITE_DISCORD_CLIENT_ID
const BATTLENET_CLIENT_ID = import.meta.env.VITE_BATTLENET_CLIENT_ID
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI || window.location.origin + '/login'

function loginWithDiscord() {
  const params = new URLSearchParams({
    client_id: DISCORD_CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    response_type: 'code',
    scope: 'identify email',
    state: 'discord',
  })
  window.location.href = `https://discord.com/api/oauth2/authorize?${params.toString()}`
}

function loginWithBattlenet() {
  const params = new URLSearchParams({
    client_id: BATTLENET_CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    response_type: 'code',
    scope: 'openid wow.profile',
    state: 'battlenet',
  })
  window.location.href = `https://oauth.battle.net/authorize?${params.toString()}`
}

// Handle OAuth callback
async function handleCallback() {
  const code = route.query.code as string
  const state = route.query.state as string

  if (!code) return

  isLoading.value = true
  error.value = null

  try {
    if (state === 'discord') {
      await authStore.loginWithDiscord(code)
    } else if (state === 'battlenet') {
      await authStore.loginWithBattlenet(code)
    }
    router.push('/dashboard')
  } catch (e) {
    error.value = 'Authentication failed. Please try again.'
    console.error('Login error:', e)
  } finally {
    isLoading.value = false
  }
}

// Check for callback on mount
if (route.query.code) {
  handleCallback()
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-900 px-4">
    <div class="max-w-md w-full">
      <!-- Logo -->
      <div class="text-center mb-8">
        <h1 class="text-4xl font-bold text-primary-400">LootMan</h1>
        <p class="mt-2 text-gray-400">Fair Loot Priority Score Dashboard</p>
      </div>

      <!-- Login Card -->
      <div class="card">
        <h2 class="text-xl font-semibold text-center mb-6">Sign in to continue</h2>

        <!-- Loading state -->
        <div v-if="isLoading" class="text-center py-8">
          <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full mx-auto"></div>
          <p class="mt-4 text-gray-400">Authenticating...</p>
        </div>

        <!-- Error message -->
        <div v-else-if="error" class="mb-4 p-4 bg-red-900/50 border border-red-700 rounded-md text-red-300">
          {{ error }}
        </div>

        <!-- Login buttons -->
        <div v-else class="space-y-4">
          <button
            @click="loginWithDiscord"
            class="w-full flex items-center justify-center px-4 py-3 rounded-md bg-[#5865F2] hover:bg-[#4752C4] text-white font-medium transition-colors"
          >
            <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24" fill="currentColor">
              <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/>
            </svg>
            Sign in with Discord
          </button>

          <button
            @click="loginWithBattlenet"
            class="w-full flex items-center justify-center px-4 py-3 rounded-md bg-[#148EFF] hover:bg-[#0070E0] text-white font-medium transition-colors"
          >
            <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm0 2c5.523 0 10 4.477 10 10s-4.477 10-10 10S2 17.523 2 12 6.477 2 12 2z"/>
            </svg>
            Sign in with Battle.net
          </button>
        </div>

        <p class="mt-6 text-center text-sm text-gray-500">
          Sign in to view your FLPS score and guild leaderboard
        </p>
      </div>
    </div>
  </div>
</template>
