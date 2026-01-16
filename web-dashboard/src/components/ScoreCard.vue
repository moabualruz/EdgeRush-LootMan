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
  if (props.score >= 0.8) return 'text-green-400 drop-shadow-sm'
  if (props.score >= 0.5) return 'text-yellow-400 drop-shadow-sm'
  return 'text-red-400 drop-shadow-sm'
})
</script>

<template>
  <div class="text-center relative z-10">
    <!-- Character info -->
    <div class="mb-4">
      <h2 :class="['text-xl font-bold tracking-tight', getClassColor(characterClass)]">
        {{ characterName }}
      </h2>
      <p class="text-sm text-muted-foreground uppercase tracking-wider text-xs font-semibold">{{ characterClass.replace('_', ' ') }}</p>
    </div>

    <!-- Score display -->
    <div class="relative inline-flex items-center justify-center mb-4 group cursor-default">
      <!-- Glow effect -->
      <div class="absolute inset-0 bg-primary/20 blur-xl rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
      
      <!-- Score circle background -->
      <svg class="w-32 h-32 transform -rotate-90 drop-shadow-xl relative z-10">
        <circle
          cx="64"
          cy="64"
          r="56"
          stroke="currentColor"
          stroke-width="8"
          fill="transparent"
          class="text-muted/20"
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
          class="transition-all duration-1000 ease-out"
          :class="scoreColor"
        />
      </svg>
      <!-- Score value -->
      <div class="absolute inset-0 flex items-center justify-center z-10">
        <span :class="['text-3xl font-bold tracking-tighter', scoreColor]">
          {{ score.toFixed(3) }}
        </span>
      </div>
    </div>

    <!-- Rank -->
    <div v-if="rank" class="mb-3">
      <span class="text-muted-foreground text-sm uppercase tracking-wide mr-2">Guild Rank</span>
      <span class="font-bold text-white text-lg">#{{ rank }}</span>
    </div>

    <!-- Eligibility status -->
    <div
      :class="[
        'inline-flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-semibold uppercase tracking-wide border transition-colors',
        eligible 
          ? 'bg-green-500/10 text-green-400 border-green-500/20 shadow-sm shadow-green-500/10' 
          : 'bg-red-500/10 text-red-400 border-red-500/20 shadow-sm shadow-red-500/10'
      ]"
    >
      <span class="w-1.5 h-1.5 rounded-full" :class="eligible ? 'bg-green-400' : 'bg-red-400'"></span>
      {{ eligible ? 'Loot Eligible' : 'Not Eligible' }}
    </div>
  </div>
</template>
