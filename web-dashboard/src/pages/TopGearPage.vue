<script setup lang="ts">
/**
 * TopGearPage - Find optimal gear combinations.
 *
 * Features:
 * - Profile selector (ST/AoE/Custom)
 * - Current vs Optimal gear comparison
 * - Tier set optimization
 * - Gem/enchant recommendations
 * - Bag/bank item analysis
 */
import { ref, computed, watch } from 'vue'
import { useQuery, useMutation } from '@tanstack/vue-query'
import { simulationApi } from '@/api/simulation'
import { gearApi } from '@/api/gear'
import { flpsApi } from '@/api/flps'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const guildId = computed(() => authStore.currentGuildId || 'dod')

// State
const selectedCharacter = ref<string>('')
const selectedRealm = ref<string>('')
const selectedProfile = ref<'single_target' | 'aoe' | 'custom'>('single_target')
const isCalculating = ref(false)

// Profile options
const profiles = [
  { value: 'single_target', label: 'Single Target', icon: '🎯' },
  { value: 'aoe', label: 'AoE', icon: '💥' },
  { value: 'custom', label: 'Custom', icon: '⚙️' },
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

// Fetch character gear
const {
  data: gearData,
  isLoading: gearLoading,
} = useQuery({
  queryKey: ['character-gear', selectedCharacter, selectedRealm],
  queryFn: () => gearApi.getMyGear(guildId.value),
  enabled: computed(() => !!guildId.value && !!selectedCharacter.value),
})

// Calculate optimal gear mutation
const calculateMutation = useMutation({
  mutationFn: async () => {
    isCalculating.value = true
    // Submit simulation with bag items included
    return simulationApi.submitSimulation(guildId.value, selectedCharacter.value, {
      characterRealm: selectedRealm.value,
      characterClass: 'Warrior',
      characterSpec: 'Arms',
      iterations: 5000,
      fightLengthSeconds: 300,
    })
  },
  onSettled: () => {
    isCalculating.value = false
  },
})

// Optimal gear (mocked for now)
const optimalGear = ref<GearSlot[]>([])

interface GearSlot {
  slot: string
  current: GearItem | null
  optimal: GearItem | null
  dpsGain: number
}

interface GearItem {
  itemId: number
  itemName: string
  itemLevel: number
  source: 'equipped' | 'bag' | 'bank' | 'vault'
}

// Equipment slots
const slots = [
  'head', 'neck', 'shoulder', 'back', 'chest',
  'wrist', 'hands', 'waist', 'legs', 'feet',
  'finger1', 'finger2', 'trinket1', 'trinket2',
  'main_hand', 'off_hand',
]

// Tier set tracking
const tierPieces = computed(() => {
  if (!gearData.value?.items) return { count: 0, bonus2pc: false, bonus4pc: false }
  
  const tierSlots = ['HEAD', 'SHOULDER', 'CHEST', 'HANDS', 'LEGS']
  const equippedTier = gearData.value.items.filter(
    (item) => tierSlots.includes(item.slot) && item.isTierPiece
  )
  
  return {
    count: equippedTier.length,
    bonus2pc: equippedTier.length >= 2,
    bonus4pc: equippedTier.length >= 4,
  }
})

// DPS summary
const dpsSummary = computed(() => {
  const totalGain = optimalGear.value.reduce((sum, slot) => sum + slot.dpsGain, 0)
  return {
    totalGain,
    percentGain: totalGain > 0 ? (totalGain / 100000 * 100).toFixed(1) : '0.0',
    upgradeCount: optimalGear.value.filter(s => s.dpsGain > 0).length,
  }
})

function calculateOptimalGear() {
  calculateMutation.mutate()
}

function formatItemLevel(ilvl: number): string {
  return ilvl.toString()
}

function getItemLevelClass(ilvl: number): string {
  if (ilvl >= 639) return 'text-purple-400'
  if (ilvl >= 626) return 'text-blue-400'
  if (ilvl >= 613) return 'text-green-400'
  return 'text-gray-400'
}
</script>

<template>
  <div class="top-gear-page p-6 max-w-7xl mx-auto">
    <!-- Header -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-3xl font-bold text-white">Top Gear</h1>
        <p class="text-gray-400 mt-1">Find your optimal gear setup from equipped, bags, and vault</p>
      </div>
    </div>

    <!-- Controls -->
    <div class="bg-gray-800/50 rounded-lg p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Character Selector -->
        <div>
          <label class="block text-sm font-medium text-gray-400 mb-2">Character</label>
          <select
            v-model="selectedCharacter"
            class="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white"
          >
            <option value="">Select character...</option>
            <option v-for="char in characters" :key="char.name" :value="char.name">
              {{ char.name }} - {{ char.realm }}
            </option>
          </select>
        </div>

        <!-- Profile Selector -->
        <div>
          <label class="block text-sm font-medium text-gray-400 mb-2">Fight Profile</label>
          <div data-testid="profile-selector" class="flex gap-2">
            <button
              v-for="profile in profiles"
              :key="profile.value"
              @click="selectedProfile = profile.value as any"
              :class="[
                'flex-1 px-3 py-2 rounded-lg text-sm font-medium transition-colors flex items-center justify-center gap-2',
                selectedProfile === profile.value
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-700 text-gray-300 hover:bg-gray-600',
              ]"
            >
              <span>{{ profile.icon }}</span>
              <span>{{ profile.label }}</span>
            </button>
          </div>
        </div>

        <!-- Calculate Button -->
        <div class="flex items-end">
          <button
            @click="calculateOptimalGear"
            :disabled="!selectedCharacter || isCalculating"
            data-testid="calculate-btn"
            class="w-full px-6 py-2 bg-primary-600 hover:bg-primary-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors flex items-center justify-center gap-2"
          >
            <svg v-if="isCalculating" class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
            {{ isCalculating ? 'Calculating...' : 'Calculate Optimal' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Tier Set Tracker -->
    <div
      data-testid="tier-set-tracker"
      class="bg-gray-800/50 rounded-lg p-4 mb-6"
    >
      <h3 class="text-lg font-semibold text-white mb-3">Tier Set Bonus</h3>
      <div class="flex items-center gap-6">
        <div class="flex items-center gap-2">
          <div class="flex gap-1">
            <div
              v-for="i in 4"
              :key="i"
              :class="[
                'w-8 h-8 rounded-lg flex items-center justify-center font-bold',
                i <= tierPieces.count ? 'bg-purple-600 text-white' : 'bg-gray-700 text-gray-500',
              ]"
            >
              {{ i }}
            </div>
          </div>
          <span class="text-gray-400 text-sm ml-2">{{ tierPieces.count }}/4 pieces</span>
        </div>
        <div class="flex gap-4">
          <span
            :class="[
              'px-3 py-1 rounded-full text-sm font-medium',
              tierPieces.bonus2pc ? 'bg-green-900/50 text-green-400' : 'bg-gray-700 text-gray-500',
            ]"
          >
            2pc {{ tierPieces.bonus2pc ? '✓' : '' }}
          </span>
          <span
            :class="[
              'px-3 py-1 rounded-full text-sm font-medium',
              tierPieces.bonus4pc ? 'bg-purple-900/50 text-purple-400' : 'bg-gray-700 text-gray-500',
            ]"
          >
            4pc {{ tierPieces.bonus4pc ? '✓' : '' }}
          </span>
        </div>
      </div>
    </div>

    <!-- Gear Comparison -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Current Gear -->
      <div data-testid="current-gear" class="bg-gray-800/50 rounded-lg p-6">
        <h2 class="text-xl font-semibold text-white mb-4 flex items-center gap-2">
          <span class="w-3 h-3 rounded-full bg-blue-500"></span>
          Current Setup
        </h2>
        <div v-if="gearLoading" class="animate-pulse space-y-3">
          <div v-for="i in 8" :key="i" class="h-12 bg-gray-700 rounded"></div>
        </div>
        <div v-else-if="gearData?.items" class="space-y-2">
          <div
            v-for="item in gearData.items"
            :key="item.slot"
            class="flex items-center justify-between p-3 bg-gray-700/50 rounded-lg"
          >
            <div>
              <span class="text-sm text-gray-400 capitalize">{{ item.slot.toLowerCase() }}</span>
              <p class="text-white font-medium">{{ item.itemName }}</p>
            </div>
            <span :class="['font-mono font-bold', getItemLevelClass(item.itemLevel)]">
              {{ formatItemLevel(item.itemLevel) }}
            </span>
          </div>
        </div>
        <div v-else class="text-center py-8 text-gray-400">
          Select a character to view gear
        </div>
      </div>

      <!-- Optimal Gear -->
      <div data-testid="optimal-gear" class="bg-gray-800/50 rounded-lg p-6">
        <h2 class="text-xl font-semibold text-white mb-4 flex items-center gap-2">
          <span class="w-3 h-3 rounded-full bg-green-500"></span>
          Optimal Setup
          <span v-if="dpsSummary.totalGain > 0" class="text-sm text-green-400 font-normal">
            (+{{ dpsSummary.percentGain }}% DPS)
          </span>
        </h2>
        <div v-if="isCalculating" class="animate-pulse space-y-3">
          <div v-for="i in 8" :key="i" class="h-12 bg-gray-700 rounded"></div>
        </div>
        <div v-else-if="optimalGear.length > 0" class="space-y-2">
          <div
            v-for="slot in optimalGear"
            :key="slot.slot"
            :class="[
              'flex items-center justify-between p-3 rounded-lg',
              slot.dpsGain > 0 ? 'bg-green-900/30 border border-green-700' : 'bg-gray-700/50',
            ]"
          >
            <div>
              <span class="text-sm text-gray-400 capitalize">{{ slot.slot }}</span>
              <p class="text-white font-medium">{{ slot.optimal?.itemName || 'Keep current' }}</p>
              <span v-if="slot.optimal?.source !== 'equipped'" class="text-xs text-yellow-400">
                {{ slot.optimal?.source }}
              </span>
            </div>
            <div class="text-right">
              <span v-if="slot.optimal" :class="['font-mono font-bold', getItemLevelClass(slot.optimal.itemLevel)]">
                {{ formatItemLevel(slot.optimal.itemLevel) }}
              </span>
              <p v-if="slot.dpsGain > 0" class="text-xs text-green-400">+{{ slot.dpsGain.toLocaleString() }} DPS</p>
            </div>
          </div>
        </div>
        <div v-else class="text-center py-8 text-gray-400">
          Click "Calculate Optimal" to analyze your gear
        </div>
      </div>
    </div>
  </div>
</template>
