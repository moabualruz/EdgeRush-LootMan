<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useGuildContextStore } from '@/stores/guildContext'
import { useAuthStore } from '@/stores/auth'
import { useLootEdit } from '@/composables/useLootEdit'
import type { LootAward } from '@/types'

const props = defineProps<{
  isOpen: boolean
  award: LootAward | null
}>()

const emit = defineEmits<{
  close: []
}>()

const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()
const guildId = computed(() => guildContextStore.currentGuildId || authStore.user?.guildId)

// Form state
const notes = ref('')

// Reset form when modal opens with new award
watch(() => props.award, (newAward) => {
  if (newAward) {
    notes.value = newAward.notes || ''
  }
}, { immediate: true })

// Edit mutation
const editMutation = useLootEdit(guildId)

const canSubmit = computed(() => {
  return props.award && !editMutation.isPending.value
})

async function handleSubmit() {
  if (!props.award) return

  await editMutation.mutateAsync({
    awardId: props.award.id,
    notes: notes.value || undefined,
  })

  emit('close')
}

function handleClose() {
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="isOpen && award"
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
          <h2 class="text-xl font-bold">Edit Loot Award</h2>
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

        <!-- Item Info -->
        <div class="mb-4 p-3 bg-gray-800/50 rounded-lg">
          <div class="text-sm text-gray-400">Item</div>
          <div class="text-purple-400 font-medium">{{ award.itemName }}</div>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- Notes -->
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1">
              Notes
            </label>
            <textarea
              v-model="notes"
              rows="3"
              class="input w-full resize-none"
              placeholder="Add any notes about this award..."
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
              <span v-if="editMutation.isPending.value" class="flex items-center justify-center gap-2">
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
    </div>
  </Teleport>
</template>
