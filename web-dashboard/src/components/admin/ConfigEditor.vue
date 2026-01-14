<script setup lang="ts">
import { ref, watch } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { flpsApi } from '@/api/flps'
import type { FlpsConfig } from '@/types'

const props = defineProps<{
  guildId: string
}>()

const queryClient = useQueryClient()

const { data: config, isLoading } = useQuery({
  queryKey: ['flpsConfig', props.guildId],
  queryFn: () => flpsApi.getConfig(props.guildId),
})

const editedConfig = ref<FlpsConfig | null>(null)
const previewResult = ref<unknown>(null)
const isPreviewLoading = ref(false)

watch(config, (newConfig) => {
  if (newConfig) {
    editedConfig.value = JSON.parse(JSON.stringify(newConfig))
  }
})

const updateMutation = useMutation({
  mutationFn: (config: Partial<FlpsConfig>) => flpsApi.updateConfig(props.guildId, config),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['flpsConfig', props.guildId] })
  },
})

async function previewChanges() {
  if (!editedConfig.value) return
  isPreviewLoading.value = true
  try {
    previewResult.value = await flpsApi.previewConfig(props.guildId, editedConfig.value)
  } finally {
    isPreviewLoading.value = false
  }
}

function saveChanges() {
  if (!editedConfig.value) return
  updateMutation.mutate(editedConfig.value)
}

function resetChanges() {
  if (config.value) {
    editedConfig.value = JSON.parse(JSON.stringify(config.value))
    previewResult.value = null
  }
}
</script>

<template>
  <div>
    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Config editor -->
    <div v-else-if="editedConfig" class="space-y-6">
      <!-- RMS Weights -->
      <div class="card">
        <h3 class="text-lg font-semibold mb-4">RMS Weights</h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="label">Attendance Weight</label>
            <input
              v-model.number="editedConfig.rmsWeights.attendance"
              type="number"
              step="0.1"
              min="0"
              max="1"
              class="input"
            />
          </div>
          <div>
            <label class="label">Mechanical Weight</label>
            <input
              v-model.number="editedConfig.rmsWeights.mechanical"
              type="number"
              step="0.1"
              min="0"
              max="1"
              class="input"
            />
          </div>
          <div>
            <label class="label">Preparation Weight</label>
            <input
              v-model.number="editedConfig.rmsWeights.preparation"
              type="number"
              step="0.1"
              min="0"
              max="1"
              class="input"
            />
          </div>
        </div>
      </div>

      <!-- IPI Weights -->
      <div class="card">
        <h3 class="text-lg font-semibold mb-4">IPI Weights</h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="label">Upgrade Value Weight</label>
            <input
              v-model.number="editedConfig.ipiWeights.upgradeValue"
              type="number"
              step="0.1"
              min="0"
              max="1"
              class="input"
            />
          </div>
          <div>
            <label class="label">Tier Bonus Weight</label>
            <input
              v-model.number="editedConfig.ipiWeights.tierBonus"
              type="number"
              step="0.1"
              min="0"
              max="1"
              class="input"
            />
          </div>
          <div>
            <label class="label">Role Multiplier Weight</label>
            <input
              v-model.number="editedConfig.ipiWeights.roleMultiplier"
              type="number"
              step="0.1"
              min="0"
              max="1"
              class="input"
            />
          </div>
        </div>
      </div>

      <!-- Thresholds -->
      <div class="card">
        <h3 class="text-lg font-semibold mb-4">Eligibility Thresholds</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="label">Attendance Threshold</label>
            <input
              v-model.number="editedConfig.thresholds.eligibilityAttendance"
              type="number"
              step="0.05"
              min="0"
              max="1"
              class="input"
            />
            <p class="text-xs text-gray-500 mt-1">Minimum attendance required for eligibility</p>
          </div>
          <div>
            <label class="label">Activity Threshold</label>
            <input
              v-model.number="editedConfig.thresholds.eligibilityActivity"
              type="number"
              step="0.05"
              min="0"
              max="1"
              class="input"
            />
            <p class="text-xs text-gray-500 mt-1">Minimum activity required for eligibility</p>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center justify-end space-x-4">
        <button @click="resetChanges" class="btn-secondary">
          Reset
        </button>
        <button @click="previewChanges" class="btn-secondary" :disabled="isPreviewLoading">
          {{ isPreviewLoading ? 'Loading...' : 'Preview Changes' }}
        </button>
        <button
          @click="saveChanges"
          class="btn-primary"
          :disabled="updateMutation.isPending.value"
        >
          {{ updateMutation.isPending.value ? 'Saving...' : 'Save Changes' }}
        </button>
      </div>

      <!-- Preview result -->
      <div v-if="previewResult" class="card bg-blue-900/20 border-blue-700">
        <h3 class="font-semibold text-blue-400 mb-2">Preview Results</h3>
        <pre class="text-sm text-gray-300 overflow-auto">{{ JSON.stringify(previewResult, null, 2) }}</pre>
      </div>

      <!-- Success/Error messages -->
      <div v-if="updateMutation.isSuccess.value" class="card bg-green-900/20 border-green-700">
        <p class="text-green-400">Configuration saved successfully!</p>
      </div>
      <div v-if="updateMutation.isError.value" class="card bg-red-900/20 border-red-700">
        <p class="text-red-400">Failed to save configuration. Please try again.</p>
      </div>
    </div>
  </div>
</template>
