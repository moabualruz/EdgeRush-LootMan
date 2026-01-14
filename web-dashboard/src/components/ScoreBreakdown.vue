<script setup lang="ts">
import type { RmsBreakdown, IpiBreakdown } from '@/types'

defineProps<{
  rms: RmsBreakdown
  ipi: IpiBreakdown
  rdf: number
}>()

const formatScore = (score: number) => score.toFixed(3)
const formatPercent = (score: number) => `${Math.round(score * 100)}%`
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
    <!-- RMS Section -->
    <div>
      <h3 class="text-sm font-semibold text-gray-400 uppercase mb-3">
        RMS (Raider Merit Score)
      </h3>
      <div class="text-2xl font-bold text-primary-400 mb-4">
        {{ formatScore(rms.value) }}
      </div>
      <div class="space-y-2">
        <div class="flex justify-between text-sm">
          <span class="text-gray-400">ACS (Attendance)</span>
          <span class="font-mono">{{ formatPercent(rms.acs) }}</span>
        </div>
        <div class="w-full bg-gray-700 rounded-full h-1.5">
          <div
            class="bg-primary-500 h-1.5 rounded-full"
            :style="{ width: formatPercent(rms.acs) }"
          />
        </div>

        <div class="flex justify-between text-sm mt-3">
          <span class="text-gray-400">MAS (Mechanical)</span>
          <span class="font-mono">{{ formatPercent(rms.mas) }}</span>
        </div>
        <div class="w-full bg-gray-700 rounded-full h-1.5">
          <div
            class="bg-primary-500 h-1.5 rounded-full"
            :style="{ width: formatPercent(rms.mas) }"
          />
        </div>

        <div class="flex justify-between text-sm mt-3">
          <span class="text-gray-400">EPS (Preparation)</span>
          <span class="font-mono">{{ formatPercent(rms.eps) }}</span>
        </div>
        <div class="w-full bg-gray-700 rounded-full h-1.5">
          <div
            class="bg-primary-500 h-1.5 rounded-full"
            :style="{ width: formatPercent(rms.eps) }"
          />
        </div>
      </div>
    </div>

    <!-- IPI Section -->
    <div>
      <h3 class="text-sm font-semibold text-gray-400 uppercase mb-3">
        IPI (Item Priority Index)
      </h3>
      <div class="text-2xl font-bold text-primary-400 mb-4">
        {{ formatScore(ipi.value) }}
      </div>
      <div class="space-y-2">
        <div class="flex justify-between text-sm">
          <span class="text-gray-400">Upgrade Value</span>
          <span class="font-mono">{{ formatPercent(ipi.uv) }}</span>
        </div>
        <div class="w-full bg-gray-700 rounded-full h-1.5">
          <div
            class="bg-primary-500 h-1.5 rounded-full"
            :style="{ width: formatPercent(ipi.uv) }"
          />
        </div>

        <div class="flex justify-between text-sm mt-3">
          <span class="text-gray-400">Tier Bonus</span>
          <span class="font-mono">{{ formatScore(ipi.tierBonus) }}</span>
        </div>

        <div class="flex justify-between text-sm mt-3">
          <span class="text-gray-400">Role Multiplier</span>
          <span class="font-mono">{{ formatScore(ipi.roleMultiplier) }}x</span>
        </div>
      </div>
    </div>

    <!-- RDF Section -->
    <div>
      <h3 class="text-sm font-semibold text-gray-400 uppercase mb-3">
        RDF (Recency Decay Factor)
      </h3>
      <div
        :class="[
          'text-2xl font-bold mb-4',
          rdf < 1 ? 'text-yellow-400' : 'text-green-400'
        ]"
      >
        {{ formatScore(rdf) }}
      </div>
      <div class="space-y-2">
        <div class="flex justify-between text-sm">
          <span class="text-gray-400">Status</span>
          <span :class="rdf < 1 ? 'text-yellow-400' : 'text-green-400'">
            {{ rdf < 1 ? 'Recent Loot Penalty' : 'No Penalty' }}
          </span>
        </div>
        <div class="w-full bg-gray-700 rounded-full h-1.5">
          <div
            :class="['h-1.5 rounded-full', rdf < 1 ? 'bg-yellow-500' : 'bg-green-500']"
            :style="{ width: formatPercent(rdf) }"
          />
        </div>
        <p class="text-xs text-gray-500 mt-2">
          {{ rdf < 1 ? 'Your score is reduced due to recent loot. This will expire over time.' : 'You have no recent loot penalty affecting your score.' }}
        </p>
      </div>
    </div>
  </div>
</template>
