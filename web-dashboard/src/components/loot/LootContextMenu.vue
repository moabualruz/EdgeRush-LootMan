<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  isOpen: boolean
  position: { x: number; y: number }
  awardId: number
  itemName: string
}>()

const emit = defineEmits<{
  edit: [awardId: number]
  revoke: [awardId: number]
  close: []
}>()

function handleEdit() {
  emit('edit', props.awardId)
  emit('close')
}

function handleRevoke() {
  emit('revoke', props.awardId)
  emit('close')
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    emit('close')
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen">
      <!-- Backdrop -->
      <div
        data-testid="backdrop"
        class="fixed inset-0 z-40"
        @click="emit('close')"
      />

      <!-- Menu -->
      <div
        data-testid="context-menu"
        class="fixed z-50 min-w-[160px] bg-gray-800 border border-gray-700 rounded-lg shadow-xl py-1"
        :style="{ left: `${position.x}px`, top: `${position.y}px` }"
      >
        <div class="px-3 py-2 text-xs text-gray-400 border-b border-gray-700 truncate max-w-[200px]">
          {{ itemName }}
        </div>

        <button
          data-testid="edit-button"
          class="w-full px-3 py-2 text-left text-sm text-gray-200 hover:bg-gray-700 flex items-center gap-2"
          @click="handleEdit"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
          </svg>
          Edit Notes
        </button>

        <button
          data-testid="revoke-button"
          class="w-full px-3 py-2 text-left text-sm text-red-400 hover:bg-gray-700 flex items-center gap-2"
          @click="handleRevoke"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
          </svg>
          Revoke Award
        </button>
      </div>
    </div>
  </Teleport>
</template>
