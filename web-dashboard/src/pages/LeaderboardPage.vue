<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { flpsApi } from '@/api/flps'
import { useAuthStore } from '@/stores/auth'
import type { Role } from '@/types'
import { getClassColor } from '@/utils/classColors'

const authStore = useAuthStore()
const guildId = computed(() => authStore.user?.guildId)
const roleFilter = ref<Role | ''>('')

const { data, isLoading, error } = useQuery({
  queryKey: ['leaderboard', guildId, roleFilter],
  queryFn: () => flpsApi.getLeaderboard(guildId.value!, roleFilter.value || undefined, 50),
  enabled: computed(() => !!guildId.value),
})

const formatScore = (score: number) => score.toFixed(3)

const isCurrentUser = (characterName: string) => {
  return authStore.user?.linkedCharacters.some((c) => c.characterName === characterName)
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">FLPS Leaderboard</h1>

      <!-- Role filter -->
      <select v-model="roleFilter" class="input w-40">
        <option value="">All Roles</option>
        <option value="TANK">Tank</option>
        <option value="HEALER">Healer</option>
        <option value="DPS">DPS</option>
      </select>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading && guildId" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
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
      <p class="text-red-400">Failed to load leaderboard. Please try again.</p>
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
            :class="[
              isCurrentUser(entry.characterName) ? 'bg-primary-900/30' : 'hover:bg-gray-700/30',
              'transition-colors'
            ]"
          >
            <td class="px-4 py-3">
              <span
                :class="[
                  'font-semibold',
                  entry.rank === 1 ? 'text-yellow-400' : '',
                  entry.rank === 2 ? 'text-gray-300' : '',
                  entry.rank === 3 ? 'text-amber-600' : ''
                ]"
              >
                {{ entry.rank === 1 ? '🥇' : entry.rank === 2 ? '🥈' : entry.rank === 3 ? '🥉' : `#${entry.rank}` }}
              </span>
            </td>
            <td class="px-4 py-3">
              <span :class="['font-medium', getClassColor(entry.characterClass)]">
                {{ entry.characterName }}
              </span>
              <span v-if="isCurrentUser(entry.characterName)" class="ml-2 text-xs text-primary-400">(You)</span>
            </td>
            <td class="px-4 py-3 text-gray-400">{{ entry.role }}</td>
            <td class="px-4 py-3 text-right font-mono">
              <span
                :class="[
                  entry.flps >= 0.8 ? 'text-score-high' : entry.flps >= 0.5 ? 'text-score-medium' : 'text-score-low'
                ]"
              >
                {{ formatScore(entry.flps) }}
              </span>
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
  </div>
</template>
