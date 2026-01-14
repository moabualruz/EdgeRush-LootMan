<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { performanceApi, type WarcraftLogsEntry } from '@/api/performance'
import { flpsApi } from '@/api/flps'
import { formatDate, formatRelativeTime } from '@/utils/date'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'

// Performance data query
const { data: performanceData, isLoading: perfLoading, error: perfError } = useQuery({
  queryKey: ['myPerformance', GUILD_ID],
  queryFn: () => performanceApi.getMyPerformance(GUILD_ID),
})

// FLPS data for MAS breakdown
const { data: flpsData, isLoading: flpsLoading } = useQuery({
  queryKey: ['myFlps', GUILD_ID],
  queryFn: () => flpsApi.getMyFlps(GUILD_ID),
})

// Warcraft Logs reports (when we have raiderId)
const { data: wclReports, isLoading: wclLoading } = useQuery({
  queryKey: ['wclReports', GUILD_ID, flpsData.value?.raiderId],
  queryFn: () => performanceApi.getWarcraftLogsReports(GUILD_ID, flpsData.value!.raiderId, 20),
  enabled: computed(() => !!flpsData.value?.raiderId),
})

// Performance trend for chart
const trendPoints = computed(() => {
  if (!performanceData.value?.performanceTrend) return []
  return performanceData.value.performanceTrend.slice(-30) // Last 30 days
})

// Chart dimensions
const chartWidth = 600
const chartHeight = 200
const chartPadding = { top: 20, right: 20, bottom: 30, left: 50 }

// SVG path for performance trend
const trendPath = computed(() => {
  if (trendPoints.value.length < 2) return ''

  const points = trendPoints.value
  const minDpa = Math.min(...points.map(p => p.dpa))
  const maxDpa = Math.max(...points.map(p => p.dpa))
  const range = maxDpa - minDpa || 1

  const xStep = (chartWidth - chartPadding.left - chartPadding.right) / (points.length - 1)

  const pathPoints = points.map((point, index) => {
    const x = chartPadding.left + index * xStep
    const y = chartPadding.top + (chartHeight - chartPadding.top - chartPadding.bottom) * (1 - (point.dpa - minDpa) / range)
    return `${index === 0 ? 'M' : 'L'} ${x} ${y}`
  })

  return pathPoints.join(' ')
})

// Helper functions
function getPercentileColor(percentile: number): string {
  if (percentile >= 95) return 'text-orange-400' // Legendary
  if (percentile >= 75) return 'text-purple-400' // Epic
  if (percentile >= 50) return 'text-blue-400' // Rare
  if (percentile >= 25) return 'text-green-400' // Uncommon
  return 'text-gray-400' // Common
}

function getPercentileBg(percentile: number): string {
  if (percentile >= 95) return 'bg-orange-900/30'
  if (percentile >= 75) return 'bg-purple-900/30'
  if (percentile >= 50) return 'bg-blue-900/30'
  if (percentile >= 25) return 'bg-green-900/30'
  return 'bg-gray-800/30'
}

function getDifficultyColor(difficulty: string): string {
  switch (difficulty.toLowerCase()) {
    case 'mythic':
      return 'text-purple-400'
    case 'heroic':
      return 'text-orange-400'
    case 'normal':
      return 'text-green-400'
    default:
      return 'text-gray-400'
  }
}

function formatMetric(value: number): string {
  if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`
  if (value >= 1000) return `${(value / 1000).toFixed(1)}K`
  return value.toFixed(1)
}

function getMasColor(mas: number): string {
  if (mas >= 0.9) return 'text-green-400'
  if (mas >= 0.7) return 'text-yellow-400'
  return 'text-red-400'
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Performance Metrics</h1>
      <div v-if="performanceData" class="text-sm text-gray-400">
        {{ performanceData.characterName }}
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="perfLoading || flpsLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="perfError" class="card bg-red-900/20 border-red-700">
      <p class="text-red-400">Failed to load performance data. Please try again.</p>
    </div>

    <!-- Content -->
    <div v-else-if="performanceData && flpsData" class="space-y-6">
      <!-- MAS Breakdown Card -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Mechanical Adherence Score (MAS)</h2>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- MAS Score -->
          <div class="flex items-center justify-center">
            <div class="text-center">
              <div :class="['text-5xl font-bold', getMasColor(flpsData.rms.mas)]">
                {{ (flpsData.rms.mas * 100).toFixed(0) }}%
              </div>
              <div class="text-gray-400 mt-2">Overall MAS</div>
            </div>
          </div>

          <!-- MAS Components -->
          <div class="space-y-4">
            <div>
              <div class="flex justify-between text-sm mb-1">
                <span class="text-gray-400">Damage Per Active (DPA)</span>
                <span class="font-medium">{{ formatMetric(performanceData.dpa) }}</span>
              </div>
              <div class="w-full bg-gray-700 rounded-full h-2">
                <div
                  class="bg-blue-500 h-2 rounded-full"
                  :style="{ width: `${Math.min(performanceData.dpa / performanceData.specAverage * 100, 100)}%` }"
                ></div>
              </div>
              <div class="text-xs text-gray-500 mt-1">
                Spec average: {{ formatMetric(performanceData.specAverage) }}
              </div>
            </div>

            <div>
              <div class="flex justify-between text-sm mb-1">
                <span class="text-gray-400">Active Damage Time (ADT)</span>
                <span class="font-medium">{{ (performanceData.adt * 100).toFixed(1) }}%</span>
              </div>
              <div class="w-full bg-gray-700 rounded-full h-2">
                <div
                  class="bg-green-500 h-2 rounded-full"
                  :style="{ width: `${performanceData.adt * 100}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- RMS Breakdown Card -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Raider Merit Score (RMS) Breakdown</h2>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="bg-gray-800/50 rounded-lg p-4 text-center">
            <div class="text-3xl font-bold text-blue-400">
              {{ (flpsData.rms.acs * 100).toFixed(0) }}%
            </div>
            <div class="text-sm text-gray-400 mt-1">ACS (Attendance)</div>
          </div>
          <div class="bg-gray-800/50 rounded-lg p-4 text-center">
            <div :class="['text-3xl font-bold', getMasColor(flpsData.rms.mas)]">
              {{ (flpsData.rms.mas * 100).toFixed(0) }}%
            </div>
            <div class="text-sm text-gray-400 mt-1">MAS (Mechanical)</div>
          </div>
          <div class="bg-gray-800/50 rounded-lg p-4 text-center">
            <div class="text-3xl font-bold text-purple-400">
              {{ (flpsData.rms.eps * 100).toFixed(0) }}%
            </div>
            <div class="text-sm text-gray-400 mt-1">EPS (Equipment)</div>
          </div>
        </div>

        <div class="mt-4 p-3 bg-gray-800/30 rounded-lg">
          <div class="flex justify-between items-center">
            <span class="text-gray-400">Combined RMS</span>
            <span class="text-xl font-bold text-primary-400">
              {{ (flpsData.rms.value * 100).toFixed(1) }}%
            </span>
          </div>
        </div>
      </div>

      <!-- Performance Trend Chart -->
      <div v-if="trendPoints.length > 1" class="card">
        <h2 class="text-lg font-semibold mb-4">Performance Trend (Last 30 Days)</h2>

        <div class="overflow-x-auto">
          <svg :width="chartWidth" :height="chartHeight" class="w-full max-w-full">
            <!-- Grid lines -->
            <g class="text-gray-700">
              <line
                v-for="i in 5"
                :key="`h-${i}`"
                :x1="chartPadding.left"
                :x2="chartWidth - chartPadding.right"
                :y1="chartPadding.top + (i - 1) * (chartHeight - chartPadding.top - chartPadding.bottom) / 4"
                :y2="chartPadding.top + (i - 1) * (chartHeight - chartPadding.top - chartPadding.bottom) / 4"
                stroke="currentColor"
                stroke-dasharray="4,4"
              />
            </g>

            <!-- Trend line -->
            <path
              :d="trendPath"
              fill="none"
              stroke="rgb(59, 130, 246)"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />

            <!-- Data points -->
            <g v-for="(point, index) in trendPoints" :key="index">
              <circle
                :cx="chartPadding.left + index * ((chartWidth - chartPadding.left - chartPadding.right) / (trendPoints.length - 1))"
                :cy="chartPadding.top + (chartHeight - chartPadding.top - chartPadding.bottom) * (1 - (point.dpa - Math.min(...trendPoints.map(p => p.dpa))) / (Math.max(...trendPoints.map(p => p.dpa)) - Math.min(...trendPoints.map(p => p.dpa)) || 1))"
                r="4"
                fill="rgb(59, 130, 246)"
              />
            </g>
          </svg>
        </div>

        <div class="text-xs text-gray-500 mt-2 text-center">
          DPA over time
        </div>
      </div>

      <!-- Warcraft Logs Reports -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Recent Warcraft Logs Reports</h2>

        <div v-if="wclLoading" class="flex items-center justify-center py-8">
          <div class="animate-spin w-6 h-6 border-2 border-primary-500 border-t-transparent rounded-full"></div>
        </div>

        <div v-else-if="wclReports?.reports?.length" class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-gray-800/50">
              <tr>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Encounter</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Difficulty</th>
                <th class="text-right px-4 py-3 text-sm font-medium text-gray-400">DPS/HPS</th>
                <th class="text-right px-4 py-3 text-sm font-medium text-gray-400">Percentile</th>
                <th class="text-right px-4 py-3 text-sm font-medium text-gray-400">Deaths</th>
                <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Date</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-700">
              <tr
                v-for="report in wclReports.reports"
                :key="`${report.reportId}-${report.encounterId}`"
                :class="['hover:bg-gray-800/30 transition-colors', getPercentileBg(report.percentile)]"
              >
                <td class="px-4 py-3">
                  <span class="font-medium">{{ report.encounterName }}</span>
                </td>
                <td class="px-4 py-3">
                  <span :class="getDifficultyColor(report.difficulty)">
                    {{ report.difficulty }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right font-mono">
                  {{ formatMetric(report.dps || report.hps || 0) }}
                </td>
                <td class="px-4 py-3 text-right">
                  <span :class="['font-bold', getPercentileColor(report.percentile)]">
                    {{ report.percentile }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right">
                  <span :class="report.deaths > 0 ? 'text-red-400' : 'text-green-400'">
                    {{ report.deaths }}
                  </span>
                </td>
                <td class="px-4 py-3 text-sm text-gray-400">
                  {{ formatDate(report.date) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else class="text-center py-8 text-gray-400">
          No Warcraft Logs reports found.
        </div>
      </div>

      <!-- Percentile Legend -->
      <div class="card">
        <h3 class="text-sm font-semibold mb-3">Percentile Legend</h3>
        <div class="flex flex-wrap gap-4 text-sm">
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-orange-400"></span>
            <span class="text-gray-400">95%+ (Legendary)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-purple-400"></span>
            <span class="text-gray-400">75-94% (Epic)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-blue-400"></span>
            <span class="text-gray-400">50-74% (Rare)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-green-400"></span>
            <span class="text-gray-400">25-49% (Uncommon)</span>
          </div>
          <div class="flex items-center space-x-2">
            <span class="w-3 h-3 rounded-full bg-gray-400"></span>
            <span class="text-gray-400">&lt;25% (Common)</span>
          </div>
        </div>
      </div>

      <!-- Last Updated -->
      <div class="text-sm text-gray-500 text-center">
        Last updated: {{ formatRelativeTime(performanceData.lastUpdated) }}
      </div>
    </div>
  </div>
</template>
