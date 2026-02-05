<script setup lang="ts">
import { computed } from 'vue'
import type { FlpsScore } from '@/types'
import { getClassColor } from '@/utils/classColors'

const props = defineProps<{
  isOpen: boolean
  raider: FlpsScore | null
}>()

const emit = defineEmits<{
  close: []
}>()

const formatScore = (score: number) => score.toFixed(3)

function handleClose() {
  emit('close')
}

const roleColorClass = computed(() => {
  switch (props.raider?.role) {
    case 'TANK':
      return 'bg-blue-600'
    case 'HEALER':
      return 'bg-green-600'
    case 'DPS':
      return 'bg-red-600'
    default:
      return 'bg-gray-600'
  }
})
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isOpen && raider"
      class="fixed inset-0 z-50 flex items-center justify-center"
      data-testid="raider-detail-modal"
    >
      <!-- Backdrop -->
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        data-testid="modal-backdrop"
        @click="handleClose"
      />

      <!-- Modal -->
      <div class="relative bg-gray-900 border border-gray-700 rounded-xl shadow-2xl w-full max-w-lg mx-4 p-6">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-3">
            <h2 class="text-xl font-bold" :class="getClassColor(raider.characterClass)">
              {{ raider.characterName }}
            </h2>
            <span :class="roleColorClass" class="px-2 py-0.5 rounded text-xs font-semibold uppercase">
              {{ raider.role }}
            </span>
          </div>
          <button
            type="button"
            class="text-gray-400 hover:text-white transition-colors"
            data-testid="close-button"
            @click="handleClose"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <!-- FLPS Score - Prominent -->
        <div class="mb-6 p-4 bg-gradient-to-r from-purple-900/50 to-indigo-900/50 rounded-lg text-center">
          <div class="text-sm text-gray-400 mb-1">FLPS Score</div>
          <div class="text-4xl font-bold text-purple-400">
            {{ formatScore(raider.flps) }}
          </div>
        </div>

        <!-- Score Breakdowns Grid -->
        <div class="grid grid-cols-2 gap-4 mb-6">
          <!-- RMS Breakdown -->
          <div class="bg-gray-800/50 rounded-lg p-4">
            <h3 class="text-sm font-semibold text-gray-300 mb-3">RMS Breakdown</h3>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-400">ACS (Attendance)</span>
                <span class="font-mono">{{ raider.rms.acs }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-400">MAS (Mechanical)</span>
                <span class="font-mono">{{ raider.rms.mas }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-400">EPS (Preparation)</span>
                <span class="font-mono">{{ raider.rms.eps }}</span>
              </div>
              <div class="flex justify-between pt-2 border-t border-gray-700">
                <span class="text-gray-300 font-medium">Total RMS</span>
                <span class="font-mono font-semibold">{{ formatScore(raider.rms.value) }}</span>
              </div>
            </div>
          </div>

          <!-- IPI Breakdown -->
          <div class="bg-gray-800/50 rounded-lg p-4">
            <h3 class="text-sm font-semibold text-gray-300 mb-3">IPI Breakdown</h3>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-400">UV (Upgrade Value)</span>
                <span class="font-mono">{{ raider.ipi.uv }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-400">Tier Bonus</span>
                <span class="font-mono">{{ raider.ipi.tierBonus }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-400">Role Multiplier</span>
                <span class="font-mono">{{ raider.ipi.roleMultiplier }}</span>
              </div>
              <div class="flex justify-between pt-2 border-t border-gray-700">
                <span class="text-gray-300 font-medium">Total IPI</span>
                <span class="font-mono font-semibold">{{ formatScore(raider.ipi.value) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- RDF -->
        <div class="mb-6 bg-gray-800/50 rounded-lg p-4">
          <div class="flex justify-between items-center">
            <div>
              <h3 class="text-sm font-semibold text-gray-300">Recent Drop Factor (RDF)</h3>
              <p class="text-xs text-gray-500">Lower value = recent loot received</p>
            </div>
            <span class="text-2xl font-mono font-semibold">{{ formatScore(raider.rdf) }}</span>
          </div>
        </div>

        <!-- Eligibility Status -->
        <div
          :class="[
            'rounded-lg p-4 border',
            raider.eligible 
              ? 'bg-green-900/20 border-green-700/50' 
              : 'bg-red-900/20 border-red-700/50'
          ]"
        >
          <div class="flex items-center gap-2 mb-2">
            <span
              :class="raider.eligible ? 'text-green-400' : 'text-red-400'"
              class="text-lg"
            >
              {{ raider.eligible ? '✓' : '✗' }}
            </span>
            <span :class="raider.eligible ? 'text-green-400' : 'text-red-400'" class="font-semibold">
              {{ raider.eligible ? 'Eligible' : 'Ineligible' }} for Loot
            </span>
          </div>
          <template v-if="!raider.eligible && raider.ineligibilityReasons?.length">
            <ul class="text-sm text-red-300/80 list-disc list-inside space-y-1">
              <li v-for="reason in raider.ineligibilityReasons" :key="reason">
                {{ reason }}
              </li>
            </ul>
          </template>
        </div>
      </div>
    </div>
  </Teleport>
</template>
