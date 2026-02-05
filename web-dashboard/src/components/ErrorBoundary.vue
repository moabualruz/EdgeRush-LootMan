<script setup lang="ts">
import { onErrorCaptured, ref } from 'vue'
import { RouterLink } from 'vue-router'

const error = ref<Error | null>(null)
const errorInfo = ref<string>('')

onErrorCaptured((err, instance, info) => {
  error.value = err
  errorInfo.value = info
  console.error('ErrorBoundary caught:', err, info)
  return false // Stop propagation
})

function retry() {
  error.value = null
  errorInfo.value = ''
}
</script>

<template>
  <div v-if="error" class="min-h-screen flex items-center justify-center bg-background relative overflow-hidden px-4 py-8">
    <!-- Animated Background -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute top-0 left-1/4 w-96 h-96 bg-destructive/20 rounded-full blur-3xl opacity-30 animate-pulse"></div>
      <div class="absolute bottom-0 right-1/4 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl opacity-30 animate-pulse delay-1000"></div>
    </div>

    <div class="relative z-10 max-w-lg w-full">
      <div class="glass-card p-8 border-white/10 backdrop-blur-xl bg-black/40 text-center">
        <!-- Error Icon -->
        <div class="w-20 h-20 bg-destructive/20 rounded-full flex items-center justify-center mx-auto mb-6">
          <svg class="w-10 h-10 text-destructive" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>

        <h2 class="text-2xl font-bold text-white mb-2">Something went wrong</h2>
        <p class="text-muted-foreground mb-6">
          An unexpected error occurred. Please try again or return to the home page.
        </p>

        <!-- Error details (collapsible) -->
        <details class="text-left mb-6 bg-black/20 rounded-lg p-4">
          <summary class="text-sm text-muted-foreground cursor-pointer hover:text-white transition-colors">
            Technical details
          </summary>
          <pre class="mt-3 text-xs text-destructive-foreground overflow-auto max-h-32 p-2 bg-black/30 rounded">{{ error.message }}</pre>
        </details>

        <!-- Actions -->
        <div class="flex flex-col sm:flex-row gap-3 justify-center">
          <button 
            @click="retry" 
            class="px-6 py-3 bg-primary hover:bg-primary-600 text-primary-foreground font-bold rounded-lg shadow-lg shadow-primary/25 hover:shadow-primary/40 transition-all duration-300"
          >
            <span class="flex items-center justify-center">
              <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              Try Again
            </span>
          </button>
          
          <RouterLink 
            to="/dashboard" 
            class="px-6 py-3 bg-white/10 hover:bg-white/20 text-white font-semibold rounded-lg border border-white/10 transition-all duration-300"
          >
            Return Home
          </RouterLink>
        </div>
      </div>

      <!-- Footer -->
      <div class="mt-8 text-center text-xs text-muted-foreground opacity-60">
        <p>If this problem persists, please contact support.</p>
      </div>
    </div>
  </div>

  <slot v-else />
</template>
