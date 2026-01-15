<script setup lang="ts">
/**
 * ProgressBar - Animated progress bar component.
 *
 * Displays a progress indicator with optional label and value.
 */

export interface ProgressBarProps {
  value: number
  max?: number
  label?: string
  showValue?: boolean
  showPercentage?: boolean
  color?: string
  height?: string
  animated?: boolean
}

const props = withDefaults(defineProps<ProgressBarProps>(), {
  max: 100,
  showValue: true,
  showPercentage: true,
  color: '#8b5cf6', // Purple-500
  height: '0.5rem',
  animated: true,
})

const percentage = computed(() => {
  if (props.max === 0) return 0
  return Math.min(100, (props.value / props.max) * 100)
})

function formatValue(value: number): string {
  if (value >= 1000000) return `${(value / 1000000).toFixed(1)}M`
  if (value >= 1000) return `${(value / 1000).toFixed(1)}K`
  if (Number.isInteger(value)) return value.toString()
  return value.toFixed(2)
}

import { computed } from 'vue'
</script>

<template>
  <div class="progress-bar-container">
    <!-- Header -->
    <div v-if="label || showValue || showPercentage" class="flex justify-between items-center mb-1">
      <span v-if="label" class="text-sm text-gray-300">{{ label }}</span>
      <div class="flex gap-2 text-sm text-gray-400">
        <span v-if="showValue">{{ formatValue(value) }} / {{ formatValue(max) }}</span>
        <span v-if="showPercentage">({{ percentage.toFixed(1) }}%)</span>
      </div>
    </div>

    <!-- Progress track -->
    <div
      class="progress-track rounded-full overflow-hidden bg-gray-700/50"
      :style="{ height }"
    >
      <div
        class="progress-fill h-full rounded-full"
        :class="{ 'progress-animated': animated }"
        :style="{
          width: `${percentage}%`,
          backgroundColor: color,
        }"
      />
    </div>
  </div>
</template>

<style scoped>
.progress-bar-container {
  width: 100%;
}

.progress-fill {
  transition: width 0.5s ease-out;
}

.progress-animated {
  position: relative;
  overflow: hidden;
}

.progress-animated::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
</style>
