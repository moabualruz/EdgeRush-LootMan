<script setup lang="ts">
/**
 * DonutChart - Simple SVG donut/pie chart component.
 *
 * Renders a donut chart with customizable segments.
 */
import { computed } from 'vue'

export interface DonutChartSegment {
  label: string
  value: number
  color: string
}

export interface DonutChartProps {
  data: DonutChartSegment[]
  size?: number
  thickness?: number
  showLegend?: boolean
  showCenterValue?: boolean
  centerLabel?: string
}

const props = withDefaults(defineProps<DonutChartProps>(), {
  size: 160,
  thickness: 20,
  showLegend: true,
  showCenterValue: true,
  centerLabel: 'Total',
})

const total = computed(() => props.data.reduce((sum, d) => sum + d.value, 0))

const radius = computed(() => (props.size - props.thickness) / 2)
const circumference = computed(() => 2 * Math.PI * radius.value)

const segments = computed(() => {
  let currentOffset = 0
  return props.data.map((segment) => {
    const percentage = total.value > 0 ? segment.value / total.value : 0
    const dashLength = percentage * circumference.value
    const dashOffset = currentOffset
    currentOffset += dashLength

    return {
      ...segment,
      percentage,
      dashLength,
      dashOffset,
    }
  })
})

function formatValue(value: number): string {
  if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`
  if (value >= 1000) return `${(value / 1000).toFixed(1)}K`
  return value.toString()
}

function formatPercentage(percentage: number): string {
  return `${(percentage * 100).toFixed(1)}%`
}
</script>

<template>
  <div class="donut-chart-container flex flex-col items-center gap-4">
    <div class="relative" :style="{ width: `${size}px`, height: `${size}px` }">
      <svg
        :viewBox="`0 0 ${size} ${size}`"
        class="transform -rotate-90"
      >
        <!-- Background circle -->
        <circle
          :cx="size / 2"
          :cy="size / 2"
          :r="radius"
          fill="none"
          stroke="rgb(55 65 81 / 0.3)"
          :stroke-width="thickness"
        />

        <!-- Segments -->
        <circle
          v-for="(segment, i) in segments"
          :key="i"
          :cx="size / 2"
          :cy="size / 2"
          :r="radius"
          fill="none"
          :stroke="segment.color"
          :stroke-width="thickness"
          :stroke-dasharray="`${segment.dashLength} ${circumference - segment.dashLength}`"
          :stroke-dashoffset="-segment.dashOffset"
          class="segment"
          stroke-linecap="butt"
        />
      </svg>

      <!-- Center value -->
      <div
        v-if="showCenterValue"
        class="absolute inset-0 flex flex-col items-center justify-center"
      >
        <span class="text-2xl font-bold text-white">{{ formatValue(total) }}</span>
        <span class="text-xs text-gray-400">{{ centerLabel }}</span>
      </div>
    </div>

    <!-- Legend -->
    <div v-if="showLegend" class="flex flex-wrap justify-center gap-4">
      <div
        v-for="segment in segments"
        :key="segment.label"
        class="flex items-center gap-2"
      >
        <span
          class="w-3 h-3 rounded-full"
          :style="{ backgroundColor: segment.color }"
        />
        <span class="text-sm text-gray-300">
          {{ segment.label }}
          <span class="text-gray-500">({{ formatPercentage(segment.percentage) }})</span>
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.segment {
  transition: stroke-width 0.2s, opacity 0.2s;
}

.segment:hover {
  stroke-width: 24;
  opacity: 0.9;
}
</style>
