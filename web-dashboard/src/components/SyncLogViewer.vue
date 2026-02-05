<script setup lang="ts">
import { computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { syncApi, type SyncLog } from '@/api/sync'

const props = defineProps<{
  isOpen: boolean
  syncRunId: number | null
}>()

const emit = defineEmits<{
  close: []
}>()

// Fetch logs when modal is open
const enabled = computed(() => props.isOpen && props.syncRunId !== null)

const { data: logs, isLoading, error } = useQuery({
  queryKey: ['syncLogs', props.syncRunId],
  queryFn: () => syncApi.getSyncLogs(props.syncRunId!),
  enabled,
})

function handleClose() {
  emit('close')
}

function getLevelColor(level: SyncLog['level']): string {
  switch (level) {
    case 'INFO':
      return 'text-blue-400'
    case 'WARN':
      return 'text-yellow-400'
    case 'ERROR':
      return 'text-red-400'
    default:
      return 'text-gray-400'
  }
}

function getLevelBadgeColor(level: SyncLog['level']): string {
  switch (level) {
    case 'INFO':
      return 'bg-blue-600/20 text-blue-400'
    case 'WARN':
      return 'bg-yellow-600/20 text-yellow-400'
    case 'ERROR':
      return 'bg-red-600/20 text-red-400'
    default:
      return 'bg-gray-600/20 text-gray-400'
  }
}

function formatTimestamp(timestamp: string): string {
  const date = new Date(timestamp)
  return date.toLocaleTimeString()
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isOpen && syncRunId"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <!-- Backdrop -->
      <div
        data-testid="backdrop"
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="handleClose"
      />

      <!-- Modal -->
      <div class="relative bg-gray-900 border border-gray-700 rounded-xl shadow-2xl w-full max-w-2xl mx-4 max-h-[80vh] flex flex-col">
        <!-- Header -->
        <div class="flex items-center justify-between p-6 border-b border-gray-700">
          <h2 class="text-xl font-bold">Sync Logs</h2>
          <button
            type="button"
            class="text-gray-400 hover:text-white transition-colors"
            @click="handleClose"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <!-- Content -->
        <div class="flex-1 overflow-auto p-6">
          <!-- Loading -->
          <div v-if="isLoading" class="flex items-center justify-center py-8">
            <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
          </div>

          <!-- Error -->
          <div v-else-if="error" class="text-red-400 text-center py-8">
            Failed to load logs. Please try again.
          </div>

          <!-- Empty -->
          <div v-else-if="!logs || logs.length === 0" class="text-gray-400 text-center py-8">
            No logs available for this sync run.
          </div>

          <!-- Logs -->
          <div v-else class="space-y-2">
            <div
              v-for="(log, index) in logs"
              :key="index"
              class="flex items-start gap-3 p-3 bg-gray-800/50 rounded-lg font-mono text-sm"
            >
              <span class="text-gray-500 shrink-0">
                {{ formatTimestamp(log.timestamp) }}
              </span>
              <span
                :class="getLevelBadgeColor(log.level)"
                class="px-2 py-0.5 rounded text-xs font-medium shrink-0"
              >
                {{ log.level }}
              </span>
              <span :class="getLevelColor(log.level)" class="break-all">
                {{ log.message }}
              </span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="p-4 border-t border-gray-700 flex justify-end">
          <button
            type="button"
            class="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg text-sm"
            @click="handleClose"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
