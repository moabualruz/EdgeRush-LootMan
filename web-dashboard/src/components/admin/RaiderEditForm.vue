<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useMutation, useQueryClient } from '@tanstack/vue-query'
import { adminApi } from '@/api/admin'
import { useToast } from '@/composables/useToast'

const props = defineProps<{
  guildId: string
  raiderId: number
  characterName: string
  currentRank: string
  isActive: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const queryClient = useQueryClient()
const toast = useToast()

// Form state
const rank = ref(props.currentRank)
const active = ref(props.isActive)

// Available ranks
const ranks = ['Trial', 'Raider', 'Core Raider', 'Officer', 'Guild Master']

// Reset form when props change
watch(() => [props.currentRank, props.isActive], () => {
  rank.value = props.currentRank
  active.value = props.isActive
})

// Update mutation
const updateMutation = useMutation({
  mutationFn: () => adminApi.updateRaider(props.guildId, props.raiderId, {
    rank: rank.value,
    isActive: active.value,
  }),
  onSuccess: () => {
    toast.success('Raider Updated', 'Raider settings have been saved.')
    queryClient.invalidateQueries({ queryKey: ['flpsReport', props.guildId] })
    queryClient.invalidateQueries({ queryKey: ['leaderboard', props.guildId] })
    emit('close')
  },
  onError: () => {
    toast.error('Update Failed', 'Could not update raider. Please try again.')
  },
})

const canSubmit = computed(() => {
  return !updateMutation.isPending.value && (
    rank.value !== props.currentRank || active.value !== props.isActive
  )
})

function handleSubmit() {
  updateMutation.mutate()
}

function handleCancel() {
  emit('close')
}
</script>

<template>
  <div class="border-t border-gray-700 pt-4 mt-4">
    <h3 class="text-sm font-semibold text-gray-300 mb-4 flex items-center gap-2">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
      </svg>
      Edit {{ characterName }}
    </h3>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <!-- Rank Select -->
      <div>
        <label for="rank" class="block text-sm font-medium text-gray-400 mb-1">
          Rank
        </label>
        <select
          id="rank"
          v-model="rank"
          data-testid="rank-select"
          class="input w-full"
        >
          <option v-for="r in ranks" :key="r" :value="r">{{ r }}</option>
        </select>
      </div>

      <!-- Active Status Toggle -->
      <div class="flex items-center justify-between">
        <label for="status" class="text-sm font-medium text-gray-400">
          Active Status
        </label>
        <label class="relative inline-flex items-center cursor-pointer">
          <input
            id="status"
            type="checkbox"
            v-model="active"
            data-testid="status-toggle"
            class="sr-only peer"
          />
          <div class="w-11 h-6 bg-gray-700 peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary/50 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-green-600"></div>
          <span class="ml-3 text-sm" :class="active ? 'text-green-400' : 'text-gray-500'">
            {{ active ? 'Active' : 'Inactive' }}
          </span>
        </label>
      </div>

      <!-- Actions -->
      <div class="flex gap-3 pt-2">
        <button
          type="button"
          data-testid="cancel-button"
          class="btn-secondary flex-1"
          @click="handleCancel"
        >
          Cancel
        </button>
        <button
          type="submit"
          class="btn-primary flex-1"
          :disabled="!canSubmit"
        >
          <span v-if="updateMutation.isPending.value" class="flex items-center justify-center gap-2">
            <svg class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
            </svg>
            Saving...
          </span>
          <span v-else>Save Changes</span>
        </button>
      </div>
    </form>
  </div>
</template>
