<script setup lang="ts">
/**
 * ToastContainer - Displays toast notifications.
 *
 * Add this component once at the app root level.
 * Toasts are displayed in the top-right corner with smooth animations.
 */
import { computed } from 'vue'
import { useToast, type Toast, type ToastType } from '@/composables/useToast'

const { toasts, dismiss } = useToast()

// Icon and color configuration for each toast type
const typeConfig: Record<ToastType, { icon: string; bgColor: string; borderColor: string; iconColor: string }> = {
  success: {
    icon: '✓',
    bgColor: 'bg-green-900/90',
    borderColor: 'border-green-500',
    iconColor: 'text-green-400',
  },
  error: {
    icon: '✕',
    bgColor: 'bg-red-900/90',
    borderColor: 'border-red-500',
    iconColor: 'text-red-400',
  },
  warning: {
    icon: '⚠',
    bgColor: 'bg-yellow-900/90',
    borderColor: 'border-yellow-500',
    iconColor: 'text-yellow-400',
  },
  info: {
    icon: 'ℹ',
    bgColor: 'bg-blue-900/90',
    borderColor: 'border-blue-500',
    iconColor: 'text-blue-400',
  },
}

function getConfig(type: ToastType) {
  return typeConfig[type]
}
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed top-4 right-4 z-50 flex flex-col gap-3 max-w-sm w-full pointer-events-none"
      aria-live="polite"
      aria-label="Notifications"
    >
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="pointer-events-auto rounded-lg border-l-4 shadow-lg backdrop-blur-sm"
          :class="[getConfig(toast.type).bgColor, getConfig(toast.type).borderColor]"
          role="alert"
        >
          <div class="flex items-start gap-3 p-4">
            <!-- Icon -->
            <span
              class="flex-shrink-0 text-lg font-bold"
              :class="getConfig(toast.type).iconColor"
            >
              {{ getConfig(toast.type).icon }}
            </span>

            <!-- Content -->
            <div class="flex-1 min-w-0">
              <p class="font-semibold text-white">{{ toast.title }}</p>
              <p v-if="toast.message" class="text-sm text-gray-300 mt-1">
                {{ toast.message }}
              </p>
            </div>

            <!-- Dismiss button -->
            <button
              v-if="toast.dismissible"
              @click="dismiss(toast.id)"
              class="flex-shrink-0 text-gray-400 hover:text-white transition-colors"
              aria-label="Dismiss notification"
            >
              <span class="text-xl leading-none">&times;</span>
            </button>
          </div>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active {
  animation: toast-in 0.3s ease-out;
}

.toast-leave-active {
  animation: toast-out 0.2s ease-in;
}

.toast-move {
  transition: transform 0.3s ease;
}

@keyframes toast-in {
  0% {
    opacity: 0;
    transform: translateX(100%);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes toast-out {
  0% {
    opacity: 1;
    transform: translateX(0);
  }
  100% {
    opacity: 0;
    transform: translateX(100%);
  }
}
</style>
