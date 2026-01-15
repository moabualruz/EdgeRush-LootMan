<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isLoading = ref(false)
const error = ref<string | null>(null)
const mode = ref<'login' | 'register'>('login')

// Form fields
const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')

const DISCORD_CLIENT_ID = import.meta.env.VITE_DISCORD_CLIENT_ID
const BATTLENET_CLIENT_ID = import.meta.env.VITE_BATTLENET_CLIENT_ID
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI || window.location.origin + '/login'

const isFormValid = computed(() => {
  if (mode.value === 'login') {
    return username.value.length >= 3 && password.value.length >= 6
  } else {
    return (
      username.value.length >= 3 &&
      email.value.includes('@') &&
      password.value.length >= 6 &&
      password.value === confirmPassword.value
    )
  }
})

function toggleMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  error.value = null
}

async function handleSubmit() {
  if (!isFormValid.value) return

  isLoading.value = true
  error.value = null

  try {
    if (mode.value === 'login') {
      await authStore.loginLocal(username.value, password.value)
    } else {
      await authStore.register(username.value, email.value, password.value)
    }
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || 'Authentication failed. Please try again.'
    console.error('Auth error:', e)
  } finally {
    isLoading.value = false
  }
}

function loginWithDiscord() {
  if (!DISCORD_CLIENT_ID) {
    error.value = 'Discord login is not configured'
    return
  }
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
  if (!BATTLENET_CLIENT_ID) {
    error.value = 'Battle.net login is not configured'
    return
  }
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
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 px-4 py-8">
    <!-- Background pattern -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute inset-0 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-primary-900/20 via-transparent to-transparent"></div>
      <div class="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-primary-500/50 to-transparent"></div>
    </div>

    <div class="relative max-w-md w-full">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-xl bg-gradient-to-br from-primary-500 to-primary-700 shadow-lg shadow-primary-500/25 mb-4">
          <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
          </svg>
        </div>
        <h1 class="text-3xl font-bold bg-gradient-to-r from-primary-400 to-primary-200 bg-clip-text text-transparent">
          LootMan
        </h1>
        <p class="mt-2 text-gray-400">Fair Loot Priority Score Dashboard</p>
      </div>

      <!-- Login Card -->
      <div class="bg-gray-800/80 backdrop-blur-sm rounded-2xl border border-gray-700/50 shadow-xl shadow-black/20 p-8">
        <h2 class="text-xl font-semibold text-center text-white mb-6">
          {{ mode === 'login' ? 'Sign in to continue' : 'Create your account' }}
        </h2>

        <!-- Loading state -->
        <div v-if="isLoading" class="text-center py-8">
          <div class="animate-spin w-10 h-10 border-3 border-primary-500 border-t-transparent rounded-full mx-auto"></div>
          <p class="mt-4 text-gray-400">{{ route.query.code ? 'Authenticating...' : 'Please wait...' }}</p>
        </div>

        <template v-else>
          <!-- Error message -->
          <div v-if="error" class="mb-6 p-4 bg-red-500/10 border border-red-500/30 rounded-xl text-red-400 text-sm">
            <div class="flex items-start gap-3">
              <svg class="w-5 h-5 flex-shrink-0 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
              <span>{{ error }}</span>
            </div>
          </div>

          <!-- Login/Register Form -->
          <form @submit.prevent="handleSubmit" class="space-y-5">
            <!-- Username -->
            <div>
              <label for="username" class="block text-sm font-medium text-gray-300 mb-2">
                {{ mode === 'login' ? 'Username or Email' : 'Username' }}
              </label>
              <input
                id="username"
                v-model="username"
                type="text"
                :placeholder="mode === 'login' ? 'Enter username or email' : 'Choose a username'"
                class="w-full px-4 py-3 bg-gray-900/50 border border-gray-600/50 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500 transition-all"
                required
              />
            </div>

            <!-- Email (register only) -->
            <div v-if="mode === 'register'">
              <label for="email" class="block text-sm font-medium text-gray-300 mb-2">Email</label>
              <input
                id="email"
                v-model="email"
                type="email"
                placeholder="Enter your email"
                class="w-full px-4 py-3 bg-gray-900/50 border border-gray-600/50 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500 transition-all"
                required
              />
            </div>

            <!-- Password -->
            <div>
              <label for="password" class="block text-sm font-medium text-gray-300 mb-2">Password</label>
              <input
                id="password"
                v-model="password"
                type="password"
                placeholder="Enter your password"
                class="w-full px-4 py-3 bg-gray-900/50 border border-gray-600/50 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500 transition-all"
                required
              />
            </div>

            <!-- Confirm Password (register only) -->
            <div v-if="mode === 'register'">
              <label for="confirmPassword" class="block text-sm font-medium text-gray-300 mb-2">Confirm Password</label>
              <input
                id="confirmPassword"
                v-model="confirmPassword"
                type="password"
                placeholder="Confirm your password"
                class="w-full px-4 py-3 bg-gray-900/50 border border-gray-600/50 rounded-xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500/50 focus:border-primary-500 transition-all"
                required
              />
              <p v-if="confirmPassword && password !== confirmPassword" class="mt-1 text-xs text-red-400">
                Passwords do not match
              </p>
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="!isFormValid"
              class="w-full py-3 px-4 bg-gradient-to-r from-primary-600 to-primary-500 hover:from-primary-500 hover:to-primary-400 disabled:from-gray-600 disabled:to-gray-600 disabled:cursor-not-allowed text-white font-medium rounded-xl shadow-lg shadow-primary-500/25 hover:shadow-primary-500/40 transition-all duration-200"
            >
              {{ mode === 'login' ? 'Sign In' : 'Create Account' }}
            </button>
          </form>

          <!-- Toggle mode -->
          <p class="mt-6 text-center text-sm text-gray-400">
            {{ mode === 'login' ? "Don't have an account?" : 'Already have an account?' }}
            <button
              @click="toggleMode"
              class="ml-1 text-primary-400 hover:text-primary-300 font-medium transition-colors"
            >
              {{ mode === 'login' ? 'Sign up' : 'Sign in' }}
            </button>
          </p>

          <!-- Divider -->
          <div class="relative my-8">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-700"></div>
            </div>
            <div class="relative flex justify-center">
              <span class="px-4 bg-gray-800 text-sm text-gray-500">or continue with</span>
            </div>
          </div>

          <!-- OAuth buttons -->
          <div class="grid grid-cols-2 gap-4">
            <button
              @click="loginWithDiscord"
              class="flex items-center justify-center px-4 py-3 rounded-xl bg-[#5865F2]/10 hover:bg-[#5865F2]/20 border border-[#5865F2]/30 text-[#5865F2] font-medium transition-all duration-200 hover:border-[#5865F2]/50"
            >
              <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24" fill="currentColor">
                <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/>
              </svg>
              Discord
            </button>

            <button
              @click="loginWithBattlenet"
              class="flex items-center justify-center px-4 py-3 rounded-xl bg-[#148EFF]/10 hover:bg-[#148EFF]/20 border border-[#148EFF]/30 text-[#148EFF] font-medium transition-all duration-200 hover:border-[#148EFF]/50"
            >
              <svg class="w-5 h-5 mr-2" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm0 2c5.523 0 10 4.477 10 10s-4.477 10-10 10S2 17.523 2 12 6.477 2 12 2z"/>
              </svg>
              Battle.net
            </button>
          </div>
        </template>

        <p class="mt-8 text-center text-xs text-gray-500">
          Sign in to view your FLPS score and guild leaderboard
        </p>
      </div>

      <!-- Footer -->
      <p class="mt-6 text-center text-xs text-gray-600">
        EdgeRush LootMan &copy; 2026
      </p>
    </div>
  </div>
</template>
