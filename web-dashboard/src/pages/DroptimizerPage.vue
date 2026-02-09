<script setup lang="ts">
/**
 * DroptimizerPage - Simulate gear upgrades from different sources.
 *
 * Features:
 * - Character selection
 * - Source filtering (Raid/M+/Vault)
 * - Slot filtering
 * - DPS gain visualization
 * - Priority ranking
 * - Simulation execution
 */
import { ref, computed, watch } from 'vue'
import { useQuery, useMutation } from '@tanstack/vue-query'
import { simulationApi, type SimulationResultDto } from '@/api/simulation'
import { flpsApi } from '@/api/flps'
import { useAuthStore } from '@/stores/auth'
import SimulationResults from '@/components/SimulationResults.vue'

const authStore = useAuthStore()
const guildId = computed(() => authStore.currentGuildId || 'dod')

// State
const selectedCharacter = ref<string>('')
const selectedRealm = ref<string>('')
const sourceFilter = ref<'all' | 'raid' | 'dungeon' | 'vault'>('all')
const slotFilter = ref<string>('all')
const isSimulating = ref(false)

// Slots for filtering
const slots = [
  { value: 'all', label: 'All Slots' },
  { value: 'head', label: 'Head' },
  { value: 'neck', label: 'Neck' },
  { value: 'shoulder', label: 'Shoulder' },
  { value: 'back', label: 'Back' },
  { value: 'chest', label: 'Chest' },
  { value: 'wrist', label: 'Wrist' },
  { value: 'hands', label: 'Hands' },
  { value: 'waist', label: 'Waist' },
  { value: 'legs', label: 'Legs' },
  { value: 'feet', label: 'Feet' },
  { value: 'finger1', label: 'Ring 1' },
  { value: 'finger2', label: 'Ring 2' },
  { value: 'trinket1', label: 'Trinket 1' },
  { value: 'trinket2', label: 'Trinket 2' },
  { value: 'main_hand', label: 'Main Hand' },
  { value: 'off_hand', label: 'Off Hand' },
]

// Fetch guild raiders for character selector via leaderboard (has character names)
const { data: leaderboard } = useQuery({
  queryKey: ['leaderboard', guildId],
  queryFn: () => flpsApi.getLeaderboard(guildId.value),
  enabled: computed(() => !!guildId.value),
})

const characters = computed(() => {
  if (!leaderboard.value?.entries) return []
  return leaderboard.value.entries.map(e => ({
    name: e.characterName,
    realm: 'TwistingNether',
    class: e.characterClass,
    role: e.role,
  }))
})

// Auto-select first character when data loads
watch(characters, (chars) => {
  if (chars.length > 0 && !selectedCharacter.value) {
    selectedCharacter.value = chars[0].name
    selectedRealm.value = chars[0].realm
  }
}, { immediate: true })

// Update realm when character changes
watch(selectedCharacter, (name) => {
  const char = characters.value.find(c => c.name === name)
  if (char) selectedRealm.value = char.realm
})

// Fetch simulation results
const {
  data: simulationResults,
  isLoading,
  isError,
  refetch,
} = useQuery({
  queryKey: ['simulation-results', guildId, selectedCharacter, selectedRealm],
  queryFn: () =>
    simulationApi.getSimulationResults(
      guildId.value,
      selectedCharacter.value,
      selectedRealm.value
    ),
  enabled: computed(() => !!guildId.value && !!selectedCharacter.value && !!selectedRealm.value),
})

// Submit simulation mutation
const submitMutation = useMutation({
  mutationFn: () =>
    simulationApi.submitSimulation(guildId.value, selectedCharacter.value, {
      characterRealm: selectedRealm.value,
      characterClass: 'Warrior', // TODO: Get from character data
      characterSpec: 'Arms',
      iterations: 10000,
      fightLengthSeconds: 300,
    }),
  onSuccess: () => {
    isSimulating.value = true
    // Poll for completion
    pollSimulation()
  },
})

// Filtered results
const filteredResults = computed<SimulationResultDto[]>(() => {
  if (!simulationResults.value?.results) return []

  let results = [...simulationResults.value.results]

  // Filter by slot
  if (slotFilter.value !== 'all') {
    results = results.filter((r) => r.slot.toLowerCase() === slotFilter.value)
  }

  return results
})

// Upgrade chart data
const upgradeChartData = computed(() => {
  if (!filteredResults.value.length) return []

  // Group by slot and get best upgrade per slot
  const slotMap = new Map<string, SimulationResultDto>()
  for (const result of filteredResults.value) {
    if (result.isUpgrade) {
      const existing = slotMap.get(result.slot)
      if (!existing || result.percentGain > existing.percentGain) {
        slotMap.set(result.slot, result)
      }
    }
  }

  return Array.from(slotMap.values())
    .sort((a, b) => b.percentGain - a.percentGain)
    .slice(0, 10)
})

// Poll for simulation completion
let pollInterval: number | null = null
function pollSimulation() {
  if (pollInterval) clearInterval(pollInterval)

  pollInterval = window.setInterval(async () => {
    await refetch()
    // Check if simulation completed
    if (simulationResults.value?.results.length) {
      isSimulating.value = false
      if (pollInterval) clearInterval(pollInterval)
    }
  }, 3000)

  // Timeout after 5 minutes
  setTimeout(() => {
    if (pollInterval) {
      clearInterval(pollInterval)
      isSimulating.value = false
    }
  }, 300000)
}

function runSimulation() {
  submitMutation.mutate()
}

function handleItemClick(result: SimulationResultDto) {
  // Open Wowhead tooltip or item modal
  window.open(`https://www.wowhead.com/item=${result.itemId}`, '_blank')
}
</script>

<template>
  <div class="droptimizer-page p-6 max-w-7xl mx-auto">
    <!-- Header -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-3xl font-bold text-white">Droptimizer</h1>
        <p class="text-gray-400 mt-1">Analyze upgrade potential from raid drops, M+, and vault</p>
      </div>
    </div>

    <!-- Controls -->
    <div class="bg-gray-800/50 rounded-lg p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <!-- Character Selector -->
        <div>
          <label class="block text-sm font-medium text-gray-400 mb-2">Character</label>
          <select
            v-model="selectedCharacter"
            data-testid="character-selector"
            class="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option value="">Select character...</option>
            <option v-for="char in characters" :key="char.name" :value="char.name">
              {{ char.name }} - {{ char.realm }}
            </option>
          </select>
        </div>

        <!-- Source Filter -->
        <div>
          <label class="block text-sm font-medium text-gray-400 mb-2">Source</label>
          <div class="flex gap-2">
            <button
              v-for="source in ['all', 'raid', 'dungeon', 'vault'] as const"
              :key="source"
              @click="sourceFilter = source"
              :class="[
                'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                sourceFilter === source
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600',
              ]"
            >
              {{ source === 'all' ? 'All' : source === 'dungeon' ? 'M+' : source === 'raid' ? 'Raid' : 'Vault' }}
            </button>
          </div>
        </div>

        <!-- Slot Filter -->
        <div>
          <label class="block text-sm font-medium text-gray-400 mb-2">Slot</label>
          <select
            v-model="slotFilter"
            data-testid="slot-filter"
            class="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:ring-2 focus:ring-primary-500 focus:border-transparent"
          >
            <option v-for="slot in slots" :key="slot.value" :value="slot.value">
              {{ slot.label }}
            </option>
          </select>
        </div>

        <!-- Run Button -->
        <div class="flex items-end">
          <button
            @click="runSimulation"
            :disabled="!selectedCharacter || isSimulating"
            data-testid="run-simulation-btn"
            class="w-full px-6 py-2 bg-primary-600 hover:bg-primary-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors flex items-center justify-center gap-2"
          >
            <svg
              v-if="isSimulating"
              class="animate-spin w-5 h-5"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                class="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="4"
              />
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            {{ isSimulating ? 'Simulating...' : 'Run Simulation' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Progress Indicator -->
    <div
      v-if="isSimulating"
      data-testid="simulation-progress"
      class="bg-blue-900/30 border border-blue-700 rounded-lg p-4 mb-6"
    >
      <div class="flex items-center gap-3">
        <div class="animate-spin w-6 h-6 border-2 border-blue-500 border-t-transparent rounded-full"></div>
        <div>
          <p class="text-blue-300 font-medium">Simulation in progress...</p>
          <p class="text-blue-400 text-sm">This may take a few minutes depending on iteration count.</p>
        </div>
      </div>
      <div class="mt-3 w-full bg-blue-900/50 rounded-full h-2">
        <div class="bg-blue-500 h-2 rounded-full animate-pulse" style="width: 60%"></div>
      </div>
    </div>

    <!-- Upgrade Priority Chart -->
    <div
      v-if="upgradeChartData.length > 0"
      data-testid="upgrade-chart"
      class="bg-gray-800/50 rounded-lg p-6 mb-6"
    >
      <h2 class="text-xl font-semibold text-white mb-4">Top Upgrade Priorities</h2>
      <div class="space-y-3">
        <div
          v-for="item in upgradeChartData"
          :key="item.itemId"
          class="flex items-center gap-4"
        >
          <div class="w-32 text-sm text-gray-300 truncate">{{ item.slot }}</div>
          <div class="flex-1 bg-gray-700 rounded-full h-6 overflow-hidden">
            <div
              class="h-full rounded-full transition-all duration-500"
              :class="[
                item.percentGain >= 10 ? 'bg-purple-500' :
                item.percentGain >= 5 ? 'bg-blue-500' :
                item.percentGain >= 2 ? 'bg-green-500' : 'bg-gray-500'
              ]"
              :style="{ width: `${Math.min(item.percentGain * 10, 100)}%` }"
            >
              <span class="text-xs text-white font-medium px-2 leading-6">
                {{ item.itemName }} (+{{ item.percentGain.toFixed(1) }}%)
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Results -->
    <SimulationResults
      v-if="simulationResults?.results"
      :results="filteredResults"
      :character-name="selectedCharacter"
      :character-realm="selectedRealm"
      :loading="isLoading"
      @item-click="handleItemClick"
    />

    <!-- Empty State -->
    <div
      v-else-if="!isLoading && !isSimulating"
      class="bg-gray-800/50 rounded-lg p-12 text-center"
    >
      <svg class="w-16 h-16 mx-auto text-gray-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
      </svg>
      <h3 class="text-xl font-semibold text-white mb-2">No Simulation Data</h3>
      <p class="text-gray-400 mb-4">Select a character and run a simulation to see upgrade priorities.</p>
    </div>
  </div>
</template>
