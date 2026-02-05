<script setup lang="ts">
/**
 * FlpsVisualization - Visual breakdown of FLPS calculation.
 *
 * Shows the FLPS formula components with interactive visualizations.
 * FLPS = (RMS × IPI) × RDF
 */
import { computed } from 'vue'
import type { RmsBreakdown, IpiBreakdown } from '@/types'
import { DonutChart, ProgressBar, DecayProjectionChart } from '@/components/charts'

const props = defineProps<{
  flps: number
  rms: RmsBreakdown
  ipi: IpiBreakdown
  rdf: number
}>()

const formatScore = (score: number) => score.toFixed(3)
const formatPercent = (score: number) => `${Math.round(score * 100)}%`

// RMS breakdown for donut chart
const rmsBreakdown = computed(() => [
  { label: 'ACS', value: props.rms.acs * 100, color: '#3b82f6' },
  { label: 'MAS', value: props.rms.mas * 100, color: '#22c55e' },
  { label: 'EPS', value: props.rms.eps * 100, color: '#a855f7' },
])

// IPI breakdown for donut chart
const ipiBreakdown = computed(() => {
  const total = props.ipi.uv + props.ipi.tierBonus
  return [
    { label: 'Upgrade Value', value: props.ipi.uv * 100, color: '#f59e0b' },
    { label: 'Tier Bonus', value: props.ipi.tierBonus * 100, color: '#ef4444' },
  ]
})

// Calculate intermediate values
const rmsTimesIpi = computed(() => props.rms.value * props.ipi.value)
</script>

<template>
  <div class="space-y-6">
    <!-- Formula Overview -->
    <div class="card bg-gray-900/50">
      <h3 class="text-sm font-semibold text-gray-400 uppercase mb-4">FLPS Formula</h3>
      <div class="flex items-center justify-center gap-4 text-lg font-mono flex-wrap">
        <div class="text-center">
          <div class="text-2xl font-bold text-blue-400">{{ formatScore(rms.value) }}</div>
          <div class="text-xs text-gray-500">RMS</div>
        </div>
        <span class="text-gray-500">×</span>
        <div class="text-center">
          <div class="text-2xl font-bold text-amber-400">{{ formatScore(ipi.value) }}</div>
          <div class="text-xs text-gray-500">IPI</div>
        </div>
        <span class="text-gray-500">×</span>
        <div class="text-center">
          <div :class="['text-2xl font-bold', rdf < 1 ? 'text-yellow-400' : 'text-green-400']">
            {{ formatScore(rdf) }}
          </div>
          <div class="text-xs text-gray-500">RDF</div>
        </div>
        <span class="text-gray-500">=</span>
        <div class="text-center">
          <div class="text-3xl font-bold text-primary-400">{{ formatScore(flps) }}</div>
          <div class="text-xs text-gray-500">FLPS</div>
        </div>
      </div>
    </div>

    <!-- Detailed Breakdown -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- RMS Section -->
      <div class="card">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-gray-400 uppercase">
            RMS (Raider Merit)
          </h3>
          <span class="text-xl font-bold text-blue-400">{{ formatScore(rms.value) }}</span>
        </div>

        <DonutChart
          :data="rmsBreakdown"
          :size="140"
          :thickness="16"
          :show-center-value="false"
          :show-legend="false"
          class="mb-4"
        />

        <div class="space-y-3">
          <div>
            <ProgressBar
              :value="rms.acs * 100"
              :max="100"
              label="ACS (Attendance)"
              color="#3b82f6"
              height="0.375rem"
            />
          </div>
          <div>
            <ProgressBar
              :value="rms.mas * 100"
              :max="100"
              label="MAS (Mechanical)"
              color="#22c55e"
              height="0.375rem"
            />
          </div>
          <div>
            <ProgressBar
              :value="rms.eps * 100"
              :max="100"
              label="EPS (Preparation)"
              color="#a855f7"
              height="0.375rem"
            />
          </div>
        </div>
      </div>

      <!-- IPI Section -->
      <div class="card">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-gray-400 uppercase">
            IPI (Item Priority)
          </h3>
          <span class="text-xl font-bold text-amber-400">{{ formatScore(ipi.value) }}</span>
        </div>

        <div class="space-y-4">
          <div>
            <ProgressBar
              :value="ipi.uv * 100"
              :max="100"
              label="Upgrade Value (UV)"
              color="#f59e0b"
              height="0.375rem"
            />
            <p class="text-xs text-gray-500 mt-1">
              Based on SimulationCraft DPS gain
            </p>
          </div>

          <div class="pt-2 border-t border-gray-700">
            <div class="flex justify-between text-sm mb-2">
              <span class="text-gray-400">Tier Set Bonus</span>
              <span class="font-mono text-amber-300">+{{ formatPercent(ipi.tierBonus) }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-gray-400">Role Multiplier</span>
              <span class="font-mono text-amber-300">{{ formatScore(ipi.roleMultiplier) }}x</span>
            </div>
          </div>
        </div>
      </div>

      <!-- RDF Section -->
      <div class="card">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-semibold text-gray-400 uppercase">
            RDF (Recency Decay)
          </h3>
          <span :class="['text-xl font-bold', rdf < 1 ? 'text-yellow-400' : 'text-green-400']">
            {{ formatScore(rdf) }}
          </span>
        </div>

        <div class="flex flex-col items-center py-4">
          <div
            class="relative w-24 h-24 rounded-full border-4"
            :class="rdf < 1 ? 'border-yellow-500' : 'border-green-500'"
          >
            <div
              class="absolute inset-2 rounded-full flex items-center justify-center"
              :class="rdf < 1 ? 'bg-yellow-500/20' : 'bg-green-500/20'"
            >
              <span :class="['text-2xl font-bold', rdf < 1 ? 'text-yellow-400' : 'text-green-400']">
                {{ formatPercent(rdf) }}
              </span>
            </div>
          </div>
        </div>

        <div class="text-center mt-4">
          <span
            :class="[
              'inline-flex items-center px-3 py-1 rounded-full text-sm font-medium',
              rdf < 1 ? 'bg-yellow-900/50 text-yellow-400' : 'bg-green-900/50 text-green-400'
            ]"
          >
            {{ rdf < 1 ? 'Recent Loot Penalty' : 'No Penalty Active' }}
          </span>
        </div>

        <p class="text-xs text-gray-500 mt-4 text-center">
          {{ rdf < 1
            ? 'Your score is reduced due to recent loot awards. This penalty will decay over time.'
            : 'You have no recent loot affecting your priority score.'
          }}
        </p>
      </div>
    </div>

    <!-- Decay Projection Section -->
    <div class="card mt-6">
      <h3 class="text-sm font-semibold text-gray-400 uppercase mb-4">
        4-Week Decay Projection (If Missing Attendance)
      </h3>
      <p class="text-xs text-gray-500 mb-4">
        Shows how your FLPS score would decay if you miss all raids for the next 4 weeks.
      </p>
      <DecayProjectionChart
        :current-flps="flps"
        :rdf-decay-rate="0.85"
        :projection-weeks="4"
        :height="140"
      />
    </div>
  </div>
</template>
