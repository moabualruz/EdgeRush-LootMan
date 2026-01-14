<script setup lang="ts">
import { computed } from 'vue'
import type { CharacterClass } from '@/types'
import { getClassColor } from '@/utils/classColors'

const props = defineProps<{
  score: number
  rank?: number
  eligible: boolean
  characterName: string
  characterClass: CharacterClass
}>()

const scoreColor = computed(() => {
  if (props.score >= 0.8) return 'text-score-high'
  if (props.score >= 0.5) return 'text-score-medium'
  return 'text-score-low'
})

const scorePercentage = computed(() => Math.round(props.score * 100))
</script>

<template>
  <div class="text-center">
    <!-- Character info -->
    <div class="mb-4">
      <h2 :class="['text-xl font-bold', getClassColor(characterClass)]">
        {{ characterName }}
      </h2>
      <p class="text-sm text-gray-400">{{ characterClass.replace('_', ' ') }}</p>
    </div>

    <!-- Score display -->
    <div class="relative inline-flex items-center justify-center mb-4">
      <!-- Score circle background -->
      <svg class="w-32 h-32 transform -rotate-90">
        <circle
          cx="64"
          cy="64"
          r="56"
          stroke="currentColor"
          stroke-width="8"
          fill="transparent"
          class="text-gray-700"
        />
        <circle
          cx="64"
          cy="64"
          r="56"
          stroke="currentColor"
          stroke-width="8"
          fill="transparent"
          :stroke-dasharray="351.86"
          :stroke-dashoffset="351.86 * (1 - score)"
          stroke-linecap="round"
          :class="scoreColor"
        />
      </svg>
      <!-- Score value -->
      <div class="absolute inset-0 flex items-center justify-center">
        <span :class="['text-3xl font-bold', scoreColor]">
          {{ score.toFixed(3) }}
        </span>
      </div>
    </div>

    <!-- Rank -->
    <div v-if="rank" class="mb-2">
      <span class="text-gray-400">Rank</span>
      <span class="ml-2 font-semibold text-white">#{{ rank }}</span>
    </div>

    <!-- Eligibility status -->
    <div
      :class="[
        'inline-flex items-center px-3 py-1 rounded-full text-sm font-medium',
        eligible ? 'bg-green-900/50 text-green-400' : 'bg-red-900/50 text-red-400'
      ]"
    >
      {{ eligible ? '✓ Eligible' : '✗ Not Eligible' }}
    </div>
  </div>
</template>
