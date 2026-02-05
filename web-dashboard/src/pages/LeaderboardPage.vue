<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { flpsApi } from '@/api/flps'
import { useAuthStore } from '@/stores/auth'
import { useGuildContextStore } from '@/stores/guildContext'
import type { Role, FlpsScore, LeaderboardEntry } from '@/types'
import { getClassColor } from '@/utils/classColors'
import RaiderDetailModal from '@/components/RaiderDetailModal.vue'

const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()
// Default to 'dod' guild for development when no guild context is set
const guildId = computed(() => guildContextStore.currentGuildId || authStore.user?.guildId || 'dod')
const roleFilter = ref<Role | ''>('')

const { data, isLoading, error } = useQuery({
  queryKey: ['leaderboard', guildId, roleFilter],
  queryFn: () => flpsApi.getLeaderboard(guildId.value!, roleFilter.value || undefined, 50),
  enabled: computed(() => !!guildId.value),
})

// Fetch full FLPS report to get score breakdowns
const { data: flpsReport } = useQuery({
  queryKey: ['flpsReport', guildId],
  queryFn: () => flpsApi.getFlpsReport(guildId.value!),
  enabled: computed(() => !!guildId.value),
})

const formatScore = (score: number) => score.toFixed(3)

const isCurrentUser = (characterName: string) => {
  return authStore.user?.linkedCharacters.some((c) => c.characterName === characterName)
}

// Modal state
const isModalOpen = ref(false)
const selectedRaider = ref<FlpsScore | null>(null)

function openRaiderDetail(entry: LeaderboardEntry) {
  // Find the full FlpsScore from the report
  const fullScore = flpsReport.value?.raiders.find(r => r.raiderId === entry.raiderId)
  if (fullScore) {
    selectedRaider.value = fullScore
    isModalOpen.value = true
  }
}

function closeModal() {
  isModalOpen.value = false
  selectedRaider.value = null
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">FLPS Leaderboard</h1>

      <!-- Role filter -->
      <!-- Role filter -->
      <div class="relative w-48">
        <select
          v-model="roleFilter"
          class="w-full appearance-none bg-secondary text-foreground border border-input rounded-md px-3 py-2 pr-8 focus:outline-none focus:ring-2 focus:ring-primary/50 cursor-pointer shadow-sm hover:bg-secondary/80 transition-colors"
        >
          <option value="" class="bg-card text-foreground">All Roles</option>
          <option value="TANK" class="bg-card text-foreground">Tank</option>
          <option value="HEALER" class="bg-card text-foreground">Healer</option>
          <option value="DPS" class="bg-card text-foreground">DPS</option>
        </select>
        <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-muted-foreground">
          <svg class="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"><path d="M9.293 12.95l.707.707L15.657 8l-1.414-1.414L10 10.828 5.757 6.586 4.343 8z"/></svg>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading && guildId" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- No Guild state -->
    <div v-else-if="!guildId" class="alert alert-info mb-6">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
      <div>
        <h5 class="alert-title">No Guild Found</h5>
        <div class="alert-description">You are not currently a member of any guild. Please join a guild to view the leaderboard.</div>
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="alert alert-error mb-6">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <div>
        <h5 class="alert-title">Error Loading Leaderboard</h5>
        <div class="alert-description">Failed to load leaderboard data. Please try again later.</div>
      </div>
    </div>

    <!-- Leaderboard table -->
    <div v-else-if="data" class="card overflow-hidden p-0">
      <div class="overflow-x-auto">
      <table class="w-full min-w-[500px]">
        <thead class="bg-gray-700/50">
          <tr>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-300">Rank</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-300">Character</th>
            <th class="px-4 py-3 text-left text-sm font-semibold text-gray-300">Role</th>
            <th class="px-4 py-3 text-right text-sm font-semibold text-gray-300">FLPS</th>
            <th class="px-4 py-3 text-center text-sm font-semibold text-gray-300">Eligible</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-700">
          <tr
            v-for="entry in data.entries"
            :key="entry.raiderId"
            class="hover:bg-gray-700/30 transition-colors cursor-pointer"
            @click="openRaiderDetail(entry)"
          >
            <td class="px-4 py-3">
              <span class="font-semibold">
                {{ entry.rank <= 3 ? ['🥇', '🥈', '🥉'][entry.rank - 1] : `#${entry.rank}` }}
              </span>
            </td>
            <td class="px-4 py-3">
              <span class="font-medium" :class="getClassColor(entry.characterClass)">
                {{ entry.characterName }}
              </span>
            </td>
            <td class="px-4 py-3 text-gray-400">{{ entry.role }}</td>
            <td class="px-4 py-3 text-right font-mono text-score-low">
                {{ formatScore(entry.flps) }}
            </td>
            <td class="px-4 py-3 text-center">
              <span :class="entry.eligible ? 'text-green-400' : 'text-red-400'">
                {{ entry.eligible ? '✓' : '✗' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      </div>

      <!-- Empty state -->
      <div v-if="data.entries.length === 0" class="p-8 text-center text-gray-400">
        No raiders found for the selected filter.
      </div>
    </div>

    <!-- Raider Detail Modal -->
    <RaiderDetailModal
      :is-open="isModalOpen"
      :raider="selectedRaider"
      :guild-id="guildId"
      @close="closeModal"
    />
  </div>
</template>
