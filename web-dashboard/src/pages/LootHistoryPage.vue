<script setup lang="ts">
import { computed, ref, toRef, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { useQuery } from '@tanstack/vue-query'
import { lootApi } from '@/api/loot'
import { useAuthStore } from '@/stores/auth'
import { useGuildContextStore } from '@/stores/guildContext'
import { formatDate, formatRelativeTime } from '@/utils/date'
import { useWowhead } from '@/composables/useWowhead'
import { useLootRevoke } from '@/composables/useLootRevoke'
import WowheadItem from '@/components/WowheadItem.vue'
import ItemHoverPreview from '@/components/ItemHoverPreview.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { DonutChart, BarChart } from '@/components/charts'
import AwardLootModal from '@/components/loot/AwardLootModal.vue'
import LootContextMenu from '@/components/loot/LootContextMenu.vue'
import EditLootModal from '@/components/loot/EditLootModal.vue'
import type { LootAward } from '@/types'

const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()
const guildId = computed(() => guildContextStore.currentGuildId || authStore.user?.guildId)

const { data, isLoading, error } = useQuery({
  queryKey: ['myLootHistory', guildId, 50],
  queryFn: () => lootApi.getMyLootHistory(guildId.value!, 50),
  enabled: computed(() => !!guildId.value),
})

// Initialize Wowhead tooltips
const dataRef = toRef(() => data.value)
useWowhead({}, [dataRef])

const formatScore = (score: number) => score.toFixed(3)

// Award modal state
const isAwardModalOpen = ref(false)

// Search state
const searchQuery = ref('')
const debouncedQuery = ref('')

const updateDebouncedQuery = useDebounceFn((value: string) => {
  debouncedQuery.value = value
}, 300)

watch(searchQuery, (val) => {
  updateDebouncedQuery(val)
})

const filteredAwards = computed(() => {
  if (!data.value?.awards) return []
  if (!debouncedQuery.value.trim()) return data.value.awards
  const q = debouncedQuery.value.toLowerCase()
  return data.value.awards.filter(award =>
    award.itemName.toLowerCase().includes(q) ||
    award.characterName?.toLowerCase().includes(q)
  )
})

// Sorting state
type SortColumn = 'awardedAt' | 'itemName' | 'flpsAtAward'
const sortColumn = ref<SortColumn>('awardedAt')
const sortDir = ref<'asc' | 'desc'>('desc')

function toggleSort(column: SortColumn) {
  if (sortColumn.value === column) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortColumn.value = column
    sortDir.value = 'desc'
  }
  currentPage.value = 0 // Reset to first page on sort
}

const sortedAwards = computed(() => {
  const awards = [...filteredAwards.value]
  const col = sortColumn.value
  const dir = sortDir.value === 'asc' ? 1 : -1
  
  return awards.sort((a, b) => {
    if (col === 'itemName') {
      return dir * a.itemName.localeCompare(b.itemName)
    } else if (col === 'flpsAtAward') {
      return dir * (a.flpsAtAward - b.flpsAtAward)
    } else {
      return dir * (new Date(a.awardedAt).getTime() - new Date(b.awardedAt).getTime())
    }
  })
})

// Pagination state
const pageSize = 10
const currentPage = ref(0)

const totalPages = computed(() => Math.ceil(sortedAwards.value.length / pageSize))

const paginatedAwards = computed(() => {
  const start = currentPage.value * pageSize
  return sortedAwards.value.slice(start, start + pageSize)
})

// Reset page when search changes
watch(debouncedQuery, () => {
  currentPage.value = 0
})

// Context menu state
const contextMenu = ref<{
  isOpen: boolean
  position: { x: number; y: number }
  award: LootAward | null
}>({
  isOpen: false,
  position: { x: 0, y: 0 },
  award: null,
})

// Edit modal state
const isEditModalOpen = ref(false)
const editingAward = ref<LootAward | null>(null)

// Revoke mutation
const revokeMutation = useLootRevoke(guildId)

function handleContextMenu(event: MouseEvent, award: LootAward) {
  event.preventDefault()
  contextMenu.value = {
    isOpen: true,
    position: { x: event.clientX, y: event.clientY },
    award,
  }
}

function closeContextMenu() {
  contextMenu.value.isOpen = false
}

function handleEdit(awardId: number) {
  const award = filteredAwards.value.find(a => a.id === awardId) || data.value?.awards.find(a => a.id === awardId)
  if (award) {
    editingAward.value = award
    isEditModalOpen.value = true
  }
  closeContextMenu()
}

async function handleRevoke(awardId: number) {
  closeContextMenu()
  if (confirm('Are you sure you want to revoke this loot award? This action cannot be undone.')) {
    await revokeMutation.mutateAsync(awardId)
  }
}

// RDF status breakdown for donut chart
const rdfBreakdown = computed(() => {
  if (!data.value?.awards) return []
  const expired = data.value.awards.filter(a => a.rdfExpired).length
  const active = data.value.awards.filter(a => !a.rdfExpired).length
  return [
    { label: 'RDF Expired', value: expired, color: '#22c55e' },
    { label: 'RDF Active', value: active, color: '#eab308' },
  ]
})

// Monthly loot breakdown for bar chart
const monthlyLoot = computed(() => {
  if (!data.value?.awards) return []
  const monthCounts: Record<string, number> = {}

  data.value.awards.forEach(award => {
    const date = new Date(award.awardedAt)
    const monthKey = date.toLocaleDateString('en-US', { month: 'short' })
    monthCounts[monthKey] = (monthCounts[monthKey] || 0) + 1
  })

  return Object.entries(monthCounts)
    .slice(-6)
    .map(([label, value]) => ({ label, value }))
})

// Average FLPS at award
const averageFlps = computed(() => {
  if (!data.value?.awards?.length) return 0
  const sum = data.value.awards.reduce((acc, a) => acc + a.flpsAtAward, 0)
  return sum / data.value.awards.length
})
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Loot History</h1>
      <button
        class="btn-primary"
        @click="isAwardModalOpen = true"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2">
          <line x1="12" y1="5" x2="12" y2="19" />
          <line x1="5" y1="12" x2="19" y2="12" />
        </svg>
        Award Loot
      </button>
    </div>

    <!-- Loading state with skeletons -->
    <div v-if="isLoading && guildId" class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <SkeletonCard :lines="2" />
        <SkeletonCard :lines="2" />
        <SkeletonCard :lines="2" />
      </div>
      <SkeletonCard :lines="5" />
    </div>

    <!-- No Guild state -->
    <div v-else-if="!guildId" class="alert alert-info">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
      <div>
        <h5 class="alert-title">No Guild Found</h5>
        <div class="alert-description">You are not currently a member of any guild. Please join one to view loot history.</div>
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="alert alert-error">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <div>
        <h5 class="alert-title">Error Loading Loot History</h5>
        <div class="alert-description">Failed to load loot history. Please try again later.</div>
      </div>
    </div>

    <!-- Content -->
    <div v-else-if="data" class="space-y-6">
      <!-- Empty state -->
      <div v-if="data.awards.length === 0" class="card text-center py-8">
        <p class="text-gray-400">No loot history found.</p>
      </div>

      <template v-else>
        <!-- Stats Summary -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="card text-center">
            <div class="text-3xl font-bold text-primary-400">{{ data.awards.length }}</div>
            <div class="text-sm text-gray-400 mt-1">Total Items</div>
          </div>
          <div class="card text-center">
            <div class="text-3xl font-bold text-blue-400">{{ formatScore(averageFlps) }}</div>
            <div class="text-sm text-gray-400 mt-1">Avg FLPS at Award</div>
          </div>
          <div class="card text-center">
            <div class="text-3xl font-bold text-green-400">
              {{ data.awards.filter(a => a.rdfExpired).length }}
            </div>
            <div class="text-sm text-gray-400 mt-1">RDF Cleared</div>
          </div>
        </div>

        <!-- Charts Row -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- RDF Status Chart -->
          <div v-if="rdfBreakdown.length > 0" class="card">
            <h2 class="text-lg font-semibold mb-4">RDF Status</h2>
            <DonutChart
              :data="rdfBreakdown"
              :size="160"
              center-label="Items"
            />
          </div>

          <!-- Monthly Loot Chart -->
          <div v-if="monthlyLoot.length > 0" class="card">
            <h2 class="text-lg font-semibold mb-4">Loot by Month</h2>
            <BarChart
              :data="monthlyLoot"
              :height="180"
              bar-color="#8b5cf6"
            />
          </div>
        </div>

        <!-- Loot items list -->
        <div class="card">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold">Recent Loot</h2>
            <div class="relative">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
              >
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Search items..."
                class="input pl-10 w-64"
              />
            </div>
          </div>
          
          <!-- No results -->
          <div v-if="filteredAwards.length === 0 && debouncedQuery" class="text-center py-6">
            <p class="text-gray-400">No items found matching "{{ debouncedQuery }}"</p>
          </div>
          
          <template v-else>
            <!-- Sort Controls -->
            <div class="flex items-center gap-4 mb-4 text-sm">
              <span class="text-gray-400">Sort by:</span>
              <button
                @click="toggleSort('awardedAt')"
                :class="['px-2 py-1 rounded', sortColumn === 'awardedAt' ? 'bg-primary-600 text-white' : 'text-gray-300 hover:bg-gray-700']"
              >
                Date {{ sortColumn === 'awardedAt' ? (sortDir === 'desc' ? '↓' : '↑') : '' }}
              </button>
              <button
                @click="toggleSort('itemName')"
                :class="['px-2 py-1 rounded', sortColumn === 'itemName' ? 'bg-primary-600 text-white' : 'text-gray-300 hover:bg-gray-700']"
              >
                Item {{ sortColumn === 'itemName' ? (sortDir === 'desc' ? '↓' : '↑') : '' }}
              </button>
              <button
                @click="toggleSort('flpsAtAward')"
                :class="['px-2 py-1 rounded', sortColumn === 'flpsAtAward' ? 'bg-primary-600 text-white' : 'text-gray-300 hover:bg-gray-700']"
              >
                FLPS {{ sortColumn === 'flpsAtAward' ? (sortDir === 'desc' ? '↓' : '↑') : '' }}
              </button>
            </div>
            
            <!-- Items List -->
            <div class="space-y-4">
              <div
                v-for="award in paginatedAwards"
                :key="award.id"
              class="flex items-center justify-between p-3 bg-gray-800/30 rounded-lg hover:bg-gray-800/50 transition-colors cursor-context-menu"
              @contextmenu="handleContextMenu($event, award)"
            >
              <div class="flex-1">
                <ItemHoverPreview :item-id="award.itemId" position="right">
                  <WowheadItem
                    :item-id="award.itemId"
                    :item-name="award.itemName"
                    quality="epic"
                  />
                </ItemHoverPreview>
                <p class="text-sm text-gray-400 mt-1">
                  Awarded {{ formatDate(award.awardedAt) }} · FLPS: {{ formatScore(award.flpsAtAward) }}
                </p>
              </div>

              <div class="text-right">
                <div
                  :class="[
                    'inline-flex items-center px-3 py-1 rounded-full text-sm font-medium',
                    award.rdfExpired
                      ? 'bg-green-900/50 text-green-400'
                      : 'bg-yellow-900/50 text-yellow-400'
                  ]"
                >
                  <span v-if="award.rdfExpired">RDF Expired</span>
                  <span v-else>
                    RDF: {{ formatRelativeTime(award.rdfExpiresAt!) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Pagination -->
          <div v-if="totalPages > 1" class="flex items-center justify-between mt-6 pt-4 border-t border-gray-700">
            <span class="text-sm text-gray-400">
              Showing {{ currentPage * pageSize + 1 }}-{{ Math.min((currentPage + 1) * pageSize, sortedAwards.length) }} of {{ sortedAwards.length }}
            </span>
            <div class="flex items-center gap-2">
              <button
                @click="currentPage = 0"
                :disabled="currentPage === 0"
                class="px-2 py-1 text-sm rounded bg-gray-700 text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600"
              >
                First
              </button>
              <button
                @click="currentPage--"
                :disabled="currentPage === 0"
                class="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600"
              >
                ←
              </button>
              <span class="text-sm text-gray-300 px-2">
                Page {{ currentPage + 1 }} of {{ totalPages }}
              </span>
              <button
                @click="currentPage++"
                :disabled="currentPage >= totalPages - 1"
                class="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600"
              >
                →
              </button>
              <button
                @click="currentPage = totalPages - 1"
                :disabled="currentPage >= totalPages - 1"
                class="px-2 py-1 text-sm rounded bg-gray-700 text-gray-300 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-600"
              >
                Last
              </button>
            </div>
          </div>
        </template>
      </template>
    </div>

    <!-- Award Loot Modal -->
    <AwardLootModal
      :is-open="isAwardModalOpen"
      @close="isAwardModalOpen = false"
    />

    <!-- Context Menu -->
    <LootContextMenu
      :is-open="contextMenu.isOpen"
      :position="contextMenu.position"
      :award-id="contextMenu.award?.id ?? 0"
      :item-name="contextMenu.award?.itemName ?? ''"
      @edit="handleEdit"
      @revoke="handleRevoke"
      @close="closeContextMenu"
    />

    <!-- Edit Loot Modal -->
    <EditLootModal
      :is-open="isEditModalOpen"
      :award="editingAward"
      @close="isEditModalOpen = false"
    />
  </div>
</template>
