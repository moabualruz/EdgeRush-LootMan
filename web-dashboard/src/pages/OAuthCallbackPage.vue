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
    if (provider === 'discord') {
      await authStore.loginWithDiscord(code)
    } else if (provider === 'battlenet') {
      await authStore.loginWithBattlenet(code)
    } else {
      error.value = `Unknown OAuth provider: ${provider}`
      loading.value = false
      return
    }

    // Redirect to dashboard on success
    router.push('/dashboard')
  } catch (err) {
    console.error('OAuth callback error:', err)
    error.value = err instanceof Error ? err.message : 'Authentication failed'
    loading.value = false
  }
})
</script>

<template>
  <div class="min-h-screen bg-slate-900 flex items-center justify-center">
    <div class="text-center">
      <div v-if="loading" class="space-y-4">
        <div class="animate-spin h-12 w-12 border-4 border-purple-500 border-t-transparent rounded-full mx-auto"></div>
        <p class="text-slate-300 text-lg">Completing authentication...</p>
      </div>

      <div v-else-if="error" class="space-y-4">
        <div class="bg-red-500/20 border border-red-500/50 rounded-lg p-6 max-w-md">
          <h2 class="text-xl font-bold text-red-400 mb-2">Authentication Failed</h2>
          <p class="text-red-300">{{ error }}</p>
        </div>
        <button
          @click="router.push('/login')"
          class="px-6 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors"
        >
          Back to Login
        </button>
      </div>
    </div>
  </div>
</template>
