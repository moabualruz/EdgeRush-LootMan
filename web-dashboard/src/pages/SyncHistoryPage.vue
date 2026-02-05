<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { syncApi } from '@/api/sync'
import { useSyncTrigger } from '@/composables/useSyncTrigger'
import SyncLogViewer from '@/components/SyncLogViewer.vue'

// Sync trigger mutations
const triggerWoWAudit = useSyncTrigger('WoWAudit')
const triggerWarcraftLogs = useSyncTrigger('WarcraftLogs')

const selectedSource = ref<string | null>(null)
const selectedStatus = ref<string | null>(null)
const currentPage = ref(0)
const pageSize = 20

const sources = ['WoWAudit', 'WarcraftLogs']
const statuses = ['RUNNING', 'COMPLETED', 'FAILED']

// Build query key based on filters
const queryKey = computed(() => {
  if (selectedSource.value) {
    return ['syncRuns', 'source', selectedSource.value, currentPage.value]
  }
  if (selectedStatus.value) {
    return ['syncRuns', 'status', selectedStatus.value, currentPage.value]
  }
  return ['syncRuns', currentPage.value]
})

// Query function based on filters
const queryFn = computed(() => {
  if (selectedSource.value) {
    return () => syncApi.getSyncRunsBySource(selectedSource.value!, currentPage.value, pageSize)
  }
  if (selectedStatus.value) {
    return () => syncApi.getSyncRunsByStatus(selectedStatus.value!, currentPage.value, pageSize)
  }
  return () => syncApi.getSyncRuns(currentPage.value, pageSize)
})

const { data, isLoading, error, refetch } = useQuery({
  queryKey,
  queryFn: () => queryFn.value(),
})

const syncRuns = computed(() => data.value?.content ?? [])
const totalPages = computed(() => data.value?.totalPages ?? 0)
const totalElements = computed(() => data.value?.totalElements ?? 0)

function setSourceFilter(source: string | null) {
  selectedSource.value = source
  selectedStatus.value = null
  currentPage.value = 0
}

function setStatusFilter(status: string | null) {
  selectedStatus.value = status
  selectedSource.value = null
  currentPage.value = 0
}

function clearFilters() {
  selectedSource.value = null
  selectedStatus.value = null
  currentPage.value = 0
}

function goToPage(page: number) {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
  }
}

function getStatusColor(status: string): string {
  switch (status) {
    case 'RUNNING':
      return 'bg-blue-600'
    case 'COMPLETED':
      return 'bg-green-600'
    case 'FAILED':
      return 'bg-red-600'
    default:
      return 'bg-gray-600'
  }
}

function getSourceIcon(source: string): string {
  switch (source) {
    case 'WoWAudit':
      return 'WA'
    case 'WarcraftLogs':
      return 'WL'
    default:
      return source.substring(0, 2).toUpperCase()
  }
}

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleString()
}

function formatDuration(startedAt: string, completedAt: string | null): string {
  if (!completedAt) return 'In progress...'
  const start = new Date(startedAt).getTime()
  const end = new Date(completedAt).getTime()
  const durationMs = end - start

  if (durationMs < 1000) return `${durationMs}ms`
  if (durationMs < 60000) return `${(durationMs / 1000).toFixed(1)}s`
  return `${(durationMs / 60000).toFixed(1)}m`
}

// Log viewer state
const selectedSyncRunId = ref<number | null>(null)
const isLogViewerOpen = ref(false)

function openLogViewer(syncRunId: number) {
  selectedSyncRunId.value = syncRunId
  isLogViewerOpen.value = true
}

function closeLogViewer() {
  isLogViewerOpen.value = false
  selectedSyncRunId.value = null
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">Sync History</h1>
        <p class="text-gray-400 text-sm mt-1">
          View data synchronization history from external sources
        </p>
      </div>
      <div class="flex items-center space-x-2">
        <button
          @click="triggerWoWAudit.mutate()"
          :disabled="triggerWoWAudit.isPending.value"
          class="bg-primary-600 hover:bg-primary-500 disabled:opacity-50 disabled:cursor-not-allowed px-4 py-2 rounded-lg text-sm font-medium flex items-center"
        >
          <span v-if="triggerWoWAudit.isPending.value" class="animate-spin mr-2">⟳</span>
          Sync WoWAudit
        </button>
        <button
          @click="triggerWarcraftLogs.mutate()"
          :disabled="triggerWarcraftLogs.isPending.value"
          class="bg-orange-600 hover:bg-orange-500 disabled:opacity-50 disabled:cursor-not-allowed px-4 py-2 rounded-lg text-sm font-medium flex items-center"
        >
          <span v-if="triggerWarcraftLogs.isPending.value" class="animate-spin mr-2">⟳</span>
          Sync WarcraftLogs
        </button>
        <button
          @click="refetch()"
          class="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg text-sm"
        >
          Refresh
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="card mb-6">
      <div class="flex flex-wrap items-center gap-4">
        <!-- Source filters -->
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-400">Source:</span>
          <button
            @click="clearFilters"
            class="px-3 py-1 rounded text-sm"
            :class="!selectedSource && !selectedStatus ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'"
          >
            All
          </button>
          <button
            v-for="source in sources"
            :key="source"
            @click="setSourceFilter(source)"
            class="px-3 py-1 rounded text-sm"
            :class="selectedSource === source ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'"
          >
            {{ source }}
          </button>
        </div>

        <div class="border-l border-gray-700 h-6"></div>

        <!-- Status filters -->
        <div class="flex items-center space-x-2">
          <span class="text-sm text-gray-400">Status:</span>
          <button
            v-for="status in statuses"
            :key="status"
            @click="setStatusFilter(status)"
            class="px-3 py-1 rounded text-sm"
            :class="selectedStatus === status ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-300 hover:bg-gray-600'"
          >
            {{ status }}
          </button>
        </div>

        <div v-if="selectedSource || selectedStatus" class="ml-auto">
          <button
            @click="clearFilters"
            class="text-sm text-gray-400 hover:text-white"
          >
            Clear filters
          </button>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="alert alert-error">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <div>
        <h5 class="alert-title">Error Loading Sync History</h5>
        <div class="alert-description">Failed to load sync history. Please try again later.</div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="syncRuns.length === 0" class="card text-center py-12">
      <p class="text-gray-400">No sync runs found.</p>
    </div>

    <!-- Sync runs list -->
    <div v-else>
      <div class="card mb-4">
        <p class="text-sm text-gray-400">
          Showing {{ syncRuns.length }} of {{ totalElements }} sync runs
        </p>
      </div>

      <div class="space-y-4">
        <div
          v-for="run in syncRuns"
          :key="run.id"
          class="card cursor-pointer hover:bg-gray-800/50 transition-colors"
          @click="openLogViewer(run.id)"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <!-- Source icon -->
              <div class="w-10 h-10 rounded-lg bg-gray-700 flex items-center justify-center font-bold text-sm">
                {{ getSourceIcon(run.source) }}
              </div>

              <div>
                <div class="flex items-center space-x-3">
                  <span class="font-medium">{{ run.source }}</span>
                  <span
                    :class="[
                      'px-2 py-0.5 rounded text-xs font-medium',
                      getStatusColor(run.status)
                    ]"
                  >
                    {{ run.status }}
                  </span>
                </div>
                <p v-if="run.message" class="text-sm text-gray-400 mt-1 max-w-xl truncate">
                  {{ run.message }}
                </p>
              </div>
            </div>

            <div class="text-right">
              <div class="text-sm">
                <span class="text-gray-400">Started:</span>
                <span class="ml-2">{{ formatDate(run.startedAt) }}</span>
              </div>
              <div class="text-sm mt-1">
                <span class="text-gray-400">Duration:</span>
                <span class="ml-2" :class="run.status === 'RUNNING' ? 'text-blue-400' : ''">
                  {{ formatDuration(run.startedAt, run.completedAt) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="card mt-6">
        <div class="flex items-center justify-between">
          <button
            @click="goToPage(currentPage - 1)"
            :disabled="currentPage === 0"
            class="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Previous
          </button>

          <div class="flex items-center space-x-2">
            <button
              v-for="page in Math.min(totalPages, 5)"
              :key="page - 1"
              @click="goToPage(page - 1)"
              class="w-8 h-8 rounded text-sm"
              :class="currentPage === page - 1 ? 'bg-primary-600 text-white' : 'bg-gray-700 hover:bg-gray-600'"
            >
              {{ page }}
            </button>
            <span v-if="totalPages > 5" class="text-gray-400">...</span>
          </div>

          <button
            @click="goToPage(currentPage + 1)"
            :disabled="currentPage >= totalPages - 1"
            class="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </div>
      </div>
    </div>

    <!-- Log Viewer Modal -->
    <SyncLogViewer
      :is-open="isLogViewerOpen"
      :sync-run-id="selectedSyncRunId"
      @close="closeLogViewer"
    />
  </div>
</template>
