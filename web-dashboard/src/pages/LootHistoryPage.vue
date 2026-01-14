<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { lootApi } from '@/api/loot'
import { formatDate, formatRelativeTime } from '@/utils/date'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'

const { data, isLoading, error } = useQuery({
  queryKey: ['myLootHistory', GUILD_ID, 20],
  queryFn: () => lootApi.getMyLootHistory(GUILD_ID, 20),
})

const formatScore = (score: number) => score.toFixed(3)
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Loot History</h1>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="card bg-red-900/20 border-red-700">
      <p class="text-red-400">Failed to load loot history. Please try again.</p>
    </div>

    <!-- Content -->
    <div v-else-if="data" class="space-y-4">
      <!-- Empty state -->
      <div v-if="data.awards.length === 0" class="card text-center py-8">
        <p class="text-gray-400">No loot history found.</p>
      </div>

      <!-- Loot items -->
      <div v-else class="space-y-4">
        <div
          v-for="award in data.awards"
          :key="award.id"
          class="card flex items-center justify-between"
        >
          <div class="flex-1">
            <h3 class="font-semibold text-primary-400">{{ award.itemName }}</h3>
            <p class="text-sm text-gray-400">
              Awarded {{ formatDate(award.awardedAt) }} · FLPS: {{ formatScore(award.flpsAtAward) }}
            </p>
          </div>

          <div class="text-right">
            <div
              :class="[
                'inline-flex items-center px-3 py-1 rounded-full text-sm font-medium',
                award.rdfExpired
                  ? 'bg-green-900/50 text-green-400'
                  : 'bg-yellow-900/50 text-yellow-400'
              ]"
            >
              <span v-if="award.rdfExpired">RDF Expired ✓</span>
              <span v-else>
                RDF Active · Expires {{ formatRelativeTime(award.rdfExpiresAt!) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
