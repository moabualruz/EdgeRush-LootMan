<script setup lang="ts">
/**
 * StepTimeline - Navigation component for raid plan steps.
 *
 * Allows navigating between plan steps and managing step order.
 */
import { computed } from 'vue'
import type { PlanStep } from '@/api/raidplan'

export interface StepTimelineProps {
  steps: PlanStep[]
  currentStep: number
}

const props = defineProps<StepTimelineProps>()

const emit = defineEmits<{
  'step-change': [index: number]
  'add-step': []
  'delete-step': [index: number]
  'notes-edit': [notes: string]
}>()

const currentStepData = computed(() => props.steps[props.currentStep])
const hasPrev = computed(() => props.currentStep > 0)
const hasNext = computed(() => props.currentStep < props.steps.length - 1)
const canDelete = computed(() => props.steps.length > 1)

function goToStep(index: number) {
  emit('step-change', index)
}

function goPrev() {
  if (hasPrev.value) {
    emit('step-change', props.currentStep - 1)
  }
}

function goNext() {
  if (hasNext.value) {
    emit('step-change', props.currentStep + 1)
  }
}

function addStep() {
  emit('add-step')
}

function deleteStep() {
  if (canDelete.value) {
    emit('delete-step', props.currentStep)
  }
}

function editNotes() {
  const currentNotes = currentStepData.value?.notes || ''
  const newNotes = prompt('Enter notes for this step:', currentNotes)
  if (newNotes !== null) {
      emit('notes-edit', newNotes)
  }
}
</script>

<template>
  <div
    data-testid="step-timeline"
    class="step-timeline bg-gray-800 rounded-lg p-4"
  >
    <!-- Navigation Controls -->
    <div class="flex items-center justify-between mb-4">
      <button
        data-testid="prev-button"
        class="nav-btn"
        :disabled="!hasPrev"
        @click="goPrev"
      >
        <span>\u2190</span> Prev
      </button>

      <div class="flex items-center gap-2">
        <button
          data-testid="add-step-button"
          class="action-btn text-green-500 hover:text-green-400"
          title="Add Step"
          @click="addStep"
        >
          <span class="text-lg">+</span>
        </button>
        <button
          data-testid="delete-step-button"
          class="action-btn text-red-500 hover:text-red-400"
          title="Delete Step"
          :disabled="!canDelete"
          @click="deleteStep"
        >
          <span class="text-lg">\u2715</span>
        </button>
      </div>

      <button
        data-testid="next-button"
        class="nav-btn"
        :disabled="!hasNext"
        @click="goNext"
      >
        Next <span>\u2192</span>
      </button>
    </div>

    <!-- Step Indicators -->
    <div class="flex items-center justify-center gap-2 mb-4">
      <button
        v-for="(step, index) in steps"
        :key="index"
        data-testid="step-indicator"
        :class="['step-indicator', { active: index === currentStep }]"
        @click="goToStep(index)"
      >
        {{ index + 1 }}
      </button>
    </div>

    <!-- Current Step Notes -->
    <div
      v-if="steps.length > 0"
      data-testid="step-notes"
      class="step-notes bg-gray-700 rounded p-3 cursor-pointer hover:bg-gray-600 transition-colors"
      @click="editNotes"
    >
      <div v-if="currentStepData?.notes" class="text-sm text-gray-200">
        {{ currentStepData.notes }}
      </div>
      <div v-else class="text-sm text-gray-500 italic">
        Click to add notes
      </div>
    </div>
  </div>
</template>

<style scoped>
.nav-btn {
  @apply px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded text-white text-sm transition-colors flex items-center gap-1;
}

.nav-btn:disabled {
  @apply opacity-50 cursor-not-allowed hover:bg-gray-700;
}

.action-btn {
  @apply w-8 h-8 flex items-center justify-center rounded bg-gray-700 hover:bg-gray-600 transition-colors cursor-pointer;
}

.action-btn:disabled {
  @apply opacity-50 cursor-not-allowed hover:bg-gray-700;
}

.step-indicator {
  @apply w-8 h-8 rounded-full bg-gray-700 text-gray-300 text-sm font-medium flex items-center justify-center cursor-pointer transition-all hover:bg-gray-600;
}

.step-indicator.active {
  @apply bg-blue-600 text-white ring-2 ring-blue-400;
}
</style>
