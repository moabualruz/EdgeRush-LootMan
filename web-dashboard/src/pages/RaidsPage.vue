<script setup lang="ts">
/**
 * RaidsPage - Raid planning and viewing.
 *
 * Features:
 * - List view with upcoming/past toggle
 * - Calendar view for visual planning
 * - Raid detail navigation
 */
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { raidsApi, type Raid, type RaidStatus } from '@/api/raids'
import { formatDate, formatRelativeTime } from '@/utils/date'
import { useRouter } from 'vue-router'
import RaidCalendar from '@/components/RaidCalendar.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'
const router = useRouter()

// View mode
const viewMode = ref<'list' | 'calendar'>('list')

// Tab state for list view
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

const isLoading = computed(() =>
  activeTab.value === 'upcoming' ? upcomingLoading.value : pastLoading.value
)
const displayedRaids = computed(() =>
  activeTab.value === 'upcoming' ? upcomingRaids.value : pastRaids.value
)

// All raids for calendar
const allRaids = computed(() => {
  const upcoming = upcomingRaids.value || []
  const past = pastRaids.value || []
  return [...upcoming, ...past]
})

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

function viewRaid(raid: Raid | number) {
  const raidId = typeof raid === 'number' ? raid : raid.id
  router.push(`/raids/${raidId}`)
}

function formatRaidTime(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })
}

function handleRaidClick(raid: Raid) {
  viewRaid(raid)
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Raids</h1>

      <!-- View mode toggle -->
      <div class="flex items-center space-x-2">
        <button
          @click="viewMode = 'list'"
          :class="[
            'p-2 rounded-lg transition-colors',
            viewMode === 'list' ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-400 hover:bg-gray-600',
          ]"
          title="List View"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
          </svg>
        </button>
        <button
          @click="viewMode = 'calendar'"
          :class="[
            'p-2 rounded-lg transition-colors',
            viewMode === 'calendar' ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-400 hover:bg-gray-600',
          ]"
          title="Calendar View"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Calendar View -->
    <div v-if="viewMode === 'calendar'" class="card">
      <RaidCalendar
        :raids="allRaids"
        @raid-click="handleRaidClick"
      />
    </div>

    <!-- List View -->
    <div v-else>
      <!-- Tab navigation -->
      <div class="flex items-center space-x-2 mb-6">
        <button
          @click="activeTab = 'upcoming'"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            activeTab === 'upcoming'
              ? 'bg-primary-600 text-white'
              : 'bg-gray-700 text-gray-400 hover:bg-gray-600',
          ]"
        >
          Upcoming
        </button>
        <button
          @click="activeTab = 'past'"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            activeTab === 'past'
              ? 'bg-primary-600 text-white'
              : 'bg-gray-700 text-gray-400 hover:bg-gray-600',
          ]"
        >
          Past Raids
        </button>
      </div>

      <!-- Loading state -->
      <div v-if="isLoading" class="space-y-4">
        <SkeletonCard :lines="3" v-for="i in 3" :key="i" />
      </div>

      <!-- Content -->
      <div v-else-if="displayedRaids" class="space-y-4">
        <div v-if="displayedRaids.length === 0" class="card text-center py-12">
          <svg class="w-12 h-12 mx-auto text-gray-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
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
            getDifficultyBg(raid.difficulty),
          ]"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center space-x-3">
                <h3 class="text-lg font-semibold">{{ raid.instanceName }}</h3>
                <span :class="['text-sm font-medium', getDifficultyColor(raid.difficulty)]">
                  {{ raid.difficulty }}
                </span>
                <span
                  :class="[
                    'text-xs px-2 py-0.5 rounded-full font-medium',
                    getStatusColor(raid.status),
                  ]"
                >
                  {{ getStatusLabel(raid.status) }}
                </span>
              </div>

              <div class="flex items-center space-x-4 mt-2 text-sm text-gray-400">
                <span class="flex items-center space-x-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                  <span>{{ raid.teamName }}</span>
                </span>
                <span class="flex items-center space-x-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <span>{{ formatDate(raid.scheduledAt) }}</span>
                </span>
                <span class="flex items-center space-x-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>{{ formatRaidTime(raid.scheduledAt) }}</span>
                </span>
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
                  raid.signupCount >= raid.maxPlayers ? 'bg-green-500' : 'bg-primary-500',
                ]"
                :style="{ width: `${Math.min((raid.signupCount / raid.maxPlayers) * 100, 100)}%` }"
              ></div>
            </div>
          </div>

          <!-- Countdown/Past indicator -->
          <div class="mt-3 text-sm">
            <span v-if="activeTab === 'upcoming'" class="text-blue-400 flex items-center space-x-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{{ formatRelativeTime(raid.scheduledAt) }}</span>
            </span>
            <span v-else class="text-gray-500 flex items-center space-x-1">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <span>Ended {{ formatRelativeTime(raid.endedAt || raid.scheduledAt) }}</span>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
