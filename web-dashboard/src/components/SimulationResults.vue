<script setup lang="ts">
/**
 * SimulationResults - Display simulation results for a character.
 *
 * Features:
 * - Sorted results by DPS/percent gain
 * - Color coding for upgrade tiers
 * - Progress bars for normalized values
 * - BiS (Best in Slot) indicators
 * - Upgrade/downgrade differentiation
 */
import { computed, ref } from 'vue'
import type { SimulationResultDto } from '@/api/simulation'
import { formatRelativeTime } from '@/utils/date'

const props = withDefaults(
  defineProps<{
    results: SimulationResultDto[]
    characterName: string
    characterRealm: string
    showUpgradesOnly?: boolean
    loading?: boolean
  }>(),
  {
    showUpgradesOnly: false,
    loading: false,
  }
)

const emit = defineEmits<{
  'item-click': [result: SimulationResultDto]
}>()

// Sort options
type SortField = 'percentGain' | 'dpsGain' | 'itemName' | 'slot' | 'normalizedValue'
type SortDirection = 'asc' | 'desc'
const sortField = ref<SortField>('percentGain')
const sortDirection = ref<SortDirection>('desc')

// Computed properties
const filteredResults = computed(() => {
  if (props.showUpgradesOnly) {
    return props.results.filter((r) => r.isUpgrade)
  }
  return props.results
})

const sortedResults = computed(() => {
  const results = [...filteredResults.value]

  results.sort((a, b) => {
    let comparison = 0
    switch (sortField.value) {
      case 'percentGain':
        comparison = a.percentGain - b.percentGain
        break
      case 'dpsGain':
        comparison = a.dpsGain - b.dpsGain
        break
      case 'normalizedValue':
        comparison = a.normalizedValue - b.normalizedValue
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

  return results
})

const upgradeCount = computed(() => props.results.filter((r) => r.isUpgrade).length)

const maxNormalizedValue = computed(() => {
  return Math.max(...props.results.map((r) => r.normalizedValue), 0)
})

const latestSimulation = computed(() => {
  if (props.results.length === 0) return null
  return props.results.reduce((latest, current) => {
    return new Date(current.simulatedAt) > new Date(latest.simulatedAt) ? current : latest
  })
})

// Helper functions
function toggleSort(field: SortField) {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = field === 'itemName' || field === 'slot' ? 'asc' : 'desc'
  }
}

function getSortIcon(field: SortField): string {
  if (sortField.value !== field) return '↕'
  return sortDirection.value === 'asc' ? '↑' : '↓'
}

function formatDps(value: number): string {
  const formatted = Math.abs(value).toLocaleString()
  return value >= 0 ? `+${formatted}` : `-${formatted}`
}

function formatPercent(value: number): string {
  const sign = value >= 0 ? '+' : ''
  return `${sign}${value.toFixed(1)}%`
}

function getPercentGainColor(value: number): string {
  if (value < 0) return 'text-red-400'
  if (value >= 10) return 'text-purple-400'
  if (value >= 5) return 'text-blue-400'
  if (value >= 2) return 'text-green-400'
  return 'text-gray-400'
}

function isBestInSlot(result: SimulationResultDto): boolean {
  return result.normalizedValue === maxNormalizedValue.value && result.normalizedValue > 0
}

function handleRowClick(result: SimulationResultDto) {
  emit('item-click', result)
}
</script>

<template>
  <div class="simulation-results">
    <!-- Header -->
    <div class="flex items-center justify-between mb-4">
      <div>
        <h3 class="text-lg font-semibold">Simulation Results</h3>
        <p class="text-sm text-gray-400">
          {{ characterName }} - {{ characterRealm }}
        </p>
      </div>
      <div v-if="latestSimulation" class="text-right text-sm text-gray-500">
        Simulated {{ formatRelativeTime(latestSimulation.simulatedAt) }}
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="sortedResults.length === 0"
      class="text-center py-12 text-gray-400"
    >
      <svg class="w-12 h-12 mx-auto mb-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
      <p>No simulation results available.</p>
      <p class="text-sm mt-1">Run a simulation to see upgrade values.</p>
    </div>

    <!-- Results -->
    <div v-else>
      <!-- Summary -->
      <div class="flex items-center gap-4 mb-4 text-sm">
        <span class="text-gray-400">
          {{ sortedResults.length }} items
        </span>
        <span class="text-green-400">
          {{ upgradeCount }} upgrade{{ upgradeCount !== 1 ? 's' : '' }}
        </span>
        <span v-if="results.length - upgradeCount > 0" class="text-red-400">
          {{ results.length - upgradeCount }} downgrade{{ results.length - upgradeCount !== 1 ? 's' : '' }}
        </span>
      </div>

      <!-- Results Table -->
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
                @click="toggleSort('dpsGain')"
                class="text-right px-4 py-3 text-sm font-medium text-gray-400 cursor-pointer hover:text-white transition-colors"
              >
                DPS {{ getSortIcon('dpsGain') }}
              </th>
              <th
                @click="toggleSort('percentGain')"
                class="text-right px-4 py-3 text-sm font-medium text-gray-400 cursor-pointer hover:text-white transition-colors"
              >
                Percent {{ getSortIcon('percentGain') }}
              </th>
              <th
                @click="toggleSort('normalizedValue')"
                class="text-left px-4 py-3 text-sm font-medium text-gray-400 cursor-pointer hover:text-white transition-colors"
              >
                Priority {{ getSortIcon('normalizedValue') }}
              </th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-700">
            <tr
              v-for="result in sortedResults"
              :key="result.itemId"
              @click="handleRowClick(result)"
              class="hover:bg-gray-800/30 transition-colors cursor-pointer"
            >
              <td class="px-4 py-3">
                <div class="flex items-center space-x-2">
                  <span
                    :class="[
                      'font-medium',
                      result.isUpgrade ? 'text-primary-300' : 'text-gray-400',
                    ]"
                  >
                    {{ result.itemName }}
                  </span>
                  <span
                    v-if="isBestInSlot(result)"
                    class="text-xs px-1.5 py-0.5 bg-purple-900/50 text-purple-400 rounded font-medium"
                  >
                    BiS
                  </span>
                </div>
              </td>
              <td class="px-4 py-3 text-sm text-gray-400">
                {{ result.slot }}
              </td>
              <td class="px-4 py-3 text-right">
                <span
                  :class="[
                    'font-mono font-medium',
                    result.dpsGain >= 0 ? 'text-green-400' : 'text-red-400',
                  ]"
                >
                  {{ formatDps(result.dpsGain) }}
                </span>
              </td>
              <td class="px-4 py-3 text-right">
                <span
                  :class="[
                    'font-mono font-semibold',
                    getPercentGainColor(result.percentGain),
                  ]"
                >
                  {{ formatPercent(result.percentGain) }}
                </span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center space-x-2">
                  <div class="progress-bar w-24 bg-gray-700 rounded-full h-2">
                    <div
                      :class="[
                        'h-2 rounded-full transition-all',
                        result.normalizedValue >= 0.8 ? 'bg-purple-500' :
                        result.normalizedValue >= 0.5 ? 'bg-blue-500' :
                        result.normalizedValue >= 0.2 ? 'bg-green-500' : 'bg-gray-500',
                      ]"
                      :style="{ width: `${Math.max(result.normalizedValue * 100, 0)}%` }"
                    ></div>
                  </div>
                  <span class="text-xs text-gray-500 w-8">
                    {{ (result.normalizedValue * 100).toFixed(0) }}%
                  </span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Legend -->
      <div class="flex flex-wrap gap-4 mt-6 pt-4 border-t border-gray-700 text-sm">
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
        <div class="flex items-center space-x-2">
          <span class="w-3 h-3 rounded-full bg-red-400"></span>
          <span class="text-gray-400">Downgrade</span>
        </div>
      </div>
    </div>
  </div>
</template>
