<script setup lang="ts">
/**
 * BarChart - Simple SVG bar chart component.
 *
 * Renders a horizontal or vertical bar chart without external dependencies.
 */
import { computed } from 'vue'

export interface BarChartData {
  label: string
  value: number
  color?: string
}

export interface BarChartProps {
  data: BarChartData[]
  height?: number
  horizontal?: boolean
  showLabels?: boolean
  showValues?: boolean
  maxValue?: number
  barColor?: string
}

const props = withDefaults(defineProps<BarChartProps>(), {
  height: 200,
  horizontal: false,
  showLabels: true,
  showValues: true,
  barColor: '#8b5cf6', // Purple-500
})

const computedMaxValue = computed(() => {
  if (props.maxValue) return props.maxValue
  return Math.max(...props.data.map((d) => d.value), 1)
})

const barWidth = computed(() => {
  if (props.horizontal) return 100 / props.data.length
  return 100 / props.data.length * 0.7 // Leave space between bars
})

const barGap = computed(() => {
  return 100 / props.data.length * 0.15
})

function getBarHeight(value: number): number {
  return (value / computedMaxValue.value) * 100
}

function formatValue(value: number): string {
  if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`
  if (value >= 1000) return `${(value / 1000).toFixed(1)}K`
  if (Number.isInteger(value)) return value.toString()
  return value.toFixed(2)
}
</script>

<template>
  <div class="bar-chart-container">
    <svg
      :viewBox="`0 0 100 ${horizontal ? data.length * 25 : 100}`"
      :style="{ height: `${height}px` }"
      class="w-full"
      preserveAspectRatio="xMidYMid meet"
    >
      <!-- Vertical bars -->
      <template v-if="!horizontal">
        <g v-for="(item, index) in data" :key="item.label">
          <!-- Bar -->
          <rect
            :x="index * (100 / data.length) + barGap"
            :y="100 - getBarHeight(item.value)"
            :width="barWidth"
            :height="getBarHeight(item.value)"
            :fill="item.color || barColor"
            class="bar"
            rx="2"
          />
          <!-- Value label -->
          <text
            v-if="showValues && item.value > 0"
            :x="index * (100 / data.length) + barGap + barWidth / 2"
            :y="100 - getBarHeight(item.value) - 2"
            text-anchor="middle"
            class="value-label"
          >
            {{ formatValue(item.value) }}
          </text>
        </g>
      </template>

      <!-- Horizontal bars -->
      <template v-else>
        <g v-for="(item, index) in data" :key="item.label">
          <!-- Background bar -->
          <rect
            x="0"
            :y="index * 25 + 5"
            width="100"
            height="15"
            fill="rgb(55 65 81 / 0.3)"
            rx="2"
          />
          <!-- Value bar -->
          <rect
            x="0"
            :y="index * 25 + 5"
            :width="getBarHeight(item.value)"
            height="15"
            :fill="item.color || barColor"
            class="bar"
            rx="2"
          />
          <!-- Value label -->
          <text
            v-if="showValues"
            :x="Math.max(getBarHeight(item.value) + 2, 5)"
            :y="index * 25 + 16"
            class="value-label horizontal"
          >
            {{ formatValue(item.value) }}
          </text>
        </g>
      </template>
    </svg>

    <!-- Labels (below chart for vertical, beside for horizontal) -->
    <div v-if="showLabels && !horizontal" class="flex justify-around mt-2">
      <span
        v-for="item in data"
        :key="item.label"
        class="text-xs text-gray-400 truncate px-1"
        :style="{ width: `${100 / data.length}%` }"
      >
        {{ item.label }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.bar-chart-container {
  width: 100%;
}

.bar {
  transition: opacity 0.2s;
}

.bar:hover {
  opacity: 0.8;
}

.value-label {
  font-size: 3px;
  fill: rgb(209 213 219); /* gray-300 */
}

.value-label.horizontal {
  font-size: 4px;
  fill: rgb(156 163 175); /* gray-400 */
}
</style>
