<script setup lang="ts">
import { computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { flpsApi } from '@/api/flps'
import { lootApi } from '@/api/loot'
import ScoreCard from '@/components/ScoreCard.vue'
import ScoreBreakdown from '@/components/ScoreBreakdown.vue'
import RecentLoot from '@/components/RecentLoot.vue'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'

const { data: flpsData, isLoading: flpsLoading, error: flpsError } = useQuery({
  queryKey: ['myFlps', GUILD_ID],
  queryFn: () => flpsApi.getMyFlps(GUILD_ID),
})

const { data: lootData, isLoading: lootLoading } = useQuery({
  queryKey: ['myLootHistory', GUILD_ID],
  queryFn: () => lootApi.getMyLootHistory(GUILD_ID, 5),
})

const scoreColor = computed(() => {
  if (!flpsData.value) return 'text-gray-400'
  const score = flpsData.value.flps
  if (score >= 0.8) return 'text-score-high'
  if (score >= 0.5) return 'text-score-medium'
  return 'text-score-low'
})
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Dashboard</h1>

    <!-- Loading state -->
    <div v-if="flpsLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="flpsError" class="card bg-red-900/20 border-red-700">
      <p class="text-red-400">Failed to load FLPS data. Please try again.</p>
    </div>

    <!-- Content -->
    <div v-else-if="flpsData" class="space-y-6">
      <!-- Score Overview -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Main FLPS Score -->
        <div class="card lg:col-span-1">
          <ScoreCard
            :score="flpsData.flps"
            :rank="flpsData.rank"
            :eligible="flpsData.eligible"
            :character-name="flpsData.characterName"
            :character-class="flpsData.characterClass"
          />
        </div>

        <!-- Score Breakdown -->
        <div class="card lg:col-span-2">
          <h2 class="text-lg font-semibold mb-4">Score Breakdown</h2>
          <ScoreBreakdown :rms="flpsData.rms" :ipi="flpsData.ipi" :rdf="flpsData.rdf" />
        </div>
      </div>

      <!-- Eligibility Warning -->
      <div
        v-if="!flpsData.eligible && flpsData.ineligibilityReasons?.length"
        class="card bg-yellow-900/20 border-yellow-700"
      >
        <h3 class="font-semibold text-yellow-400 mb-2">Eligibility Issues</h3>
        <ul class="list-disc list-inside text-yellow-300 space-y-1">
          <li v-for="reason in flpsData.ineligibilityReasons" :key="reason">{{ reason }}</li>
        </ul>
      </div>

      <!-- Recent Loot -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Recent Loot</h2>
        <div v-if="lootLoading" class="text-gray-400">Loading...</div>
        <RecentLoot v-else-if="lootData" :awards="lootData.awards" />
        <p v-else class="text-gray-400">No loot history found.</p>
      </div>
    </div>
  </div>
</template>
