<script setup lang="ts">
import type { LootAward } from '@/types'
import { formatDate, formatRelativeTime } from '@/utils/date'

defineProps<{
  awards: LootAward[]
}>()

const formatScore = (score: number) => score.toFixed(3)
</script>

<template>
  <div v-if="awards.length === 0" class="text-gray-400 text-center py-4">
    No recent loot awards.
  </div>

  <div v-else class="space-y-3">
    <div
      v-for="award in awards"
      :key="award.id"
      class="flex items-center justify-between p-3 bg-gray-700/30 rounded-lg"
    >
      <div>
        <span class="font-medium text-primary-300">{{ award.itemName }}</span>
        <p class="text-xs text-gray-400">
          {{ formatDate(award.awardedAt) }} · FLPS: {{ formatScore(award.flpsAtAward) }}
        </p>
      </div>
      <div
        :class="[
          'text-xs px-2 py-1 rounded',
          award.rdfExpired ? 'bg-green-900/50 text-green-400' : 'bg-yellow-900/50 text-yellow-400'
        ]"
      >
        {{ award.rdfExpired ? 'RDF Expired' : `RDF: ${formatRelativeTime(award.rdfExpiresAt!)}` }}
      </div>
    </div>
  </div>
</template>
