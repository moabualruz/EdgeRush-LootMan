<script setup lang="ts">
import { computed, toRef } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { lootApi } from '@/api/loot'
import { useAuthStore } from '@/stores/auth'
import { formatDate, formatRelativeTime } from '@/utils/date'
import { useWowhead } from '@/composables/useWowhead'
import WowheadItem from '@/components/WowheadItem.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { DonutChart, BarChart } from '@/components/charts'

const authStore = useAuthStore()
const guildId = computed(() => authStore.user?.guildId)

const { data, isLoading, error } = useQuery({
  queryKey: ['myLootHistory', guildId, 50],
  queryFn: () => lootApi.getMyLootHistory(guildId.value!, 50),
  enabled: computed(() => !!guildId.value),
})

// Initialize Wowhead tooltips
const dataRef = toRef(() => data.value)
useWowhead({}, [dataRef])

const formatScore = (score: number) => score.toFixed(3)

// RDF status breakdown for donut chart
const rdfBreakdown = computed(() => {
  if (!data.value?.awards) return []
  const expired = data.value.awards.filter(a => a.rdfExpired).length
  const active = data.value.awards.filter(a => !a.rdfExpired).length
  return [
    { label: 'RDF Expired', value: expired, color: '#22c55e' },
    { label: 'RDF Active', value: active, color: '#eab308' },
  ]
})

// Monthly loot breakdown for bar chart
const monthlyLoot = computed(() => {
  if (!data.value?.awards) return []
  const monthCounts: Record<string, number> = {}

  data.value.awards.forEach(award => {
    const date = new Date(award.awardedAt)
    const monthKey = date.toLocaleDateString('en-US', { month: 'short' })
    monthCounts[monthKey] = (monthCounts[monthKey] || 0) + 1
  })

  return Object.entries(monthCounts)
    .slice(-6)
    .map(([label, value]) => ({ label, value }))
})

// Average FLPS at award
const averageFlps = computed(() => {
  if (!data.value?.awards?.length) return 0
  const sum = data.value.awards.reduce((acc, a) => acc + a.flpsAtAward, 0)
  return sum / data.value.awards.length
})
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Loot History</h1>

    <!-- Loading state with skeletons -->
    <div v-if="isLoading && guildId" class="space-y-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <SkeletonCard :lines="2" />
        <SkeletonCard :lines="2" />
        <SkeletonCard :lines="2" />
      </div>
      <SkeletonCard :lines="5" />
    </div>

    <!-- No Guild state -->
    <div v-else-if="!guildId" class="card bg-blue-900/20 border-blue-700">
       <h2 class="text-lg font-semibold text-blue-400 mb-2">No Guild Found</h2>
       <p class="text-blue-300">
         You are not currently a member of any guild.
       </p>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="card bg-red-900/20 border-red-700">
      <p class="text-red-400">Failed to load loot history. Please try again.</p>
    </div>

    <!-- Content -->
    <div v-else-if="data" class="space-y-6">
      <!-- Empty state -->
      <div v-if="data.awards.length === 0" class="card text-center py-8">
        <p class="text-gray-400">No loot history found.</p>
      </div>

      <template v-else>
        <!-- Stats Summary -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="card text-center">
            <div class="text-3xl font-bold text-primary-400">{{ data.awards.length }}</div>
            <div class="text-sm text-gray-400 mt-1">Total Items</div>
          </div>
          <div class="card text-center">
            <div class="text-3xl font-bold text-blue-400">{{ formatScore(averageFlps) }}</div>
            <div class="text-sm text-gray-400 mt-1">Avg FLPS at Award</div>
          </div>
          <div class="card text-center">
            <div class="text-3xl font-bold text-green-400">
              {{ data.awards.filter(a => a.rdfExpired).length }}
            </div>
            <div class="text-sm text-gray-400 mt-1">RDF Cleared</div>
          </div>
        </div>

        <!-- Charts Row -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- RDF Status Chart -->
          <div v-if="rdfBreakdown.length > 0" class="card">
            <h2 class="text-lg font-semibold mb-4">RDF Status</h2>
            <DonutChart
              :data="rdfBreakdown"
              :size="160"
              center-label="Items"
            />
          </div>

          <!-- Monthly Loot Chart -->
          <div v-if="monthlyLoot.length > 0" class="card">
            <h2 class="text-lg font-semibold mb-4">Loot by Month</h2>
            <BarChart
              :data="monthlyLoot"
              :height="180"
              bar-color="#8b5cf6"
            />
          </div>
        </div>

        <!-- Loot items list -->
        <div class="card">
          <h2 class="text-lg font-semibold mb-4">Recent Loot</h2>
          <div class="space-y-4">
            <div
              v-for="award in data.awards"
              :key="award.id"
              class="flex items-center justify-between p-3 bg-gray-800/30 rounded-lg hover:bg-gray-800/50 transition-colors"
            >
              <div class="flex-1">
                <WowheadItem
                  :item-id="award.itemId"
                  :item-name="award.itemName"
                  quality="epic"
                />
                <p class="text-sm text-gray-400 mt-1">
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
                  <span v-if="award.rdfExpired">RDF Expired</span>
                  <span v-else>
                    RDF: {{ formatRelativeTime(award.rdfExpiresAt!) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>
