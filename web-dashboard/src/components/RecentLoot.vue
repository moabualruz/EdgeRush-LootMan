<script setup lang="ts">
import { toRef } from 'vue'
import type { LootAward } from '@/types'
import { formatDate, formatRelativeTime } from '@/utils/date'
import { useWowhead } from '@/composables/useWowhead'
import WowheadItem from '@/components/WowheadItem.vue'

const props = defineProps<{
  awards: LootAward[]
}>()

// Initialize Wowhead tooltips and refresh when awards change
const awardsRef = toRef(props, 'awards')
useWowhead({}, [awardsRef])

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
        <WowheadItem
          :item-id="award.itemId"
          :item-name="award.itemName"
          quality="epic"
        />
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
