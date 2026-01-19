<script setup lang="ts">
/**
 * ApplicationsPage - Officer review interface for guild applications.
 *
 * Features:
 * - Application list with filtering and pagination
 * - Detailed modal view with performance data
 * - Officer notes system
 * - Approve/decline/request info actions
 */
import { ref, computed, watch } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import {
  applicationsApi,
  type Application,
  type UpdateApplicationRequest,
  type ApplicationNote,
  type AddNoteRequest,
} from '@/api/applications'
import { formatDate, formatRelativeTime } from '@/utils/date'
import { useToast } from '@/composables/useToast'
import { ProgressBar, BarChart } from '@/components/charts'
import SkeletonCard from '@/components/SkeletonCard.vue'
import SkeletonTable from '@/components/SkeletonTable.vue'

const queryClient = useQueryClient()
const toast = useToast()

// Filter state
const statusFilter = ref<string>('all')
const currentPage = ref(0)
const pageSize = 20

// Selected application for detail view
const selectedApp = ref<Application | null>(null)
const showDetailModal = ref(false)

// Notes state
const newNoteContent = ref('')
const showDeclineModal = ref(false)
const declineReason = ref('')
const showRequestInfoModal = ref(false)
const infoRequestMessage = ref('')

// Active tab in detail modal
const activeTab = ref<'overview' | 'performance' | 'responses' | 'notes'>('overview')

// Applications query
const { data: applicationsData, isLoading, error, refetch } = useQuery({
  queryKey: ['applications', statusFilter, currentPage],
  queryFn: () => {
    if (statusFilter.value === 'all') {
      return applicationsApi.getApplications(currentPage.value, pageSize)
    }
    return applicationsApi.getApplicationsByStatus(
      statusFilter.value,
      currentPage.value,
      pageSize
    )
  },
})

// Notes query for selected application
const { data: notes, isLoading: notesLoading } = useQuery({
  queryKey: ['applicationNotes', selectedApp],
  queryFn: () =>
    selectedApp.value ? applicationsApi.getNotes(selectedApp.value.applicationId) : [],
  enabled: () => !!selectedApp.value,
})

// Update mutation
const updateMutation = useMutation({
  mutationFn: ({ id, request }: { id: number; request: UpdateApplicationRequest }) =>
    applicationsApi.updateApplication(id, request),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['applications'] })
    showDetailModal.value = false
    selectedApp.value = null
    toast.success('Application updated successfully')
  },
  onError: () => {
    toast.error('Failed to update application')
  },
})

// Approve mutation
const approveMutation = useMutation({
  mutationFn: (id: number) => applicationsApi.approveApplication(id),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['applications'] })
    showDetailModal.value = false
    selectedApp.value = null
    toast.success('Application approved! Trial period started.')
  },
  onError: () => {
    toast.error('Failed to approve application')
  },
})

// Decline mutation
const declineMutation = useMutation({
  mutationFn: ({ id, reason }: { id: number; reason: string }) =>
    applicationsApi.declineApplication(id, reason),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['applications'] })
    showDetailModal.value = false
    showDeclineModal.value = false
    selectedApp.value = null
    declineReason.value = ''
    toast.success('Application declined')
  },
  onError: () => {
    toast.error('Failed to decline application')
  },
})

// Request info mutation
const requestInfoMutation = useMutation({
  mutationFn: ({ id, message }: { id: number; message: string }) =>
    applicationsApi.requestInfo(id, message),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['applications'] })
    showRequestInfoModal.value = false
    infoRequestMessage.value = ''
    toast.success('Information request sent')
  },
  onError: () => {
    toast.error('Failed to send request')
  },
})

// Add note mutation
const addNoteMutation = useMutation({
  mutationFn: ({ id, request }: { id: number; request: AddNoteRequest }) =>
    applicationsApi.addNote(id, request),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['applicationNotes'] })
    newNoteContent.value = ''
    toast.success('Note added')
  },
  onError: () => {
    toast.error('Failed to add note')
  },
})

// Delete mutation
const deleteMutation = useMutation({
  mutationFn: (id: number) => applicationsApi.deleteApplication(id),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['applications'] })
    toast.success('Application deleted')
  },
  onError: () => {
    toast.error('Failed to delete application')
  },
})

// Computed
const applications = computed(() => applicationsData.value?.content ?? [])
const totalPages = computed(() => applicationsData.value?.totalPages ?? 0)
const totalElements = computed(() => applicationsData.value?.totalElements ?? 0)

// Performance data for charts
const parseBreakdown = computed(() => {
  if (!selectedApp.value?.performanceData) return []
  const perf = selectedApp.value.performanceData
  return [
    { label: 'Item Level', value: perf.itemLevel ?? 0, color: '#8b5cf6' },
    { label: 'M+ Score', value: (perf.mythicPlusScore ?? 0) / 40, color: '#3b82f6' },
    { label: 'Avg Parse', value: perf.averageParse ?? 0, color: '#22c55e' },
    { label: 'Best Parse', value: perf.bestParse ?? 0, color: '#f59e0b' },
  ]
})

// Watch for filter changes - reset to page 0
watch(statusFilter, () => {
  currentPage.value = 0
})

// Status helpers
function getStatusBadgeClasses(status: string | null): string {
  switch (status?.toLowerCase()) {
    case 'pending':
      return 'bg-yellow-900/30 text-yellow-400'
    case 'approved':
      return 'bg-green-900/30 text-green-400'
    case 'rejected':
      return 'bg-red-900/30 text-red-400'
    case 'info_requested':
      return 'bg-blue-900/30 text-blue-400'
    case 'withdrawn':
      return 'bg-gray-800/30 text-gray-400'
    default:
      return 'bg-gray-800/30 text-gray-400'
  }
}

function getClassColor(characterClass: string | null): string {
  const classColors: Record<string, string> = {
    warrior: 'text-amber-600',
    paladin: 'text-pink-300',
    hunter: 'text-green-400',
    rogue: 'text-yellow-400',
    priest: 'text-white',
    'death knight': 'text-red-400',
    shaman: 'text-blue-400',
    mage: 'text-cyan-300',
    warlock: 'text-purple-400',
    monk: 'text-emerald-400',
    druid: 'text-orange-400',
    'demon hunter': 'text-purple-600',
    evoker: 'text-emerald-300',
  }
  return classColors[characterClass?.toLowerCase() ?? ''] ?? 'text-gray-400'
}

function getParseColor(parse: number): string {
  if (parse >= 95) return 'text-orange-400' // Legendary
  if (parse >= 75) return 'text-purple-400' // Epic
  if (parse >= 50) return 'text-blue-400' // Rare
  if (parse >= 25) return 'text-green-400' // Uncommon
  return 'text-gray-400'
}

// Actions
function viewApplication(app: Application) {
  selectedApp.value = app
  activeTab.value = 'overview'
  showDetailModal.value = true
}

function approveApplication() {
  if (!selectedApp.value) return
  approveMutation.mutate(selectedApp.value.applicationId)
}

function openDeclineModal() {
  showDeclineModal.value = true
}

function confirmDecline() {
  if (!selectedApp.value || !declineReason.value.trim()) return
  declineMutation.mutate({
    id: selectedApp.value.applicationId,
    reason: declineReason.value,
  })
}

function openRequestInfoModal() {
  showRequestInfoModal.value = true
}

function confirmRequestInfo() {
  if (!selectedApp.value || !infoRequestMessage.value.trim()) return
  requestInfoMutation.mutate({
    id: selectedApp.value.applicationId,
    message: infoRequestMessage.value,
  })
}

function addNote() {
  if (!selectedApp.value || !newNoteContent.value.trim()) return
  addNoteMutation.mutate({
    id: selectedApp.value.applicationId,
    request: { content: newNoteContent.value, isPrivate: true },
  })
}

function deleteApplication(app: Application) {
  if (confirm('Are you sure you want to delete this application?')) {
    deleteMutation.mutate(app.applicationId)
  }
}

function closeModal() {
  showDetailModal.value = false
  selectedApp.value = null
  activeTab.value = 'overview'
}

function getExternalLinks(app: Application) {
  const region = app.mainCharacterRegion?.toLowerCase() || 'eu'
  const name = encodeURIComponent(app.mainCharacterName || '')
  const realm = encodeURIComponent(app.mainCharacterRealm || '')

  return {
    armory: `https://worldofwarcraft.com/en-${region}/character/${region}/${realm}/${name}`,
    warcraftlogs: `https://www.warcraftlogs.com/character/${region}/${realm}/${name}`,
    raiderio: `https://raider.io/characters/${region}/${realm}/${name}`,
    wowprogress: `https://www.wowprogress.com/character/${region}/${realm}/${name}`,
  }
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Guild Applications</h1>
      <div class="flex items-center space-x-4">
        <select
          v-model="statusFilter"
          class="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-sm"
        >
          <option value="all">All Applications</option>
          <option value="pending">Pending</option>
          <option value="info_requested">Info Requested</option>
          <option value="approved">Approved</option>
          <option value="rejected">Rejected</option>
          <option value="withdrawn">Withdrawn</option>
        </select>
        <button
          @click="refetch()"
          class="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg text-sm flex items-center space-x-2"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
          <span>Refresh</span>
        </button>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="space-y-4">
      <SkeletonTable :rows="5" :columns="6" />
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="alert alert-error">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <div>
        <h5 class="alert-title">Error Loading Applications</h5>
        <div class="alert-description">Failed to load applications. Please try again later.</div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="applications.length === 0" class="card text-center py-12">
      <svg class="w-12 h-12 mx-auto text-gray-600 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <p class="text-gray-400">No applications found.</p>
      <p class="text-gray-500 text-sm mt-1">Applications will appear here when players apply.</p>
    </div>

    <!-- Applications table -->
    <div v-else class="card p-0">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-800/50">
            <tr>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Character</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Class / Role</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Performance</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Applied</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Status</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-400">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-700">
            <tr
              v-for="app in applications"
              :key="app.applicationId"
              class="hover:bg-gray-800/30 transition-colors cursor-pointer"
              @click="viewApplication(app)"
            >
              <td class="px-4 py-3">
                <div class="flex items-center space-x-3">
                  <div>
                    <span class="font-medium">{{ app.mainCharacterName || 'Unknown' }}</span>
                    <span class="text-gray-500 text-sm ml-1">- {{ app.mainCharacterRealm || 'Unknown' }}</span>
                  </div>
                </div>
                <div class="text-xs text-gray-500 mt-0.5">
                  Level {{ app.mainCharacterLevel || '?' }} {{ app.mainCharacterRegion || '' }}
                </div>
              </td>
              <td class="px-4 py-3">
                <span :class="getClassColor(app.mainCharacterClass)">
                  {{ app.mainCharacterClass || 'Unknown' }}
                </span>
                <span class="text-gray-500 text-sm ml-1">
                  ({{ app.mainCharacterRole || app.role || 'Unknown' }})
                </span>
              </td>
              <td class="px-4 py-3">
                <div v-if="app.performanceData" class="flex items-center space-x-4">
                  <div class="text-sm">
                    <span class="text-gray-500">iLvl:</span>
                    <span class="ml-1">{{ app.performanceData.itemLevel || '-' }}</span>
                  </div>
                  <div class="text-sm">
                    <span class="text-gray-500">Parse:</span>
                    <span :class="['ml-1', getParseColor(app.performanceData.averageParse || 0)]">
                      {{ app.performanceData.averageParse || '-' }}%
                    </span>
                  </div>
                </div>
                <div v-else class="text-sm text-gray-500">No data</div>
              </td>
              <td class="px-4 py-3">
                <div class="text-sm">{{ app.appliedAt ? formatDate(app.appliedAt) : 'N/A' }}</div>
                <div class="text-xs text-gray-500">
                  {{ app.appliedAt ? formatRelativeTime(app.appliedAt) : '' }}
                </div>
              </td>
              <td class="px-4 py-3">
                <span
                  :class="[
                    'px-2 py-1 rounded-full text-xs font-medium',
                    getStatusBadgeClasses(app.status),
                  ]"
                >
                  {{ app.status || 'Unknown' }}
                </span>
              </td>
              <td class="px-4 py-3 text-right" @click.stop>
                <div class="flex items-center justify-end space-x-2">
                  <button
                    @click="viewApplication(app)"
                    class="text-blue-400 hover:text-blue-300 text-sm px-2 py-1 rounded hover:bg-blue-900/20"
                  >
                    View
                  </button>
                  <button
                    v-if="app.status?.toLowerCase() === 'pending'"
                    @click.stop="() => { selectedApp = app; approveApplication() }"
                    class="text-green-400 hover:text-green-300 text-sm px-2 py-1 rounded hover:bg-green-900/20"
                    :disabled="approveMutation.isPending.value"
                  >
                    Approve
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex items-center justify-between px-4 py-3 border-t border-gray-700">
        <div class="text-sm text-gray-400">
          Showing {{ applications.length }} of {{ totalElements }} applications
        </div>
        <div class="flex items-center space-x-2">
          <button
            @click="currentPage--"
            :disabled="currentPage === 0"
            class="px-3 py-1 rounded bg-gray-700 hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Previous
          </button>
          <span class="text-sm text-gray-400">
            Page {{ currentPage + 1 }} of {{ totalPages }}
          </span>
          <button
            @click="currentPage++"
            :disabled="currentPage >= totalPages - 1"
            class="px-3 py-1 rounded bg-gray-700 hover:bg-gray-600 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </div>
      </div>
    </div>

    <!-- Detail Modal -->
    <Teleport to="body">
      <div
        v-if="showDetailModal && selectedApp"
        class="fixed inset-0 bg-black/70 flex items-center justify-center z-50 p-4"
        @click.self="closeModal"
      >
        <div class="bg-gray-900 rounded-xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
          <!-- Modal Header -->
          <div class="p-6 border-b border-gray-700 flex-shrink-0">
            <div class="flex items-start justify-between">
              <div>
                <div class="flex items-center space-x-3">
                  <h2 class="text-xl font-bold">{{ selectedApp.mainCharacterName }}</h2>
                  <span
                    :class="[
                      'px-2 py-1 rounded-full text-xs font-medium',
                      getStatusBadgeClasses(selectedApp.status),
                    ]"
                  >
                    {{ selectedApp.status }}
                  </span>
                </div>
                <div class="flex items-center space-x-2 mt-1 text-sm text-gray-400">
                  <span :class="getClassColor(selectedApp.mainCharacterClass)">
                    {{ selectedApp.mainCharacterClass }}
                  </span>
                  <span>-</span>
                  <span>{{ selectedApp.mainCharacterRealm }}</span>
                  <span>-</span>
                  <span>{{ selectedApp.mainCharacterRegion }}</span>
                </div>
              </div>
              <button @click="closeModal" class="text-gray-400 hover:text-white p-1">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <!-- Quick Actions -->
            <div v-if="selectedApp.status?.toLowerCase() === 'pending'" class="flex items-center space-x-2 mt-4">
              <button
                @click="approveApplication"
                class="bg-green-600 hover:bg-green-500 text-white px-4 py-2 rounded-lg font-medium text-sm flex items-center space-x-2"
                :disabled="approveMutation.isPending.value"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                <span>Approve</span>
              </button>
              <button
                @click="openDeclineModal"
                class="bg-red-600 hover:bg-red-500 text-white px-4 py-2 rounded-lg font-medium text-sm flex items-center space-x-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                <span>Decline</span>
              </button>
              <button
                @click="openRequestInfoModal"
                class="bg-blue-600 hover:bg-blue-500 text-white px-4 py-2 rounded-lg font-medium text-sm flex items-center space-x-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>Request Info</span>
              </button>
            </div>

            <!-- Tabs -->
            <div class="flex space-x-4 mt-4 border-b border-gray-700 -mb-px">
              <button
                v-for="tab in ['overview', 'performance', 'responses', 'notes']"
                :key="tab"
                @click="activeTab = tab as typeof activeTab"
                :class="[
                  'px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors capitalize',
                  activeTab === tab
                    ? 'border-primary-500 text-primary-400'
                    : 'border-transparent text-gray-400 hover:text-gray-300',
                ]"
              >
                {{ tab }}
              </button>
            </div>
          </div>

          <!-- Modal Content -->
          <div class="p-6 overflow-y-auto flex-1">
            <!-- Overview Tab -->
            <div v-show="activeTab === 'overview'" class="space-y-6">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <!-- Character Info -->
                <div class="bg-gray-800/50 rounded-lg p-4">
                  <h3 class="text-sm font-medium text-gray-400 mb-3">Character Information</h3>
                  <div class="grid grid-cols-2 gap-3 text-sm">
                    <div>
                      <div class="text-xs text-gray-500">Level</div>
                      <div>{{ selectedApp.mainCharacterLevel }}</div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-500">Role</div>
                      <div>{{ selectedApp.mainCharacterRole || selectedApp.role }}</div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-500">Race</div>
                      <div>{{ selectedApp.mainCharacterRace }}</div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-500">Faction</div>
                      <div>{{ selectedApp.mainCharacterFaction }}</div>
                    </div>
                  </div>
                </div>

                <!-- Contact Info -->
                <div class="bg-gray-800/50 rounded-lg p-4">
                  <h3 class="text-sm font-medium text-gray-400 mb-3">Contact Information</h3>
                  <div class="grid grid-cols-2 gap-3 text-sm">
                    <div>
                      <div class="text-xs text-gray-500">Age</div>
                      <div>{{ selectedApp.age || 'Not provided' }}</div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-500">Timezone</div>
                      <div>{{ selectedApp.timezone || 'Not provided' }}</div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-500">BattleTag</div>
                      <div>{{ selectedApp.battletag || 'Not provided' }}</div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-500">Discord</div>
                      <div>{{ selectedApp.discordId || 'Not provided' }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Availability -->
              <div class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-3">Raid Availability</h3>
                <div class="flex flex-wrap gap-2">
                  <span
                    v-for="day in (selectedApp.raidAvailability || '').split(',')"
                    :key="day"
                    class="px-3 py-1 bg-primary-900/30 text-primary-400 rounded-full text-sm"
                  >
                    {{ day }}
                  </span>
                  <span v-if="!selectedApp.raidAvailability" class="text-gray-500 text-sm">
                    Not specified
                  </span>
                </div>
              </div>

              <!-- External Links -->
              <div class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-3">External Links</h3>
                <div class="flex flex-wrap gap-2">
                  <a
                    :href="getExternalLinks(selectedApp).armory"
                    target="_blank"
                    class="px-3 py-1.5 bg-gray-700 hover:bg-gray-600 rounded-lg text-sm flex items-center space-x-2 transition-colors"
                  >
                    <span>Armory</span>
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </a>
                  <a
                    :href="getExternalLinks(selectedApp).warcraftlogs"
                    target="_blank"
                    class="px-3 py-1.5 bg-orange-900/30 hover:bg-orange-900/50 text-orange-400 rounded-lg text-sm flex items-center space-x-2 transition-colors"
                  >
                    <span>Warcraft Logs</span>
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </a>
                  <a
                    :href="getExternalLinks(selectedApp).raiderio"
                    target="_blank"
                    class="px-3 py-1.5 bg-blue-900/30 hover:bg-blue-900/50 text-blue-400 rounded-lg text-sm flex items-center space-x-2 transition-colors"
                  >
                    <span>Raider.IO</span>
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </a>
                  <a
                    :href="getExternalLinks(selectedApp).wowprogress"
                    target="_blank"
                    class="px-3 py-1.5 bg-purple-900/30 hover:bg-purple-900/50 text-purple-400 rounded-lg text-sm flex items-center space-x-2 transition-colors"
                  >
                    <span>WoWProgress</span>
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                    </svg>
                  </a>
                </div>
              </div>
            </div>

            <!-- Performance Tab -->
            <div v-show="activeTab === 'performance'" class="space-y-6">
              <div v-if="selectedApp.performanceData" class="space-y-6">
                <!-- Performance Stats -->
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div class="bg-gray-800/50 rounded-lg p-4 text-center">
                    <div class="text-3xl font-bold text-primary-400">
                      {{ selectedApp.performanceData.itemLevel || '-' }}
                    </div>
                    <div class="text-sm text-gray-400 mt-1">Item Level</div>
                  </div>
                  <div class="bg-gray-800/50 rounded-lg p-4 text-center">
                    <div class="text-3xl font-bold text-blue-400">
                      {{ selectedApp.performanceData.mythicPlusScore || '-' }}
                    </div>
                    <div class="text-sm text-gray-400 mt-1">M+ Score</div>
                  </div>
                  <div class="bg-gray-800/50 rounded-lg p-4 text-center">
                    <div
                      :class="[
                        'text-3xl font-bold',
                        getParseColor(selectedApp.performanceData.averageParse || 0),
                      ]"
                    >
                      {{ selectedApp.performanceData.averageParse || '-' }}%
                    </div>
                    <div class="text-sm text-gray-400 mt-1">Avg Parse</div>
                  </div>
                  <div class="bg-gray-800/50 rounded-lg p-4 text-center">
                    <div
                      :class="[
                        'text-3xl font-bold',
                        getParseColor(selectedApp.performanceData.bestParse || 0),
                      ]"
                    >
                      {{ selectedApp.performanceData.bestParse || '-' }}%
                    </div>
                    <div class="text-sm text-gray-400 mt-1">Best Parse</div>
                  </div>
                </div>

                <!-- Deaths per pull -->
                <div v-if="selectedApp.performanceData.deathsPerPull !== undefined" class="bg-gray-800/50 rounded-lg p-4">
                  <div class="flex items-center justify-between mb-2">
                    <h3 class="text-sm font-medium text-gray-400">Deaths Per Pull</h3>
                    <span
                      :class="[
                        'text-lg font-bold',
                        selectedApp.performanceData.deathsPerPull <= 0.8 ? 'text-green-400' :
                        selectedApp.performanceData.deathsPerPull <= 1.2 ? 'text-yellow-400' : 'text-red-400',
                      ]"
                    >
                      {{ selectedApp.performanceData.deathsPerPull.toFixed(2) }}
                    </span>
                  </div>
                  <ProgressBar
                    :value="Math.min(selectedApp.performanceData.deathsPerPull * 50, 100)"
                    :max="100"
                    :color="selectedApp.performanceData.deathsPerPull <= 0.8 ? '#22c55e' :
                            selectedApp.performanceData.deathsPerPull <= 1.2 ? '#eab308' : '#ef4444'"
                    height="0.375rem"
                    :show-label="false"
                  />
                  <p class="text-xs text-gray-500 mt-2">
                    Guild average: 0.8 deaths/pull
                  </p>
                </div>

                <!-- Progression History -->
                <div v-if="selectedApp.performanceData.progressionHistory?.length" class="bg-gray-800/50 rounded-lg p-4">
                  <h3 class="text-sm font-medium text-gray-400 mb-3">Raid Progression</h3>
                  <ul class="space-y-2">
                    <li
                      v-for="prog in selectedApp.performanceData.progressionHistory"
                      :key="prog"
                      class="text-sm flex items-center space-x-2"
                    >
                      <span class="w-2 h-2 rounded-full bg-primary-500"></span>
                      <span>{{ prog }}</span>
                    </li>
                  </ul>
                </div>
              </div>
              <div v-else class="text-center py-12 text-gray-500">
                <p>No performance data available.</p>
                <p class="text-sm mt-1">Data is fetched from Warcraft Logs and Raider.IO.</p>
              </div>
            </div>

            <!-- Responses Tab -->
            <div v-show="activeTab === 'responses'" class="space-y-6">
              <div v-if="selectedApp.previousGuild" class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">Previous Guild</h3>
                <p class="text-gray-200">{{ selectedApp.previousGuild }}</p>
              </div>

              <div v-if="selectedApp.reasonForLeaving" class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">Reason for Leaving</h3>
                <p class="text-gray-300 whitespace-pre-wrap">{{ selectedApp.reasonForLeaving }}</p>
              </div>

              <div v-if="selectedApp.whyThisGuild" class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">Why EdgeRush?</h3>
                <p class="text-gray-300 whitespace-pre-wrap">{{ selectedApp.whyThisGuild }}</p>
              </div>

              <div v-if="selectedApp.whatYouBring" class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">What They Bring</h3>
                <p class="text-gray-300 whitespace-pre-wrap">{{ selectedApp.whatYouBring }}</p>
              </div>

              <div v-if="selectedApp.goals" class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-2">Goals</h3>
                <p class="text-gray-300 whitespace-pre-wrap">{{ selectedApp.goals }}</p>
              </div>

              <div v-if="!selectedApp.previousGuild && !selectedApp.whyThisGuild" class="text-center py-12 text-gray-500">
                <p>No detailed responses available.</p>
              </div>
            </div>

            <!-- Notes Tab -->
            <div v-show="activeTab === 'notes'" class="space-y-6">
              <!-- Add Note -->
              <div class="bg-gray-800/50 rounded-lg p-4">
                <h3 class="text-sm font-medium text-gray-400 mb-3">Add Officer Note</h3>
                <div class="flex space-x-2">
                  <textarea
                    v-model="newNoteContent"
                    placeholder="Add a private note about this applicant..."
                    rows="2"
                    class="flex-1 bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                  ></textarea>
                  <button
                    @click="addNote"
                    :disabled="!newNoteContent.trim() || addNoteMutation.isPending.value"
                    class="px-4 py-2 bg-primary-600 hover:bg-primary-500 rounded-lg text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Add
                  </button>
                </div>
              </div>

              <!-- Notes List -->
              <div v-if="notesLoading" class="py-4">
                <SkeletonCard :lines="2" :show-header="false" />
              </div>
              <div v-else-if="notes && notes.length > 0" class="space-y-3">
                <div
                  v-for="note in notes"
                  :key="note.id"
                  class="bg-gray-800/50 rounded-lg p-4"
                >
                  <div class="flex items-center justify-between mb-2">
                    <span class="font-medium text-sm">{{ note.authorName }}</span>
                    <span class="text-xs text-gray-500">{{ formatRelativeTime(note.createdAt) }}</span>
                  </div>
                  <p class="text-sm text-gray-300">{{ note.content }}</p>
                </div>
              </div>
              <div v-else class="text-center py-8 text-gray-500">
                <p>No notes yet.</p>
                <p class="text-sm mt-1">Add a note to share with other officers.</p>
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="p-4 border-t border-gray-700 bg-gray-800/50 flex-shrink-0">
            <div class="flex items-center justify-between text-xs text-gray-500">
              <span>Applied: {{ selectedApp.appliedAt ? formatDate(selectedApp.appliedAt) : 'N/A' }}</span>
              <span>Last synced: {{ formatRelativeTime(selectedApp.syncedAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Decline Modal -->
    <Teleport to="body">
      <div
        v-if="showDeclineModal"
        class="fixed inset-0 bg-black/70 flex items-center justify-center z-[60]"
        @click.self="showDeclineModal = false"
      >
        <div class="bg-gray-900 rounded-xl max-w-md w-full mx-4 p-6">
          <h3 class="text-lg font-bold mb-4">Decline Application</h3>
          <p class="text-gray-400 text-sm mb-4">
            Please provide a reason for declining this application. This will be sent to the applicant.
          </p>
          <textarea
            v-model="declineReason"
            placeholder="Reason for declining..."
            rows="4"
            class="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent mb-4"
          ></textarea>
          <div class="flex space-x-3">
            <button
              @click="showDeclineModal = false"
              class="flex-1 bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg font-medium"
            >
              Cancel
            </button>
            <button
              @click="confirmDecline"
              :disabled="!declineReason.trim() || declineMutation.isPending.value"
              class="flex-1 bg-red-600 hover:bg-red-500 px-4 py-2 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Decline
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Request Info Modal -->
    <Teleport to="body">
      <div
        v-if="showRequestInfoModal"
        class="fixed inset-0 bg-black/70 flex items-center justify-center z-[60]"
        @click.self="showRequestInfoModal = false"
      >
        <div class="bg-gray-900 rounded-xl max-w-md w-full mx-4 p-6">
          <h3 class="text-lg font-bold mb-4">Request Information</h3>
          <p class="text-gray-400 text-sm mb-4">
            Ask the applicant for additional information. They will be notified via Discord.
          </p>
          <textarea
            v-model="infoRequestMessage"
            placeholder="What information do you need?"
            rows="4"
            class="w-full bg-gray-800 border border-gray-700 rounded-lg px-3 py-2 text-sm resize-none focus:ring-2 focus:ring-primary-500 focus:border-transparent mb-4"
          ></textarea>
          <div class="flex space-x-3">
            <button
              @click="showRequestInfoModal = false"
              class="flex-1 bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg font-medium"
            >
              Cancel
            </button>
            <button
              @click="confirmRequestInfo"
              :disabled="!infoRequestMessage.trim() || requestInfoMutation.isPending.value"
              class="flex-1 bg-blue-600 hover:bg-blue-500 px-4 py-2 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Send Request
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
