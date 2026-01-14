<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { applicationsApi, type Application, type UpdateApplicationRequest } from '@/api/applications'
import { formatDate, formatRelativeTime } from '@/utils/date'

const queryClient = useQueryClient()

// Filter state
const statusFilter = ref<string>('all')
const currentPage = ref(0)
const pageSize = 20

// Selected application for detail view
const selectedApp = ref<Application | null>(null)
const showDetailModal = ref(false)

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

// Update mutation
const updateMutation = useMutation({
  mutationFn: ({ id, request }: { id: number; request: UpdateApplicationRequest }) =>
    applicationsApi.updateApplication(id, request),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['applications'] })
    showDetailModal.value = false
    selectedApp.value = null
  },
})

// Delete mutation
const deleteMutation = useMutation({
  mutationFn: (id: number) => applicationsApi.deleteApplication(id),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['applications'] })
  },
})

// Computed
const applications = computed(() => applicationsData.value?.content ?? [])
const totalPages = computed(() => applicationsData.value?.totalPages ?? 0)
const totalElements = computed(() => applicationsData.value?.totalElements ?? 0)

// Watch for filter changes - reset to page 0
watch(statusFilter, () => {
  currentPage.value = 0
})

// Status helpers
function getStatusColor(status: string | null): string {
  switch (status?.toLowerCase()) {
    case 'pending':
      return 'text-yellow-400'
    case 'approved':
      return 'text-green-400'
    case 'rejected':
      return 'text-red-400'
    case 'withdrawn':
      return 'text-gray-400'
    default:
      return 'text-gray-400'
  }
}

function getStatusBadgeClasses(status: string | null): string {
  switch (status?.toLowerCase()) {
    case 'pending':
      return 'bg-yellow-900/30 text-yellow-400'
    case 'approved':
      return 'bg-green-900/30 text-green-400'
    case 'rejected':
      return 'bg-red-900/30 text-red-400'
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

// Actions
function viewApplication(app: Application) {
  selectedApp.value = app
  showDetailModal.value = true
}

function approveApplication(app: Application) {
  updateMutation.mutate({
    id: app.applicationId,
    request: { status: 'approved' },
  })
}

function rejectApplication(app: Application) {
  updateMutation.mutate({
    id: app.applicationId,
    request: { status: 'rejected' },
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
          <option value="approved">Approved</option>
          <option value="rejected">Rejected</option>
          <option value="withdrawn">Withdrawn</option>
        </select>
        <button
          @click="refetch()"
          class="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg text-sm"
        >
          Refresh
        </button>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="card bg-red-900/20 border-red-700">
      <p class="text-red-400">Failed to load applications. Please try again.</p>
    </div>

    <!-- Empty state -->
    <div v-else-if="applications.length === 0" class="card text-center py-12">
      <p class="text-gray-400">No applications found.</p>
    </div>

    <!-- Applications table -->
    <div v-else class="card">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead class="bg-gray-800/50">
            <tr>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Character</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Class / Role</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Applied</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Status</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Contact</th>
              <th class="text-right px-4 py-3 text-sm font-medium text-gray-400">Actions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-700">
            <tr
              v-for="app in applications"
              :key="app.applicationId"
              class="hover:bg-gray-800/30 transition-colors"
            >
              <td class="px-4 py-3">
                <div>
                  <span class="font-medium">{{ app.mainCharacterName || 'Unknown' }}</span>
                  <span class="text-gray-500 text-sm ml-1">- {{ app.mainCharacterRealm || 'Unknown' }}</span>
                </div>
                <div class="text-xs text-gray-500">
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
              <td class="px-4 py-3">
                <div v-if="app.battletag" class="text-sm">{{ app.battletag }}</div>
                <div v-if="app.discordId" class="text-xs text-gray-500">
                  Discord: {{ app.discordId }}
                </div>
              </td>
              <td class="px-4 py-3 text-right">
                <div class="flex items-center justify-end space-x-2">
                  <button
                    @click="viewApplication(app)"
                    class="text-blue-400 hover:text-blue-300 text-sm"
                  >
                    View
                  </button>
                  <button
                    v-if="app.status?.toLowerCase() === 'pending'"
                    @click="approveApplication(app)"
                    class="text-green-400 hover:text-green-300 text-sm"
                    :disabled="updateMutation.isPending.value"
                  >
                    Approve
                  </button>
                  <button
                    v-if="app.status?.toLowerCase() === 'pending'"
                    @click="rejectApplication(app)"
                    class="text-red-400 hover:text-red-300 text-sm"
                    :disabled="updateMutation.isPending.value"
                  >
                    Reject
                  </button>
                  <button
                    @click="deleteApplication(app)"
                    class="text-gray-400 hover:text-gray-300 text-sm"
                    :disabled="deleteMutation.isPending.value"
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex items-center justify-between mt-4 pt-4 border-t border-gray-700">
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
        class="fixed inset-0 bg-black/70 flex items-center justify-center z-50"
        @click.self="closeModal"
      >
        <div class="bg-gray-900 rounded-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
          <div class="p-6 border-b border-gray-700">
            <div class="flex items-center justify-between">
              <h2 class="text-xl font-bold">Application Details</h2>
              <button @click="closeModal" class="text-gray-400 hover:text-white">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          <div class="p-6 space-y-6">
            <!-- Character Info -->
            <div>
              <h3 class="text-sm font-medium text-gray-400 mb-3">Character Information</h3>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <div class="text-xs text-gray-500">Name</div>
                  <div class="font-medium">{{ selectedApp.mainCharacterName }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Realm</div>
                  <div>{{ selectedApp.mainCharacterRealm }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Class</div>
                  <div :class="getClassColor(selectedApp.mainCharacterClass)">
                    {{ selectedApp.mainCharacterClass }}
                  </div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Role</div>
                  <div>{{ selectedApp.mainCharacterRole || selectedApp.role }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Level</div>
                  <div>{{ selectedApp.mainCharacterLevel }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Region</div>
                  <div>{{ selectedApp.mainCharacterRegion }}</div>
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
            <div>
              <h3 class="text-sm font-medium text-gray-400 mb-3">Contact Information</h3>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <div class="text-xs text-gray-500">BattleTag</div>
                  <div>{{ selectedApp.battletag || 'Not provided' }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Discord</div>
                  <div>{{ selectedApp.discordId || 'Not provided' }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Age</div>
                  <div>{{ selectedApp.age || 'Not provided' }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Country</div>
                  <div>{{ selectedApp.country || 'Not provided' }}</div>
                </div>
              </div>
            </div>

            <!-- Status Info -->
            <div>
              <h3 class="text-sm font-medium text-gray-400 mb-3">Application Status</h3>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <div class="text-xs text-gray-500">Status</div>
                  <span
                    :class="[
                      'px-2 py-1 rounded-full text-xs font-medium',
                      getStatusBadgeClasses(selectedApp.status),
                    ]"
                  >
                    {{ selectedApp.status }}
                  </span>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Applied At</div>
                  <div>{{ selectedApp.appliedAt ? formatDate(selectedApp.appliedAt) : 'N/A' }}</div>
                </div>
                <div>
                  <div class="text-xs text-gray-500">Last Synced</div>
                  <div>{{ formatDate(selectedApp.syncedAt) }}</div>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div
              v-if="selectedApp.status?.toLowerCase() === 'pending'"
              class="flex items-center space-x-4 pt-4 border-t border-gray-700"
            >
              <button
                @click="approveApplication(selectedApp)"
                class="flex-1 bg-green-600 hover:bg-green-500 text-white px-4 py-2 rounded-lg font-medium"
                :disabled="updateMutation.isPending.value"
              >
                Approve Application
              </button>
              <button
                @click="rejectApplication(selectedApp)"
                class="flex-1 bg-red-600 hover:bg-red-500 text-white px-4 py-2 rounded-lg font-medium"
                :disabled="updateMutation.isPending.value"
              >
                Reject Application
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
