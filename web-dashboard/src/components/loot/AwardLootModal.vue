<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { useGuildContextStore } from '@/stores/guildContext'
import { useAuthStore } from '@/stores/auth'
import { useLootAward } from '@/composables/useLootAward'
import { flpsApi } from '@/api/flps'
import ItemAutocomplete from './ItemAutocomplete.vue'
import type { WowItem } from '@/types'

const props = defineProps<{
  isOpen: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()
const guildId = computed(() => guildContextStore.currentGuildId || authStore.user?.guildId)

// Form state
const selectedItem = ref<WowItem | null>(null)
const selectedRaiderId = ref<number | null>(null)
const notes = ref('')

// Fetch raiders for dropdown
const { data: raiders, isLoading: loadingRaiders } = useQuery({
  queryKey: ['flpsReport', guildId],
  queryFn: () => flpsApi.getFlpsReport(guildId.value!),
  enabled: computed(() => !!guildId.value && props.isOpen),
  select: (data) => data.raiders.map((r) => ({ id: r.raiderId, name: r.characterName })),
})

// Award mutation
const awardMutation = useLootAward(guildId)

const canSubmit = computed(() => {
  return selectedItem.value && selectedRaiderId.value && !awardMutation.isPending.value
})

async function handleSubmit() {
  if (!selectedItem.value || !selectedRaiderId.value) return

  await awardMutation.mutateAsync({
    raiderId: selectedRaiderId.value,
    itemId: selectedItem.value.id,
    itemName: selectedItem.value.name,
    notes: notes.value || undefined,
  })

  // Reset form and close
  selectedItem.value = null
  selectedRaiderId.value = null
  notes.value = ''
  emit('close')
}

function handleClose() {
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isOpen"
      class="fixed inset-0 z-50 flex items-center justify-center"
    >
      <!-- Backdrop -->
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="handleClose"
      />

      <!-- Modal -->
      <div class="relative bg-gray-900 border border-gray-700 rounded-xl shadow-2xl w-full max-w-md mx-4 p-6">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-xl font-bold">Award Loot</h2>
          <button
            type="button"
            class="text-gray-400 hover:text-white transition-colors"
            @click="handleClose"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- Item Search -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">
              Item
            </label>
            <ItemAutocomplete v-model="selectedItem" />
          </div>

          <!-- Raider Select -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">
              Raider
            </label>
            <select
              v-model="selectedRaiderId"
              class="input w-full"
              :disabled="loadingRaiders"
            >
              <option :value="null" disabled>Select a raider...</option>
              <option
                v-for="raider in raiders"
                :key="raider.id"
                :value="raider.id"
              >
                {{ raider.name }}
              </option>
            </select>
          </div>

          <!-- Notes -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">
              Notes (optional)
            </label>
            <textarea
              v-model="notes"
              rows="2"
              class="input w-full resize-none"
              placeholder="Add any notes..."
            />
          </div>

          <!-- Actions -->
          <div class="flex gap-3 pt-2">
            <button
              type="button"
              class="btn-secondary flex-1"
              @click="handleClose"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="btn-primary flex-1"
              :disabled="!canSubmit"
            >
              <span v-if="awardMutation.isPending.value" class="flex items-center justify-center gap-2">
                <svg class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                </svg>
                Awarding...
              </span>
              <span v-else>Award Loot</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </Teleport>
</template>
