<script setup lang="ts">
/**
 * LineChart - Simple SVG line chart component.
 *
 * Renders a line chart with optional area fill and data points.
 */
import { computed } from 'vue'

export interface LineChartDataPoint {
  x: string | number
  y: number
}

export interface LineChartProps {
  data: LineChartDataPoint[]
  height?: number
  showArea?: boolean
  showPoints?: boolean
  showGrid?: boolean
  lineColor?: string
  areaColor?: string
  pointColor?: string
  minY?: number
  maxY?: number
}

const props = withDefaults(defineProps<LineChartProps>(), {
  height: 200,
  showArea: true,
  showPoints: true,
  showGrid: true,
  lineColor: '#8b5cf6', // Purple-500
  areaColor: 'rgba(139, 92, 246, 0.2)', // Purple-500 with opacity
  pointColor: '#a78bfa', // Purple-400
})

const padding = { top: 10, right: 10, bottom: 20, left: 10 }
const chartWidth = 100 - padding.left - padding.right
const chartHeight = 100 - padding.top - padding.bottom

const computedMinY = computed(() => props.minY ?? 0)

const computedMaxY = computed(() => {
  if (props.maxY !== undefined) return props.maxY
  const max = Math.max(...props.data.map((d) => d.y))
  return max > 0 ? max * 1.1 : 1 // Add 10% padding
})

const yRange = computed(() => computedMaxY.value - computedMinY.value)

const points = computed(() => {
  if (props.data.length === 0) return []

  return props.data.map((point, index) => {
    const x = padding.left + (index / Math.max(props.data.length - 1, 1)) * chartWidth
    const normalizedY = (point.y - computedMinY.value) / yRange.value
    const y = padding.top + chartHeight - normalizedY * chartHeight
    return { x, y, data: point }
  })
})

const linePath = computed(() => {
  if (points.value.length === 0) return ''
  return points.value
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`)
    .join(' ')
})

const areaPath = computed(() => {
  if (points.value.length === 0) return ''
  const baseline = padding.top + chartHeight
  const first = points.value[0]
  const last = points.value[points.value.length - 1]
  return `${linePath.value} L ${last.x} ${baseline} L ${first.x} ${baseline} Z`
})

// Grid lines (horizontal)
const gridLines = computed(() => {
  const lines = []
  for (let i = 0; i <= 4; i++) {
    const y = padding.top + (chartHeight / 4) * i
    lines.push({ y, value: computedMaxY.value - (yRange.value / 4) * i })
  }
  return lines
})
</script>

<template>
  <div class="line-chart-container">
    <svg
      viewBox="0 0 100 100"
      :style="{ height: `${height}px` }"
      class="w-full"
      preserveAspectRatio="xMidYMid meet"
    >
      <!-- Grid lines -->
      <g v-if="showGrid" class="grid-lines">
        <line
          v-for="(line, i) in gridLines"
          :key="i"
          :x1="padding.left"
          :y1="line.y"
          :x2="100 - padding.right"
          :y2="line.y"
          stroke="rgb(55 65 81 / 0.3)"
          stroke-width="0.2"
        />
      </g>

      <!-- Area fill -->
      <path
        v-if="showArea && data.length > 1"
        :d="areaPath"
        :fill="areaColor"
        class="area"
      />

      <!-- Line -->
      <path
        v-if="data.length > 1"
        :d="linePath"
        :stroke="lineColor"
        stroke-width="0.8"
        fill="none"
        stroke-linecap="round"
        stroke-linejoin="round"
        class="line"
      />

      <!-- Data points -->
      <g v-if="showPoints">
        <circle
          v-for="(point, i) in points"
          :key="i"
          :cx="point.x"
          :cy="point.y"
          r="1.5"
          :fill="pointColor"
          class="point"
        />
      </g>

      <!-- X-axis labels -->
      <g v-if="data.length > 0" class="x-labels">
        <text
          v-for="(point, i) in points"
          :key="i"
          :x="point.x"
          :y="100 - 2"
          text-anchor="middle"
          class="axis-label"
        >
          {{ point.data.x }}
        </text>
      </g>
    </svg>
  </div>
</template>

<style scoped>
.line-chart-container {
  width: 100%;
}

.line {
  transition: stroke-width 0.2s;
}

.area {
  transition: opacity 0.2s;
}

.point {
  transition: r 0.2s;
}

.point:hover {
  r: 2.5;
}

.axis-label {
  font-size: 3px;
  fill: rgb(156 163 175); /* gray-400 */
}
</style>
