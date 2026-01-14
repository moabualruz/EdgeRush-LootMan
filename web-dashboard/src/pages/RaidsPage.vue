<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { raidsApi, type Raid, type RaidStatus } from '@/api/raids'
import { formatDate, formatRelativeTime } from '@/utils/date'
import { useRouter } from 'vue-router'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'
const router = useRouter()

// Tab state
const activeTab = ref<'upcoming' | 'past'>('upcoming')

// Queries
const { data: upcomingRaids, isLoading: upcomingLoading } = useQuery({
  queryKey: ['upcomingRaids', GUILD_ID],
  queryFn: () => raidsApi.getUpcomingRaids(GUILD_ID, 20),
})

const { data: pastRaids, isLoading: pastLoading } = useQuery({
  queryKey: ['pastRaids', GUILD_ID],
  queryFn: () => raidsApi.getPastRaids(GUILD_ID, 20),
})

const isLoading = computed(() => activeTab.value === 'upcoming' ? upcomingLoading.value : pastLoading.value)
const displayedRaids = computed(() => activeTab.value === 'upcoming' ? upcomingRaids.value : pastRaids.value)

// Helper functions
function getDifficultyColor(difficulty: Raid['difficulty']): string {
  switch (difficulty) {
    case 'MYTHIC':
      return 'text-purple-400'
    case 'HEROIC':
      return 'text-orange-400'
    case 'NORMAL':
      return 'text-green-400'
    default:
      return 'text-gray-400'
  }
}

function getDifficultyBg(difficulty: Raid['difficulty']): string {
  switch (difficulty) {
    case 'MYTHIC':
      return 'bg-purple-900/30'
    case 'HEROIC':
      return 'bg-orange-900/30'
    case 'NORMAL':
      return 'bg-green-900/30'
    default:
      return 'bg-gray-800/30'
  }
}

function getStatusColor(status: RaidStatus): string {
  switch (status) {
    case 'SCHEDULED':
      return 'text-blue-400'
    case 'IN_PROGRESS':
      return 'text-yellow-400'
    case 'COMPLETED':
      return 'text-green-400'
    case 'CANCELLED':
      return 'text-red-400'
    default:
      return 'text-gray-400'
  }
}

function getStatusLabel(status: RaidStatus): string {
  switch (status) {
    case 'SCHEDULED':
      return 'Scheduled'
    case 'IN_PROGRESS':
      return 'In Progress'
    case 'COMPLETED':
      return 'Completed'
    case 'CANCELLED':
      return 'Cancelled'
    default:
      return status
  }
}

function getSignupRatio(raid: Raid): { text: string; color: string } {
  const ratio = raid.signupCount / raid.maxPlayers
  if (ratio >= 1) {
    return { text: 'Full', color: 'text-green-400' }
  } else if (ratio >= 0.8) {
    return { text: `${raid.signupCount}/${raid.maxPlayers}`, color: 'text-yellow-400' }
  } else {
    return { text: `${raid.signupCount}/${raid.maxPlayers}`, color: 'text-gray-400' }
  }
}

function viewRaid(raidId: number) {
  router.push(`/raids/${raidId}`)
}

function formatRaidTime(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Raids</h1>

    <!-- Tab navigation -->
    <div class="flex items-center space-x-2 mb-6">
      <button
        @click="activeTab = 'upcoming'"
        :class="[
          'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
          activeTab === 'upcoming' ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-400 hover:bg-gray-600'
        ]"
      >
        Upcoming
      </button>
      <button
        @click="activeTab = 'past'"
        :class="[
          'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
          activeTab === 'past' ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-400 hover:bg-gray-600'
        ]"
      >
        Past Raids
      </button>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Content -->
    <div v-else-if="displayedRaids" class="space-y-4">
      <div v-if="displayedRaids.length === 0" class="card text-center py-8">
        <p class="text-gray-400">
          {{ activeTab === 'upcoming' ? 'No upcoming raids scheduled.' : 'No past raids found.' }}
        </p>
      </div>

      <!-- Raid Cards -->
      <div
        v-for="raid in displayedRaids"
        :key="raid.id"
        @click="viewRaid(raid.id)"
        :class="[
          'card cursor-pointer hover:border-primary-500 transition-all',
          getDifficultyBg(raid.difficulty)
        ]"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center space-x-3">
              <h3 class="text-lg font-semibold">{{ raid.instanceName }}</h3>
              <span :class="['text-sm font-medium', getDifficultyColor(raid.difficulty)]">
                {{ raid.difficulty }}
              </span>
              <span :class="['text-xs px-2 py-0.5 rounded', getStatusColor(raid.status)]">
                {{ getStatusLabel(raid.status) }}
              </span>
            </div>

            <div class="flex items-center space-x-4 mt-2 text-sm text-gray-400">
              <span>{{ raid.teamName }}</span>
              <span>{{ formatDate(raid.scheduledAt) }}</span>
              <span>{{ formatRaidTime(raid.scheduledAt) }}</span>
            </div>

            <p v-if="raid.description" class="text-sm text-gray-500 mt-2">
              {{ raid.description }}
            </p>
          </div>

          <div class="text-right">
            <div :class="['text-lg font-bold', getSignupRatio(raid).color]">
              {{ getSignupRatio(raid).text }}
            </div>
            <div class="text-xs text-gray-500">signups</div>
          </div>
        </div>

        <!-- Progress bar for signups -->
        <div class="mt-4">
          <div class="w-full bg-gray-700 rounded-full h-1.5">
            <div
              :class="[
                'h-1.5 rounded-full transition-all',
                raid.signupCount >= raid.maxPlayers ? 'bg-green-500' : 'bg-primary-500'
              ]"
              :style="{ width: `${Math.min((raid.signupCount / raid.maxPlayers) * 100, 100)}%` }"
            ></div>
          </div>
        </div>

        <!-- Countdown/Past indicator -->
        <div class="mt-3 text-sm">
          <span v-if="activeTab === 'upcoming'" class="text-blue-400">
            {{ formatRelativeTime(raid.scheduledAt) }}
          </span>
          <span v-else class="text-gray-500">
            Ended {{ formatRelativeTime(raid.endedAt || raid.scheduledAt) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
