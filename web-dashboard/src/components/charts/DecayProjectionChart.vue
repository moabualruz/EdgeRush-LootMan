<script setup lang="ts">
/**
 * DecayProjectionChart - Shows projected FLPS decay over time.
 *
 * Renders a dotted line chart showing how FLPS will decay over the next
 * N weeks if the raider misses attendance.
 */
import { computed } from 'vue'

export interface DecayProjectionChartProps {
  currentFlps: number
  /** Weekly decay multiplier (e.g., 0.85 means 15% decay per week) */
  rdfDecayRate?: number
  /** Number of weeks to project (default: 4) */
  projectionWeeks?: number
  height?: number
  lineColor?: string
  projectedColor?: string
}

const props = withDefaults(defineProps<DecayProjectionChartProps>(), {
  rdfDecayRate: 0.85,
  projectionWeeks: 4,
  height: 120,
  lineColor: '#8b5cf6',
  projectedColor: '#ef4444',
})

const padding = { top: 15, right: 15, bottom: 25, left: 40 }
const chartWidth = 100 - padding.left - padding.right
const chartHeight = 100 - padding.top - padding.bottom

// Generate decay projection data points
const projectionData = computed(() => {
  const points: { week: number; flps: number; isProjected: boolean }[] = []
  
  // Current week (week 0)
  points.push({ week: 0, flps: props.currentFlps, isProjected: false })
  
  // Projected weeks
  let currentValue = props.currentFlps
  for (let week = 1; week <= props.projectionWeeks; week++) {
    currentValue *= props.rdfDecayRate
    points.push({ week, flps: currentValue, isProjected: true })
  }
  
  return points
})

// Min/Max for Y-axis scaling
const minY = computed(() => 0)
const maxY = computed(() => {
  const max = Math.max(...projectionData.value.map(d => d.flps))
  return max > 0 ? max * 1.15 : 1
})
const yRange = computed(() => maxY.value - minY.value)

// Convert data to SVG coordinates
const chartPoints = computed(() => {
  return projectionData.value.map((point, index) => {
    const x = padding.left + (index / props.projectionWeeks) * chartWidth
    const normalizedY = (point.flps - minY.value) / yRange.value
    const y = padding.top + chartHeight - normalizedY * chartHeight
    return { x, y, ...point }
  })
})

// SVG path for decay line (dotted for projected)
const linePath = computed(() => {
  if (chartPoints.value.length === 0) return ''
  return chartPoints.value
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`)
    .join(' ')
})

// Y-axis labels (0, 25%, 50%, 75%, 100% of max)
const yAxisLabels = computed(() => {
  const labels = []
  for (let i = 0; i <= 4; i++) {
    const value = maxY.value - (yRange.value / 4) * i
    const y = padding.top + (chartHeight / 4) * i
    labels.push({ y, value: value.toFixed(2) })
  }
  return labels
})

// X-axis labels (weeks)
const xAxisLabels = computed(() => {
  return chartPoints.value.map(p => ({
    x: p.x,
    label: p.week === 0 ? 'Now' : `W${p.week}`
  }))
})

const formatScore = (score: number) => score.toFixed(3)
</script>

<template>
  <div class="decay-projection-chart">
    <svg
      viewBox="0 0 100 100"
      :style="{ height: `${height}px` }"
      class="w-full"
      preserveAspectRatio="xMidYMid meet"
    >
      <!-- Grid lines -->
      <g class="grid-lines">
        <line
          v-for="(label, i) in yAxisLabels"
          :key="`grid-${i}`"
          :x1="padding.left"
          :y1="label.y"
          :x2="100 - padding.right"
          :y2="label.y"
          stroke="rgb(55 65 81 / 0.3)"
          stroke-width="0.2"
        />
      </g>

      <!-- Y-axis labels -->
      <g class="y-axis-labels">
        <text
          v-for="(label, i) in yAxisLabels"
          :key="`y-${i}`"
          :x="padding.left - 2"
          :y="label.y"
          text-anchor="end"
          dominant-baseline="middle"
          class="axis-label"
        >
          {{ label.value }}
        </text>
      </g>

      <!-- Decay projection line (dotted) -->
      <path
        v-if="chartPoints.length > 1"
        :d="linePath"
        :stroke="projectedColor"
        stroke-width="0.8"
        fill="none"
        stroke-dasharray="2,1"
        stroke-linecap="round"
        stroke-linejoin="round"
        class="decay-line"
      />

      <!-- Current value point (solid) -->
      <circle
        v-if="chartPoints.length > 0"
        :cx="chartPoints[0].x"
        :cy="chartPoints[0].y"
        r="2"
        :fill="lineColor"
        class="current-point"
      />

      <!-- Projected value points -->
      <circle
        v-for="(point, i) in chartPoints.slice(1)"
        :key="`point-${i}`"
        :cx="point.x"
        :cy="point.y"
        r="1.5"
        :fill="projectedColor"
        fill-opacity="0.7"
        class="projected-point"
      />

      <!-- X-axis labels -->
      <g class="x-axis-labels">
        <text
          v-for="(label, i) in xAxisLabels"
          :key="`x-${i}`"
          :x="label.x"
          :y="100 - 5"
          text-anchor="middle"
          class="axis-label"
        >
          {{ label.label }}
        </text>
      </g>

      <!-- End value annotation -->
      <g v-if="chartPoints.length > 1" class="end-annotation">
        <text
          :x="chartPoints[chartPoints.length - 1].x + 1"
          :y="chartPoints[chartPoints.length - 1].y"
          text-anchor="start"
          dominant-baseline="middle"
          class="end-value"
        >
          {{ formatScore(chartPoints[chartPoints.length - 1].flps) }}
        </text>
      </g>
    </svg>

    <!-- Legend -->
    <div class="flex items-center justify-center gap-4 mt-2 text-xs text-gray-400">
      <div class="flex items-center gap-1">
        <span class="w-3 h-3 rounded-full" :style="{ backgroundColor: lineColor }"></span>
        <span>Current</span>
      </div>
      <div class="flex items-center gap-1">
        <span class="w-3 h-0.5" :style="{ backgroundColor: projectedColor, borderStyle: 'dashed' }"></span>
        <span>Projected Decay</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.decay-projection-chart {
  width: 100%;
}

.axis-label {
  font-size: 3px;
  fill: rgb(156 163 175);
}

.end-value {
  font-size: 3px;
  fill: #ef4444;
  font-weight: 600;
}

.decay-line {
  transition: stroke-dasharray 0.3s;
}

.current-point {
  transition: r 0.2s;
}

.current-point:hover {
  r: 3;
}

.projected-point {
  transition: r 0.2s, fill-opacity 0.2s;
}

.projected-point:hover {
  r: 2.5;
  fill-opacity: 1;
}
</style>
