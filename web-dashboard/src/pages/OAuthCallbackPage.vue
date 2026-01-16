<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const error = ref<string | null>(null)
const loading = ref(true)

onMounted(async () => {
  const code = route.query.code as string
  const provider = route.params.provider as string
  const errorParam = route.query.error as string
  // Check if this is an account linking flow (user already has a token)
  const isLinking = route.query.link === 'true' || !!authStore.token

  if (errorParam) {
    error.value = `OAuth error: ${errorParam}`
    loading.value = false
    return
  }

  if (!code) {
    error.value = 'No authorization code received'
    loading.value = false
    return
  }

  try {
    if (isLinking && authStore.token) {
      // User is already logged in - link the account instead of creating new
      if (provider === 'discord') {
        await authStore.linkDiscord(code)
      } else if (provider === 'battlenet') {
        await authStore.linkBattlenet(code)
      } else {
        error.value = `Unknown OAuth provider: ${provider}`
        loading.value = false
        return
      }
    } else {
      // User is not logged in - this is a login/register flow
      if (provider === 'discord') {
        await authStore.loginWithDiscord(code)
      } else if (provider === 'battlenet') {
        await authStore.loginWithBattlenet(code)
      } else {
        error.value = `Unknown OAuth provider: ${provider}`
        loading.value = false
        return
      }
    }

    // Redirect to dashboard on success
    router.push('/dashboard')
  } catch (err: any) {
    console.error('OAuth callback error:', err)
    error.value = err.response?.data?.message || err.message || 'Authentication failed'
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-background flex items-center justify-center relative overflow-hidden">
    <!-- Background element -->
    <div class="absolute inset-0 bg-gradient-to-br from-primary/5 to-purple-500/5"></div>
    <div class="absolute inset-0 bg-[url('/img/grid.svg')] bg-center [mask-image:linear-gradient(180deg,white,rgba(255,255,255,0))]"></div>

    <div class="text-center relative z-10 p-8 glass-card border-white/5 animate-fade-in-up">
      <div v-if="loading" class="space-y-6">
        <div class="relative w-16 h-16 mx-auto">
          <div class="absolute inset-0 rounded-full border-4 border-primary/30"></div>
          <div class="absolute inset-0 rounded-full border-4 border-primary border-t-transparent animate-spin"></div>
        </div>
        <div>
          <h2 class="text-2xl font-bold text-white mb-2 text-glow">Connecting...</h2>
          <p class="text-muted-foreground">Securing your connection to the grid.</p>
        </div>
      </div>

      <div v-else-if="error" class="space-y-6 max-w-md">
        <div class="w-16 h-16 rounded-full bg-destructive/10 flex items-center justify-center mx-auto text-destructive border border-destructive/20">
          <svg class="w-8 h-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
        </div>
        <div class="bg-destructive/5 border border-destructive/20 rounded-xl p-6">
          <h2 class="text-xl font-bold text-destructive-foreground mb-2">Authentication Failed</h2>
          <p class="text-muted-foreground text-sm">{{ error }}</p>
        </div>
        <button
          @click="router.push('/login')"
          class="px-8 py-3 bg-primary hover:bg-primary/90 text-primary-foreground rounded-lg transition-all duration-300 font-medium shadow-lg shadow-primary/20"
        >
          Return to Login
        </button>
      </div>
    </div>
  </div>
</template>
