<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { flpsApi } from '@/api/flps'
import { lootApi } from '@/api/loot'
import ScoreCard from '@/components/ScoreCard.vue'
import ScoreBreakdown from '@/components/ScoreBreakdown.vue'
import FlpsVisualization from '@/components/FlpsVisualization.vue'
import RecentLoot from '@/components/RecentLoot.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import SkeletonProfile from '@/components/SkeletonProfile.vue'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'

// View mode for FLPS breakdown
const detailedView = ref(false)

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

    <!-- Loading state with skeletons -->
    <div v-if="flpsLoading" class="space-y-6">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <SkeletonProfile :stats-count="2" />
        <div class="lg:col-span-2">
          <SkeletonCard :lines="6" />
        </div>
      </div>
      <SkeletonCard :lines="4" />
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

        <!-- Score Breakdown with toggle -->
        <div class="card lg:col-span-2">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold">Score Breakdown</h2>
            <button
              @click="detailedView = !detailedView"
              class="text-sm text-primary-400 hover:text-primary-300 transition-colors"
            >
              {{ detailedView ? 'Simple View' : 'Detailed View' }}
            </button>
          </div>
          <ScoreBreakdown v-if="!detailedView" :rms="flpsData.rms" :ipi="flpsData.ipi" :rdf="flpsData.rdf" />
        </div>
      </div>

      <!-- Detailed FLPS Visualization -->
      <FlpsVisualization
        v-if="detailedView"
        :flps="flpsData.flps"
        :rms="flpsData.rms"
        :ipi="flpsData.ipi"
        :rdf="flpsData.rdf"
      />

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
        <div v-if="lootLoading">
          <SkeletonCard :lines="3" :show-header="false" />
        </div>
        <RecentLoot v-else-if="lootData" :awards="lootData.awards" />
        <p v-else class="text-gray-400">No loot history found.</p>
      </div>
    </div>
  </div>
</template>
