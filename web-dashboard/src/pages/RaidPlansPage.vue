<script setup lang="ts">
/**
 * RaidPlansPage - List view for raid plans.
 *
 * Displays all raid plans for the guild with create/edit functionality.
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { raidPlanApi, type PlanVisibility } from '@/api/raidplan'
import { useAuthStore } from '@/stores/auth'
import SkeletonCard from '@/components/SkeletonCard.vue'

const router = useRouter()
const authStore = useAuthStore()
const queryClient = useQueryClient()

const currentPage = ref(0)
const pageSize = 20
const showCreateModal = ref(false)

// Form state for new plan
const newPlanForm = ref({
  encounterId: 0,
  encounterName: '',
  name: '',
  visibility: 'GUILD' as PlanVisibility,
})

// Fetch plans
const {
  data: plansData,
  isLoading,
} = useQuery({
  queryKey: ['raid-plans', authStore.guildId, currentPage],
  queryFn: () => raidPlanApi.getPlansByGuild(authStore.guildId!, currentPage.value, pageSize),
  enabled: computed(() => !!authStore.guildId),
})

// Create mutation
const createMutation = useMutation({
  mutationFn: () =>
    raidPlanApi.createPlan({
      guildId: authStore.guildId!,
      encounterId: newPlanForm.value.encounterId,
      encounterName: newPlanForm.value.encounterName,
      name: newPlanForm.value.name,
      createdBy: authStore.user?.id ?? 0,
      visibility: newPlanForm.value.visibility,
    }),
  onSuccess: (plan) => {
    queryClient.invalidateQueries({ queryKey: ['raid-plans'] })
    showCreateModal.value = false
    router.push(`/raid-plans/${plan.id}`)
  },
})

const plans = computed(() => plansData.value?.content ?? [])
const totalPages = computed(() => plansData.value?.totalPages ?? 1)
const showPagination = computed(() => totalPages.value > 1)

function openPlan(planId: string) {
  router.push(`/raid-plans/${planId}`)
}

function openCreateModal() {
  newPlanForm.value = {
    encounterId: 0,
    encounterName: '',
    name: '',
    visibility: 'GUILD',
  }
  showCreateModal.value = true
}

function closeCreateModal() {
  showCreateModal.value = false
}

function createPlan() {
  createMutation.mutate()
}

function getVisibilityLabel(visibility: PlanVisibility): string {
  const labels: Record<PlanVisibility, string> = {
    PRIVATE: 'Private',
    GUILD: 'Guild',
    PUBLIC: 'Public',
  }
  return labels[visibility]
}

function getVisibilityColor(visibility: PlanVisibility): string {
  const colors: Record<PlanVisibility, string> = {
    PRIVATE: 'text-gray-400',
    GUILD: 'text-blue-400',
    PUBLIC: 'text-green-400',
  }
  return colors[visibility]
}

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString()
}

function goToPage(page: number) {
  currentPage.value = page
}
</script>

<template>
  <div class="raid-plans-page p-6">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-white">Raid Plans</h1>
      <button
        data-testid="create-plan-button"
        class="btn-primary"
        @click="openCreateModal"
      >
        + Create Plan
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <SkeletonCard v-for="i in 6" :key="i" />
    </div>

    <!-- Empty State -->
    <div
      v-else-if="plans.length === 0"
      class="text-center py-16 text-gray-400"
    >
      <p class="text-lg mb-4">No raid plans yet</p>
      <p class="text-sm">Create your first raid plan to start positioning strategies.</p>
    </div>

    <!-- Plans Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="plan in plans"
        :key="plan.id"
        data-testid="plan-card"
        class="card cursor-pointer hover:border-blue-500 transition-colors"
        @click="openPlan(plan.id)"
      >
        <div class="flex items-start justify-between mb-2">
          <h3 class="font-semibold text-white">{{ plan.name }}</h3>
          <span
            :class="['text-xs px-2 py-1 rounded', getVisibilityColor(plan.visibility)]"
          >
            {{ getVisibilityLabel(plan.visibility) }}
          </span>
        </div>
        <p class="text-sm text-gray-400 mb-2">{{ plan.encounterName }}</p>
        <div class="flex items-center justify-between text-xs text-gray-500">
          <span>{{ plan.steps.length }} step{{ plan.steps.length !== 1 ? 's' : '' }}</span>
          <span>Updated {{ formatDate(plan.updatedAt) }}</span>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div
      v-if="showPagination"
      data-testid="pagination"
      class="flex items-center justify-center gap-2 mt-6"
    >
      <button
        class="btn-secondary"
        :disabled="currentPage === 0"
        @click="goToPage(currentPage - 1)"
      >
        Previous
      </button>
      <span class="text-gray-400">
        Page {{ currentPage + 1 }} of {{ totalPages }}
      </span>
      <button
        class="btn-secondary"
        :disabled="currentPage >= totalPages - 1"
        @click="goToPage(currentPage + 1)"
      >
        Next
      </button>
    </div>

    <!-- Create Plan Modal -->
    <div
      v-if="showCreateModal"
      data-testid="create-plan-modal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="closeCreateModal"
    >
      <div class="bg-gray-800 rounded-lg p-6 w-full max-w-md">
        <h2 class="text-xl font-bold text-white mb-4">Create New Plan</h2>

        <div class="space-y-4">
          <div>
            <label class="block text-sm text-gray-400 mb-1">Plan Name</label>
            <input
              v-model="newPlanForm.name"
              type="text"
              class="input w-full"
              placeholder="e.g., Phase 1 Positions"
            />
          </div>

          <div>
            <label class="block text-sm text-gray-400 mb-1">Encounter Name</label>
            <input
              v-model="newPlanForm.encounterName"
              type="text"
              class="input w-full"
              placeholder="e.g., Queen Ansurek"
            />
          </div>

          <div>
            <label class="block text-sm text-gray-400 mb-1">Encounter ID</label>
            <input
              v-model.number="newPlanForm.encounterId"
              type="number"
              class="input w-full"
              placeholder="e.g., 2902"
            />
          </div>

          <div>
            <label class="block text-sm text-gray-400 mb-1">Visibility</label>
            <select v-model="newPlanForm.visibility" class="input w-full">
              <option value="PRIVATE">Private</option>
              <option value="GUILD">Guild</option>
              <option value="PUBLIC">Public</option>
            </select>
          </div>
        </div>

        <div class="flex justify-end gap-2 mt-6">
          <button class="btn-secondary" @click="closeCreateModal">
            Cancel
          </button>
          <button
            class="btn-primary"
            :disabled="!newPlanForm.name || !newPlanForm.encounterName"
            @click="createPlan"
          >
            Create
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card {
  @apply bg-gray-800 border border-gray-700 rounded-lg p-4;
}

.btn-primary {
  @apply px-4 py-2 bg-blue-600 hover:bg-blue-500 rounded text-white text-sm font-medium transition-colors;
}

.btn-primary:disabled {
  @apply opacity-50 cursor-not-allowed hover:bg-blue-600;
}

.btn-secondary {
  @apply px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded text-white text-sm font-medium transition-colors;
}

.btn-secondary:disabled {
  @apply opacity-50 cursor-not-allowed hover:bg-gray-700;
}

.input {
  @apply bg-gray-700 border border-gray-600 rounded px-3 py-2 text-white placeholder-gray-400 focus:outline-none focus:border-blue-500;
}
</style>
