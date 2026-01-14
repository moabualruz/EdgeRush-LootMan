<script setup lang="ts">
import { ref } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { adminApi } from '@/api/admin'
import { formatDate } from '@/utils/date'

const props = defineProps<{
  guildId: string
}>()

const queryClient = useQueryClient()

const { data: bans, isLoading } = useQuery({
  queryKey: ['lootBans', props.guildId],
  queryFn: () => adminApi.getLootBans(props.guildId),
})

const showCreateModal = ref(false)
const newBan = ref({
  raiderId: 0,
  characterName: '',
  reason: '',
  startDate: new Date().toISOString().split('T')[0],
  endDate: '',
})

const createMutation = useMutation({
  mutationFn: (ban: typeof newBan.value) => adminApi.createLootBan(props.guildId, ban),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['lootBans', props.guildId] })
    showCreateModal.value = false
    resetForm()
  },
})

const deleteMutation = useMutation({
  mutationFn: (banId: number) => adminApi.deleteLootBan(props.guildId, banId),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['lootBans', props.guildId] })
  },
})

function resetForm() {
  newBan.value = {
    raiderId: 0,
    characterName: '',
    reason: '',
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
  }
}

function handleCreate() {
  createMutation.mutate(newBan.value)
}

function handleDelete(banId: number) {
  if (confirm('Are you sure you want to delete this ban?')) {
    deleteMutation.mutate(banId)
  }
}
</script>

<template>
  <div>
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-lg font-semibold">Loot Bans</h2>
      <button @click="showCreateModal = true" class="btn-primary">
        Create Ban
      </button>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Bans list -->
    <div v-else-if="bans" class="space-y-4">
      <div v-if="bans.length === 0" class="card text-center py-8">
        <p class="text-gray-400">No loot bans found.</p>
      </div>

      <div
        v-for="ban in bans"
        :key="ban.id"
        class="card flex items-center justify-between"
      >
        <div class="flex-1">
          <div class="flex items-center space-x-2">
            <span class="font-semibold">{{ ban.characterName }}</span>
            <span
              :class="[
                'text-xs px-2 py-0.5 rounded',
                ban.active ? 'bg-red-900/50 text-red-400' : 'bg-gray-700 text-gray-400'
              ]"
            >
              {{ ban.active ? 'Active Ban' : 'Expired' }}
            </span>
          </div>
          <p class="text-sm text-gray-400 mt-1">{{ ban.reason }}</p>
          <p class="text-xs text-gray-500 mt-1">
            {{ formatDate(ban.startDate) }}
            <span v-if="ban.endDate"> - {{ formatDate(ban.endDate) }}</span>
            · Created by {{ ban.createdBy }}
          </p>
        </div>

        <button
          @click="handleDelete(ban.id)"
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
        <h3 class="text-lg font-semibold mb-4">Create Loot Ban</h3>

        <div class="space-y-4">
          <div>
            <label class="label">Character Name</label>
            <input v-model="newBan.characterName" type="text" class="input" />
          </div>

          <div>
            <label class="label">Reason</label>
            <textarea v-model="newBan.reason" class="input" rows="3"></textarea>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="label">Start Date</label>
              <input v-model="newBan.startDate" type="date" class="input" />
            </div>
            <div>
              <label class="label">End Date (optional)</label>
              <input v-model="newBan.endDate" type="date" class="input" />
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
