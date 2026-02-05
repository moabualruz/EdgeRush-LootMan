<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const route = useRoute()

const token = ref('')
const password = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)
const isSuccess = ref(false)
const error = ref<string | null>(null)

onMounted(() => {
  token.value = (route.query.token as string) || ''
  if (!token.value) {
    error.value = 'Invalid reset link. Please request a new password reset.'
  }
})

const isValidPassword = computed(() => {
  return password.value.length >= 6
})

const passwordsMatch = computed(() => {
  return password.value === confirmPassword.value
})

const isFormValid = computed(() => {
  return isValidPassword.value && passwordsMatch.value && token.value
})

async function handleSubmit() {
  if (!isFormValid.value) return

  isLoading.value = true
  error.value = null

  try {
    await authApi.resetPassword(token.value, password.value)
    isSuccess.value = true
    // Redirect to login after 2 seconds
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (e: any) {
    error.value = e.message || 'Failed to reset password. The link may have expired.'
  } finally {
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
        <div class="inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-gradient-to-br from-primary to-accent shadow-2xl shadow-primary/30 mb-6">
          <svg class="w-10 h-10 text-white drop-shadow-md" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
          </svg>
        </div>
        <h1 class="text-4xl font-bold text-white tracking-tight text-glow">
          New Password
        </h1>
        <p class="mt-3 text-muted-foreground font-medium">Enter your new password below</p>
      </div>

      <!-- Card -->
      <div class="glass-card p-8 border-white/10 backdrop-blur-xl bg-black/40">
        <!-- Success State -->
        <div v-if="isSuccess" class="text-center py-6">
          <div class="w-16 h-16 bg-green-500/20 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg class="w-8 h-8 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 class="text-xl font-bold text-white mb-2">Password Reset Successfully!</h2>
          <p class="text-muted-foreground mb-6">
            Your password has been updated. Redirecting to login...
          </p>
          <RouterLink 
            to="/login" 
            class="inline-flex items-center text-primary hover:text-white font-semibold transition-colors"
          >
            Go to Login Now
          </RouterLink>
        </div>

        <!-- Form State -->
        <template v-else>
          <!-- Error message -->
          <div v-if="error" class="mb-6 p-4 bg-destructive/10 border border-destructive/20 rounded-lg text-destructive-foreground text-sm flex items-start gap-3">
            <svg class="w-5 h-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ error }}</span>
          </div>

          <form @submit.prevent="handleSubmit" class="space-y-5">
            <!-- New Password -->
            <div class="group">
              <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1.5 ml-1">
                New Password
              </label>
              <input
                v-model="password"
                type="password"
                placeholder="••••••••"
                class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white placeholder-muted-foreground/50 focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
                required
                minlength="6"
              />
              <p v-if="password && !isValidPassword" class="mt-1 text-xs text-destructive">
                Password must be at least 6 characters
              </p>
            </div>

            <!-- Confirm Password -->
            <div class="group">
              <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1.5 ml-1">
                Confirm Password
              </label>
              <input
                v-model="confirmPassword"
                type="password"
                placeholder="••••••••"
                class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white placeholder-muted-foreground/50 focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all"
                required
              />
              <p v-if="confirmPassword && !passwordsMatch" class="mt-1 text-xs text-destructive">
                Passwords do not match
              </p>
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="!isFormValid || isLoading"
              class="w-full py-3.5 px-4 bg-primary hover:bg-primary-600 disabled:opacity-50 disabled:cursor-not-allowed text-primary-foreground font-bold rounded-lg shadow-lg shadow-primary/25 hover:shadow-primary/40 transition-all duration-300 transform hover:-translate-y-0.5"
            >
              <span v-if="isLoading" class="flex items-center justify-center">
                <svg class="w-5 h-5 mr-2 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Resetting...
              </span>
              <span v-else>Reset Password</span>
            </button>
          </form>

          <!-- Back to login link -->
          <div class="mt-8 text-center">
            <RouterLink 
              to="/login" 
              class="text-sm text-muted-foreground hover:text-primary transition-colors"
            >
              ← Back to Login
            </RouterLink>
          </div>
        </template>
      </div>

      <!-- Footer -->
      <div class="mt-8 text-center text-xs text-muted-foreground opacity-60">
        <p>EdgeRush LootMan &copy; 2026. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>
