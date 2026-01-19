<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { raidsApi, type RaidSignup, type RaidEncounter, type SignupStatus } from '@/api/raids'
import { flpsApi } from '@/api/flps'
import { formatDate, formatRelativeTime } from '@/utils/date'
import { useAuthStore } from '@/stores/auth'
import { useGuildContextStore } from '@/stores/guildContext'
import type { Role } from '@/types'

const route = useRoute()
const router = useRouter()
const queryClient = useQueryClient()
const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()
const raidId = computed(() => Number(route.params.id))
const guildId = computed(() => guildContextStore.currentGuildId || authStore.user?.guildId)

// Queries
const { data: raid, isLoading, error } = useQuery({
  queryKey: ['raid', raidId],
  queryFn: () => raidsApi.getRaidById(raidId.value),
  enabled: computed(() => !!raidId.value),
})

const { data: flpsData } = useQuery({
  queryKey: ['myFlps', guildId],
  queryFn: () => flpsApi.getMyFlps(guildId.value!),
  enabled: computed(() => !!guildId.value),
})

// Signup modal state
const showSignupModal = ref(false)
const signupForm = ref({
  role: 'DPS' as Role,
  status: 'CONFIRMED' as SignupStatus,
  notes: '',
})

// My current signup
const mySignup = computed(() => {
  if (!raid.value?.signups || !flpsData.value?.raiderId) return null
  return raid.value.signups.find(s => s.raiderId === flpsData.value!.raiderId)
})

// Grouped signups by status
const groupedSignups = computed(() => {
  if (!raid.value?.signups) return { confirmed: [], tentative: [], standby: [], declined: [] }

  return {
    confirmed: raid.value.signups.filter(s => s.status === 'CONFIRMED'),
    tentative: raid.value.signups.filter(s => s.status === 'TENTATIVE'),
    standby: raid.value.signups.filter(s => s.status === 'STANDBY'),
    declined: raid.value.signups.filter(s => s.status === 'DECLINED'),
  }
})

// Role counts for confirmed signups
const roleCounts = computed(() => {
  const confirmed = groupedSignups.value.confirmed
  return {
    TANK: confirmed.filter(s => s.role === 'TANK').length,
    HEALER: confirmed.filter(s => s.role === 'HEALER').length,
    DPS: confirmed.filter(s => s.role === 'DPS').length,
  }
})

// Mutations
const createSignupMutation = useMutation({
  mutationFn: () => raidsApi.createSignup(raidId.value, {
    raiderId: flpsData.value!.raiderId,
    ...signupForm.value,
  }),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['raid', raidId.value] })
    showSignupModal.value = false
  },
})

const updateSignupMutation = useMutation({
  mutationFn: (data: { signupId: number; update: Partial<typeof signupForm.value> }) =>
    raidsApi.updateSignup(data.signupId, data.update),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['raid', raidId.value] })
    showSignupModal.value = false
  },
})

const cancelSignupMutation = useMutation({
  mutationFn: (signupId: number) => raidsApi.deleteSignup(signupId),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['raid', raidId.value] })
  },
})

// Helper functions
function getDifficultyColor(difficulty: string): string {
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

function getEncounterStatusColor(status: RaidEncounter['status']): string {
  switch (status) {
    case 'KILLED':
      return 'text-green-400'
    case 'IN_PROGRESS':
      return 'text-yellow-400'
    case 'WIPED':
      return 'text-red-400'
    default:
      return 'text-gray-400'
  }
}

function getEncounterStatusIcon(status: RaidEncounter['status']): string {
  switch (status) {
    case 'KILLED':
      return '✓'
    case 'IN_PROGRESS':
      return '⟳'
    case 'WIPED':
      return '✗'
    default:
      return '○'
  }
}

function getRoleColor(role: Role): string {
  switch (role) {
    case 'TANK':
      return 'text-blue-400'
    case 'HEALER':
      return 'text-green-400'
    case 'DPS':
      return 'text-red-400'
    default:
      return 'text-gray-400'
  }
}

function getRoleIcon(role: Role): string {
  switch (role) {
    case 'TANK':
      return '🛡️'
    case 'HEALER':
      return '💚'
    case 'DPS':
      return '⚔️'
    default:
      return ''
  }
}

function getSignupStatusColor(status: SignupStatus): string {
  switch (status) {
    case 'CONFIRMED':
      return 'text-green-400'
    case 'TENTATIVE':
      return 'text-yellow-400'
    case 'STANDBY':
      return 'text-blue-400'
    case 'DECLINED':
      return 'text-red-400'
    default:
      return 'text-gray-400'
  }
}

function formatDuration(seconds: number): string {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

function openSignupModal() {
  if (mySignup.value) {
    signupForm.value = {
      role: mySignup.value.role,
      status: mySignup.value.status,
      notes: mySignup.value.notes || '',
    }
  } else {
    signupForm.value = {
      role: flpsData.value?.role || 'DPS',
      status: 'CONFIRMED',
      notes: '',
    }
  }
  showSignupModal.value = true
}

function submitSignup() {
  if (mySignup.value) {
    updateSignupMutation.mutate({
      signupId: mySignup.value.id,
      update: signupForm.value,
    })
  } else {
    createSignupMutation.mutate()
  }
}

function cancelMySignup() {
  if (mySignup.value && confirm('Are you sure you want to cancel your signup?')) {
    cancelSignupMutation.mutate(mySignup.value.id)
  }
}
</script>

<template>
  <div>
    <!-- Back button -->
    <button @click="router.push('/raids')" class="flex items-center space-x-2 text-gray-400 hover:text-white mb-6 transition-colors">
      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
      <span>Back to Raids</span>
    </button>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="alert alert-error">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <div>
        <h5 class="alert-title">Error Loading Raid</h5>
        <div class="alert-description">Failed to load raid details. Please try again later.</div>
      </div>
    </div>

    <!-- Content -->
    <div v-else-if="raid" class="space-y-6">
      <!-- Raid Header -->
      <div class="card">
        <div class="flex items-start justify-between">
          <div>
            <div class="flex items-center space-x-3">
              <h1 class="text-2xl font-bold">{{ raid.instanceName }}</h1>
              <span :class="['text-lg font-medium', getDifficultyColor(raid.difficulty)]">
                {{ raid.difficulty }}
              </span>
            </div>
            <div class="flex items-center space-x-4 mt-2 text-gray-400">
              <span>{{ raid.teamName }}</span>
              <span>{{ formatDate(raid.scheduledAt) }}</span>
              <span>{{ new Date(raid.scheduledAt).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' }) }}</span>
            </div>
            <p v-if="raid.description" class="text-gray-500 mt-3">{{ raid.description }}</p>
          </div>

          <div v-if="raid.status === 'SCHEDULED'" class="flex flex-col items-end space-y-2">
            <button
              v-if="mySignup"
              @click="openSignupModal"
              class="btn-secondary"
            >
              Edit Signup
            </button>
            <button
              v-else
              @click="openSignupModal"
              class="btn-primary"
            >
              Sign Up
            </button>
            <button
              v-if="mySignup"
              @click="cancelMySignup"
              class="text-sm text-red-400 hover:text-red-300"
              :disabled="cancelSignupMutation.isPending.value"
            >
              Cancel Signup
            </button>
          </div>
        </div>

        <!-- Role composition -->
        <div class="mt-6 pt-4 border-t border-gray-700">
          <h3 class="text-sm font-medium text-gray-400 mb-3">Confirmed Roster</h3>
          <div class="flex items-center space-x-6">
            <div class="flex items-center space-x-2">
              <span class="text-blue-400">🛡️</span>
              <span class="font-medium">{{ roleCounts.TANK }}</span>
              <span class="text-gray-500">Tanks</span>
            </div>
            <div class="flex items-center space-x-2">
              <span class="text-green-400">💚</span>
              <span class="font-medium">{{ roleCounts.HEALER }}</span>
              <span class="text-gray-500">Healers</span>
            </div>
            <div class="flex items-center space-x-2">
              <span class="text-red-400">⚔️</span>
              <span class="font-medium">{{ roleCounts.DPS }}</span>
              <span class="text-gray-500">DPS</span>
            </div>
            <div class="flex-1"></div>
            <div class="text-lg">
              <span class="font-bold text-primary-400">{{ groupedSignups.confirmed.length }}</span>
              <span class="text-gray-500">/{{ raid.maxPlayers }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Encounters -->
      <div v-if="raid.encounters?.length" class="card">
        <h2 class="text-lg font-semibold mb-4">Encounters</h2>
        <div class="space-y-2">
          <div
            v-for="encounter in raid.encounters"
            :key="encounter.id"
            class="flex items-center justify-between p-3 bg-gray-800/30 rounded-lg"
          >
            <div class="flex items-center space-x-3">
              <span :class="['text-lg', getEncounterStatusColor(encounter.status)]">
                {{ getEncounterStatusIcon(encounter.status) }}
              </span>
              <span class="font-medium">{{ encounter.encounterName }}</span>
            </div>
            <div class="flex items-center space-x-4 text-sm text-gray-400">
              <span v-if="encounter.pullCount > 0">{{ encounter.pullCount }} pulls</span>
              <span v-if="encounter.duration">{{ formatDuration(encounter.duration) }}</span>
              <span v-if="encounter.killedAt" class="text-green-400">
                Killed {{ formatRelativeTime(encounter.killedAt) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Signups -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Signups</h2>

        <!-- Confirmed -->
        <div v-if="groupedSignups.confirmed.length" class="mb-6">
          <h3 class="text-sm font-medium text-green-400 mb-3">Confirmed ({{ groupedSignups.confirmed.length }})</h3>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2">
            <div
              v-for="signup in groupedSignups.confirmed"
              :key="signup.id"
              class="flex items-center space-x-2 p-2 bg-gray-800/30 rounded"
            >
              <span>{{ getRoleIcon(signup.role) }}</span>
              <span :class="['font-medium', getRoleColor(signup.role)]">{{ signup.characterName }}</span>
            </div>
          </div>
        </div>

        <!-- Tentative -->
        <div v-if="groupedSignups.tentative.length" class="mb-6">
          <h3 class="text-sm font-medium text-yellow-400 mb-3">Tentative ({{ groupedSignups.tentative.length }})</h3>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2">
            <div
              v-for="signup in groupedSignups.tentative"
              :key="signup.id"
              class="flex items-center space-x-2 p-2 bg-gray-800/30 rounded"
            >
              <span>{{ getRoleIcon(signup.role) }}</span>
              <span :class="['font-medium', getRoleColor(signup.role)]">{{ signup.characterName }}</span>
            </div>
          </div>
        </div>

        <!-- Standby -->
        <div v-if="groupedSignups.standby.length" class="mb-6">
          <h3 class="text-sm font-medium text-blue-400 mb-3">Standby ({{ groupedSignups.standby.length }})</h3>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2">
            <div
              v-for="signup in groupedSignups.standby"
              :key="signup.id"
              class="flex items-center space-x-2 p-2 bg-gray-800/30 rounded"
            >
              <span>{{ getRoleIcon(signup.role) }}</span>
              <span :class="['font-medium', getRoleColor(signup.role)]">{{ signup.characterName }}</span>
            </div>
          </div>
        </div>

        <!-- Declined -->
        <div v-if="groupedSignups.declined.length">
          <h3 class="text-sm font-medium text-red-400 mb-3">Declined ({{ groupedSignups.declined.length }})</h3>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-2">
            <div
              v-for="signup in groupedSignups.declined"
              :key="signup.id"
              class="flex items-center space-x-2 p-2 bg-gray-800/30 rounded opacity-50"
            >
              <span>{{ getRoleIcon(signup.role) }}</span>
              <span class="text-gray-500">{{ signup.characterName }}</span>
            </div>
          </div>
        </div>

        <div v-if="!raid.signups?.length" class="text-center py-8 text-gray-400">
          No signups yet.
        </div>
      </div>
    </div>

    <!-- Signup Modal -->
    <div
      v-if="showSignupModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="showSignupModal = false"
    >
      <div class="card max-w-md w-full">
        <h3 class="text-lg font-semibold mb-4">
          {{ mySignup ? 'Edit Signup' : 'Sign Up for Raid' }}
        </h3>

        <div class="space-y-4">
          <div>
            <label class="label">Role</label>
            <select v-model="signupForm.role" class="input">
              <option value="TANK">Tank</option>
              <option value="HEALER">Healer</option>
              <option value="DPS">DPS</option>
            </select>
          </div>

          <div>
            <label class="label">Status</label>
            <select v-model="signupForm.status" class="input">
              <option value="CONFIRMED">Confirmed</option>
              <option value="TENTATIVE">Tentative</option>
              <option value="STANDBY">Standby</option>
              <option value="DECLINED">Declined</option>
            </select>
          </div>

          <div>
            <label class="label">Notes (optional)</label>
            <textarea v-model="signupForm.notes" class="input" rows="2" placeholder="Any notes for the raid leader..."></textarea>
          </div>
        </div>

        <div class="flex justify-end space-x-3 mt-6">
          <button @click="showSignupModal = false" class="btn-secondary">
            Cancel
          </button>
          <button
            @click="submitSignup"
            class="btn-primary"
            :disabled="createSignupMutation.isPending.value || updateSignupMutation.isPending.value"
          >
            {{ (createSignupMutation.isPending.value || updateSignupMutation.isPending.value) ? 'Saving...' : 'Save' }}
          </button>
        </div>

        <div v-if="createSignupMutation.isError.value || updateSignupMutation.isError.value" class="mt-4 alert alert-error">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          <div class="alert-description">
            Failed to save signup. Please try again.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
