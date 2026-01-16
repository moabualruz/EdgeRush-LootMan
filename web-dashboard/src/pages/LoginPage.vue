<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const isLoading = ref(false)
const error = ref<string | null>(null)
const mode = ref<'login' | 'register'>('login')

// Form fields
const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const role = ref('RAIDER')

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
      await authStore.register(username.value, email.value, password.value, role.value)
    }
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || 'Authentication failed. Please try again.'
    console.error('Auth error:', e)
  } finally {
    isLoading.value = false
  }
}

async function loginWithDiscord() {
  isLoading.value = true
  error.value = null
  try {
    const url = await authStore.getDiscordAuthUrl()
    console.log('Discord OAuth URL:', url)
    if (url) {
      window.location.href = url
    } else {
      error.value = 'Failed to get Discord authorization URL'
      isLoading.value = false
    }
  } catch (e: any) {
    console.error('Discord OAuth error:', e)
    error.value = e.response?.data?.message || e.message || 'Discord login is not configured'
    isLoading.value = false
  }
}

async function loginWithBattlenet() {
  isLoading.value = true
  error.value = null
  try {
    const url = await authStore.getBattlenetAuthUrl()
    console.log('Battle.net OAuth URL:', url)
    if (url) {
      window.location.href = url
    } else {
      error.value = 'Failed to get Battle.net authorization URL'
      isLoading.value = false
    }
  } catch (e: any) {
    console.error('Battle.net OAuth error:', e)
    error.value = e.response?.data?.message || e.message || 'Battle.net login is not configured'
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-background relative overflow-hidden px-4 py-8">
    <!-- Animated Background -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-0 left-1/4 w-96 h-96 bg-primary/20 rounded-full blur-3xl opacity-30 animate-pulse"></div>
      <div class="absolute bottom-0 right-1/4 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl opacity-30 animate-pulse delay-1000"></div>
    </div>

    <div class="relative max-w-md w-full z-10">
      <!-- Logo -->
      <div class="text-center mb-10">
        <div class="inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-gradient-to-br from-primary to-accent shadow-2xl shadow-primary/30 mb-6 group transition-transform hover:scale-105 duration-500">
          <svg class="w-10 h-10 text-white drop-shadow-md" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <h1 class="text-4xl font-bold text-white tracking-tight text-glow">
          LootMan
        </h1>
        <p class="mt-3 text-muted-foreground font-medium">Premium Raid Management</p>
      </div>

      <!-- Login Card -->
      <div class="glass-card p-8 border-white/10 backdrop-blur-xl bg-black/40">
        <h2 class="text-2xl font-bold text-center text-white mb-8">
          {{ mode === 'login' ? 'Welcome Back' : 'Join the Ranks' }}
        </h2>

        <!-- Loading state -->
        <div v-if="isLoading" class="text-center py-12">
          <div class="w-12 h-12 border-4 border-primary border-t-transparent rounded-full mx-auto animate-spin"></div>
          <p class="mt-4 text-muted-foreground animate-pulse">Authenticating...</p>
        </div>

        <template v-else>
          <!-- Error message -->
          <div v-if="error" class="mb-6 p-4 bg-destructive/10 border border-destructive/20 rounded-lg text-destructive-foreground text-sm flex items-start gap-3">
             <svg class="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
             <span>{{ error }}</span>
          </div>

          <!-- Login/Register Form -->
          <form @submit.prevent="handleSubmit" class="space-y-5">
            <!-- Username -->
            <div class="group">
              <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1.5 ml-1">
                {{ mode === 'login' ? 'Identity' : 'Username' }}
              </label>
              <input
                v-model="username"
                type="text"
                :placeholder="mode === 'login' ? 'Username or Email' : 'Choose a username'"
                class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white placeholder-muted-foreground/50 focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
                required
              />
            </div>

            <!-- Email (register only) -->
            <div v-if="mode === 'register'" class="group animate-fade-in-up">
              <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1.5 ml-1">Email Address</label>
              <input
                v-model="email"
                type="email"
                placeholder="name@example.com"
                class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white placeholder-muted-foreground/50 focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
                required
              />
            </div>

            <!-- Password -->
            <div class="group">
              <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1.5 ml-1">Password</label>
              <input
                v-model="password"
                type="password"
                placeholder="••••••••"
                class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white placeholder-muted-foreground/50 focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
                required
              />
            </div>

            <!-- Confirm Password (register only) -->
            <div v-if="mode === 'register'" class="group animate-fade-in-up">
              <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1.5 ml-1">Confirm Password</label>
              <input
                v-model="confirmPassword"
                type="password"
                placeholder="••••••••"
                class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white placeholder-muted-foreground/50 focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
                required
              />
              <p v-if="confirmPassword && password !== confirmPassword" class="mt-1 text-xs text-destructive">
                Passwords do not match
              </p>
            </div>

            <!-- Role Selection (register only) -->
            <div v-if="mode === 'register'" class="group animate-fade-in-up">
              <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1.5 ml-1">Account Type</label>
              <div class="relative">
                <select
                  v-model="role"
                  class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all appearance-none cursor-pointer"
                >
                  <option value="RAIDER" class="bg-gray-900">Raider</option>
                  <option value="GUILD_ADMIN" class="bg-gray-900">Guild Admin</option>
                </select>
                <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-muted-foreground">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" /></svg>
                </div>
              </div>
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="!isFormValid"
              class="w-full py-3.5 px-4 bg-primary hover:bg-primary-600 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold rounded-lg shadow-lg shadow-primary/25 hover:shadow-primary/40 transition-all duration-300 transform hover:-translate-y-0.5"
            >
              {{ mode === 'login' ? 'Sign In' : 'Create Account' }}
            </button>
          </form>

          <!-- Toggle mode -->
          <div class="mt-8 text-center">
            <p class="text-sm text-muted-foreground">
              {{ mode === 'login' ? "Don't have an account?" : 'Already have an account?' }}
              <button
                @click="toggleMode"
                class="ml-1 text-primary hover:text-white font-semibold transition-colors border-b border-transparent hover:border-primary"
              >
                {{ mode === 'login' ? 'Sign up' : 'Sign in' }}
              </button>
            </p>
          </div>

          <!-- Divider -->
          <div class="relative my-8">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-white/10"></div>
            </div>
            <div class="relative flex justify-center">
              <span class="px-4 bg-[#0a0f1c] text-xs text-muted-foreground uppercase tracking-widest backdrop-blur-sm">Or connect with</span>
            </div>
          </div>

          <!-- OAuth buttons -->
          <div class="grid grid-cols-2 gap-4">
            <button
              @click="loginWithDiscord"
              class="flex items-center justify-center px-4 py-3 rounded-lg bg-[#5865F2]/10 hover:bg-[#5865F2]/20 border border-[#5865F2]/20 text-[#5865F2] hover:text-white font-semibold transition-all duration-300 group"
            >
              <svg class="w-5 h-5 mr-2 transition-transform group-hover:scale-110" viewBox="0 0 24 24" fill="currentColor">
                <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/>
              </svg>
              Discord
            </button>

            <button
              @click="loginWithBattlenet"
              class="flex items-center justify-center px-4 py-3 rounded-lg bg-[#148EFF]/10 hover:bg-[#148EFF]/20 border border-[#148EFF]/20 text-[#148EFF] hover:text-white font-semibold transition-all duration-300 group"
            >
              <svg class="w-5 h-5 mr-2 transition-transform group-hover:scale-110" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 0C5.373 0 0 5.373 0 12s5.373 12 12 12 12-5.373 12-12S18.627 0 12 0zm0 2c5.523 0 10 4.477 10 10s-4.477 10-10 10S2 17.523 2 12 6.477 2 12 2z"/>
              </svg>
              Battle.net
            </button>
          </div>
        </template>
      </div>

      <!-- Footer -->
      <div class="mt-8 text-center text-xs text-muted-foreground opacity-60">
        <p>EdgeRush LootMan &copy; 2026. All rights reserved.</p>
        <p class="mt-1">Forged for the World of Warcraft Community</p>
      </div>
    </div>
  </div>
</template>
