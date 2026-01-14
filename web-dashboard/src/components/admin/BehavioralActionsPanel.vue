<script setup lang="ts">
import { ref } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { adminApi } from '@/api/admin'
import type { BehavioralAction } from '@/types'
import { formatDate } from '@/utils/date'

const props = defineProps<{
  guildId: string
}>()

const queryClient = useQueryClient()

const { data: actions, isLoading } = useQuery({
  queryKey: ['behavioralActions', props.guildId],
  queryFn: () => adminApi.getBehavioralActions(props.guildId),
})

const showCreateModal = ref(false)
const newAction = ref({
  raiderId: 0,
  characterName: '',
  actionType: 'PENALTY' as 'PENALTY' | 'BONUS',
  reason: '',
  flpsModifier: 0,
  startDate: new Date().toISOString().split('T')[0],
  endDate: '',
})

const createMutation = useMutation({
  mutationFn: (action: typeof newAction.value) => adminApi.createBehavioralAction(props.guildId, action),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['behavioralActions', props.guildId] })
    showCreateModal.value = false
    resetForm()
  },
})

const deleteMutation = useMutation({
  mutationFn: (actionId: number) => adminApi.deleteBehavioralAction(props.guildId, actionId),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['behavioralActions', props.guildId] })
  },
})

function resetForm() {
  newAction.value = {
    raiderId: 0,
    characterName: '',
    actionType: 'PENALTY',
    reason: '',
    flpsModifier: 0,
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
  }
}

function handleCreate() {
  createMutation.mutate(newAction.value)
}

function handleDelete(actionId: number) {
  if (confirm('Are you sure you want to delete this action?')) {
    deleteMutation.mutate(actionId)
  }
}
</script>

<template>
  <div>
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-lg font-semibold">Behavioral Actions</h2>
      <button @click="showCreateModal = true" class="btn-primary">
        Create Action
      </button>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Actions list -->
    <div v-else-if="actions" class="space-y-4">
      <div v-if="actions.length === 0" class="card text-center py-8">
        <p class="text-gray-400">No behavioral actions found.</p>
      </div>

      <div
        v-for="action in actions"
        :key="action.id"
        class="card flex items-center justify-between"
      >
        <div class="flex-1">
          <div class="flex items-center space-x-2">
            <span class="font-semibold">{{ action.characterName }}</span>
            <span
              :class="[
                'text-xs px-2 py-0.5 rounded',
                action.actionType === 'PENALTY' ? 'bg-red-900/50 text-red-400' : 'bg-green-900/50 text-green-400'
              ]"
            >
              {{ action.actionType }}
            </span>
            <span
              :class="[
                'text-xs px-2 py-0.5 rounded',
                action.active ? 'bg-yellow-900/50 text-yellow-400' : 'bg-gray-700 text-gray-400'
              ]"
            >
              {{ action.active ? 'Active' : 'Expired' }}
            </span>
          </div>
          <p class="text-sm text-gray-400 mt-1">{{ action.reason }}</p>
          <p class="text-xs text-gray-500 mt-1">
            {{ formatDate(action.startDate) }}
            <span v-if="action.endDate"> - {{ formatDate(action.endDate) }}</span>
            · Modifier: {{ action.flpsModifier > 0 ? '+' : '' }}{{ action.flpsModifier }}
          </p>
        </div>

        <button
          @click="handleDelete(action.id)"
          class="text-red-400 hover:text-red-300 transition-colors"
          :disabled="deleteMutation.isPending.value"
        >
          Delete
        </button>
      </div>
    </div>

    <!-- Create modal -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50"
      @click.self="showCreateModal = false"
    >
      <div class="card max-w-md w-full">
        <h3 class="text-lg font-semibold mb-4">Create Behavioral Action</h3>

        <div class="space-y-4">
          <div>
            <label class="label">Character Name</label>
            <input v-model="newAction.characterName" type="text" class="input" />
          </div>

          <div>
            <label class="label">Action Type</label>
            <select v-model="newAction.actionType" class="input">
              <option value="PENALTY">Penalty</option>
              <option value="BONUS">Bonus</option>
            </select>
          </div>

          <div>
            <label class="label">Reason</label>
            <textarea v-model="newAction.reason" class="input" rows="2"></textarea>
          </div>

          <div>
            <label class="label">FLPS Modifier</label>
            <input v-model.number="newAction.flpsModifier" type="number" step="0.01" class="input" />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="label">Start Date</label>
              <input v-model="newAction.startDate" type="date" class="input" />
            </div>
            <div>
              <label class="label">End Date (optional)</label>
              <input v-model="newAction.endDate" type="date" class="input" />
            </div>
          </div>
        </div>

        <div class="flex justify-end space-x-3 mt-6">
          <button @click="showCreateModal = false" class="btn-secondary">Cancel</button>
          <button
            @click="handleCreate"
            class="btn-primary"
            :disabled="createMutation.isPending.value"
          >
            {{ createMutation.isPending.value ? 'Creating...' : 'Create' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
