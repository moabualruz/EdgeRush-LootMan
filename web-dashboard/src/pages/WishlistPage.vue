<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { wishlistApi, type SimulationStatus } from '@/api/wishlist'
import type { WishlistItem } from '@/types'
import { formatRelativeTime } from '@/utils/date'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'

const queryClient = useQueryClient()

// Sort options
type SortField = 'upgradeValue' | 'itemName' | 'slot'
type SortDirection = 'asc' | 'desc'
const sortField = ref<SortField>('upgradeValue')
const sortDirection = ref<SortDirection>('desc')
const filterSlot = ref<string>('all')

// Wishlist data query
const { data: wishlistData, isLoading: wishlistLoading, error: wishlistError } = useQuery({
  queryKey: ['myWishlist', GUILD_ID],
  queryFn: () => wishlistApi.getMyWishlist(GUILD_ID),
})

// Track simulation polling state separately to avoid circular reference
const shouldPollSimulation = ref(false)

// Simulation status query - polls when running
const { data: simStatus, isLoading: simStatusLoading } = useQuery({
  queryKey: ['simulationStatus', GUILD_ID, wishlistData.value?.raiderId],
  queryFn: () => wishlistApi.getSimulationStatus(GUILD_ID, wishlistData.value!.raiderId),
  enabled: computed(() => !!wishlistData.value?.raiderId),
  refetchInterval: computed(() => shouldPollSimulation.value ? 3000 : false),
})

// Watch simulation status to control polling
watch(() => simStatus.value?.status, (status) => {
  shouldPollSimulation.value = status === 'RUNNING' || status === 'QUEUED'
}, { immediate: true })

// Trigger simulation mutation
const triggerSimMutation = useMutation({
  mutationFn: () => wishlistApi.triggerSimulation(GUILD_ID, wishlistData.value!.raiderId),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['simulationStatus', GUILD_ID] })
  },
})

// Compute unique slots for filtering
const availableSlots = computed(() => {
  if (!wishlistData.value?.items) return []
  const slots = new Set(wishlistData.value.items.map(item => item.slot))
  return Array.from(slots).sort()
})

// Sorted and filtered items
const sortedItems = computed(() => {
  if (!wishlistData.value?.items) return []

  let items = [...wishlistData.value.items]

  // Filter by slot
  if (filterSlot.value !== 'all') {
    items = items.filter(item => item.slot === filterSlot.value)
  }

  // Sort
  items.sort((a, b) => {
    let comparison = 0
    switch (sortField.value) {
      case 'upgradeValue':
        comparison = a.upgradeValue - b.upgradeValue
        break
      case 'itemName':
        comparison = a.itemName.localeCompare(b.itemName)
        break
      case 'slot':
        comparison = a.slot.localeCompare(b.slot)
        break
    }
    return sortDirection.value === 'desc' ? -comparison : comparison
  })

  return items
})

// Stale items count
const staleItemsCount = computed(() => {
  if (!wishlistData.value?.items) return 0
  return wishlistData.value.items.filter(item => item.isStale).length
})

// Helper functions
function toggleSort(field: SortField) {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = field === 'itemName' ? 'asc' : 'desc'
  }
}

function getSortIcon(field: SortField): string {
  if (sortField.value !== field) return '↕'
  return sortDirection.value === 'asc' ? '↑' : '↓'
}

function getUpgradeValueColor(value: number): string {
  if (value >= 10) return 'text-purple-400'
  if (value >= 5) return 'text-blue-400'
  if (value >= 2) return 'text-green-400'
  return 'text-gray-400'
}

function getSimulationStatusColor(status: SimulationStatus['status']): string {
  switch (status) {
    case 'COMPLETED':
      return 'text-green-400'
    case 'RUNNING':
      return 'text-blue-400'
    case 'QUEUED':
      return 'text-yellow-400'
    case 'FAILED':
      return 'text-red-400'
    default:
      return 'text-gray-400'
  }
}

function getSimulationStatusLabel(status: SimulationStatus['status']): string {
  switch (status) {
    case 'COMPLETED':
      return 'Up to date'
    case 'RUNNING':
      return 'Simulating...'
    case 'QUEUED':
      return 'Queued'
    case 'FAILED':
      return 'Failed'
    default:
      return 'Idle'
  }
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Wishlist</h1>
      <div v-if="wishlistData" class="text-sm text-gray-400">
        {{ wishlistData.characterName }}
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="wishlistLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="wishlistError" class="card bg-red-900/20 border-red-700">
      <p class="text-red-400">Failed to load wishlist data. Please try again.</p>
    </div>

    <!-- Content -->
    <div v-else-if="wishlistData" class="space-y-6">
      <!-- Simulation Status Card -->
      <div class="card">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <h2 class="text-lg font-semibold">Simulation Status</h2>
            <div v-if="simStatus" class="flex items-center space-x-2">
              <span
                :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  getSimulationStatusColor(simStatus.status)
                ]"
              >
                <span
                  v-if="simStatus.status === 'RUNNING'"
                  class="w-2 h-2 mr-1.5 bg-blue-400 rounded-full animate-pulse"
                ></span>
                {{ getSimulationStatusLabel(simStatus.status) }}
              </span>
            </div>
          </div>

          <button
            @click="triggerSimMutation.mutate()"
            :disabled="triggerSimMutation.isPending.value || simStatus?.status === 'RUNNING' || simStatus?.status === 'QUEUED'"
            class="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ triggerSimMutation.isPending.value ? 'Starting...' : 'Run Simulation' }}
          </button>
        </div>

        <!-- Simulation Progress -->
        <div v-if="simStatus?.status === 'RUNNING' && simStatus.progress" class="mt-4">
          <div class="flex justify-between text-sm text-gray-400 mb-1">
            <span>Progress</span>
            <span>{{ Math.round(simStatus.progress * 100) }}%</span>
          </div>
          <div class="w-full bg-gray-700 rounded-full h-2">
            <div
              class="bg-primary-500 h-2 rounded-full transition-all duration-300"
              :style="{ width: `${simStatus.progress * 100}%` }"
            ></div>
          </div>
        </div>

        <!-- Last Run Info -->
        <div v-if="simStatus?.lastRunAt" class="mt-4 text-sm text-gray-400">
          Last simulation: {{ formatRelativeTime(simStatus.lastRunAt) }}
        </div>

        <!-- Error Display -->
        <div v-if="simStatus?.status === 'FAILED' && simStatus.error" class="mt-4 p-3 bg-red-900/20 rounded-lg">
          <p class="text-sm text-red-400">{{ simStatus.error }}</p>
        </div>

        <!-- Mutation Error -->
        <div v-if="triggerSimMutation.isError.value" class="mt-4 p-3 bg-red-900/20 rounded-lg">
          <p class="text-sm text-red-400">Failed to start simulation. Please try again.</p>
        </div>
      </div>

      <!-- Stale Data Warning -->
      <div v-if="staleItemsCount > 0" class="card bg-yellow-900/20 border-yellow-700">
        <div class="flex items-start space-x-3">
          <span class="text-yellow-400 text-xl">⚠</span>
          <div>
            <h3 class="font-semibold text-yellow-400">Stale Data Warning</h3>
            <p class="text-yellow-300 text-sm mt-1">
              {{ staleItemsCount }} item(s) have outdated simulation data. Run a new simulation to update upgrade values.
            </p>
          </div>
        </div>
      </div>

      <!-- Filters and Controls -->
      <div class="card">
        <div class="flex flex-wrap items-center gap-4">
          <div class="flex items-center space-x-2">
            <label class="text-sm text-gray-400">Slot:</label>
            <select v-model="filterSlot" class="input py-1.5 text-sm w-auto">
              <option value="all">All Slots</option>
              <option v-for="slot in availableSlots" :key="slot" :value="slot">
                {{ slot }}
              </option>
            </select>
          </div>

          <div class="flex-1"></div>

          <div class="text-sm text-gray-400">
            {{ sortedItems.length }} items
            <span v-if="filterSlot !== 'all'">
              ({{ wishlistData.items.length }} total)
            </span>
          </div>
        </div>
      </div>

      <!-- Wishlist Table -->
      <div class="card overflow-hidden p-0">
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-800/50">
              <tr>
                <th
                  @click="toggleSort('itemName')"
                  class="text-left px-4 py-3 text-sm font-medium text-gray-400 cursor-pointer hover:text-white transition-colors"
                >
                  Item {{ getSortIcon('itemName') }}
                </th>
                <th
                  @click="toggleSort('slot')"
                  class="text-left px-4 py-3 text-sm font-medium text-gray-400 cursor-pointer hover:text-white transition-colors"
                >
                  Slot {{ getSortIcon('slot') }}
                </th>
                <th
                  @click="toggleSort('upgradeValue')"
                  class="text-right px-4 py-3 text-sm font-medium text-gray-400 cursor-pointer hover:text-white transition-colors"
                >
                  Upgrade Value {{ getSortIcon('upgradeValue') }}
                </th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">
                  Source
                </th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">
                  Status
                </th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-700">
              <tr v-if="sortedItems.length === 0">
                <td colspan="5" class="px-4 py-8 text-center text-gray-400">
                  No items in wishlist.
                </td>
              </tr>
              <tr
                v-for="item in sortedItems"
                :key="item.itemId"
                class="hover:bg-gray-800/30 transition-colors"
              >
                <td class="px-4 py-3">
                  <span class="font-medium text-primary-300">{{ item.itemName }}</span>
                </td>
                <td class="px-4 py-3 text-sm text-gray-400">
                  {{ item.slot }}
                </td>
                <td class="px-4 py-3 text-right">
                  <span :class="['font-mono font-semibold', getUpgradeValueColor(item.upgradeValue)]">
                    +{{ item.upgradeValue.toFixed(1) }}%
                  </span>
                </td>
                <td class="px-4 py-3">
                  <span
                    :class="[
                      'text-xs px-2 py-0.5 rounded',
                      item.simulationSource === 'RAIDBOTS'
                        ? 'bg-blue-900/50 text-blue-400'
                        : 'bg-gray-700 text-gray-400'
                    ]"
                  >
                    {{ item.simulationSource === 'RAIDBOTS' ? 'Raidbots' : 'Manual' }}
                  </span>
                </td>
                <td class="px-4 py-3">
                  <div class="flex items-center space-x-2">
                    <span
                      v-if="item.isStale"
                      class="text-xs px-2 py-0.5 rounded bg-yellow-900/50 text-yellow-400"
                    >
                      Stale
                    </span>
                    <span
                      v-else
                      class="text-xs px-2 py-0.5 rounded bg-green-900/50 text-green-400"
                    >
                      Current
                    </span>
                    <span v-if="item.lastSimulatedAt" class="text-xs text-gray-500">
                      {{ formatRelativeTime(item.lastSimulatedAt) }}
                    </span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Legend -->
      <div class="card">
        <h3 class="text-sm font-semibold mb-3">Upgrade Value Legend</h3>
        <div class="flex flex-wrap gap-4 text-sm">
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-purple-400"></span>
            <span class="text-gray-400">10%+ (Best in Slot)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-blue-400"></span>
            <span class="text-gray-400">5-10% (Major Upgrade)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-green-400"></span>
            <span class="text-gray-400">2-5% (Minor Upgrade)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-gray-400"></span>
            <span class="text-gray-400">&lt;2% (Sidegrade)</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
