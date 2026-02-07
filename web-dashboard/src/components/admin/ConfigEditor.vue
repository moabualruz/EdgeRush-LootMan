<script setup lang="ts">
import { ref, watch } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { flpsApi } from '@/api/flps'
import { useToast } from '@/composables/useToast'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import type { FlpsConfig } from '@/types'

const props = defineProps<{
  guildId: string
}>()

const queryClient = useQueryClient()
const toast = useToast()

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
    toast.success('Configuration Saved', 'FLPS configuration updated successfully!')
  },
  onError: () => {
    toast.error('Save Failed', 'Failed to save configuration. Please try again.')
  },
})

async function previewChanges() {
  if (!editedConfig.value) return
  isPreviewLoading.value = true
  try {
    previewResult.value = await flpsApi.previewConfig(props.guildId, editedConfig.value)
  } catch (error) {
    toast.error('Preview Failed', 'Failed to preview configuration changes.')
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
    toast.info('Changes Reset', 'Configuration changes have been reverted across the editor.')
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
          <BaseInput
            v-model.number="editedConfig.rmsWeights.attendance"
            type="number"
            limit-min="0"
            limit-max="1"
            step="0.1"
            label="Attendance Weight"
          />
          <BaseInput
            v-model.number="editedConfig.rmsWeights.mechanical"
            type="number"
            limit-min="0"
            limit-max="1"
            step="0.1"
            label="Mechanical Weight"
          />
          <BaseInput
            v-model.number="editedConfig.rmsWeights.preparation"
            type="number"
            limit-min="0"
            limit-max="1"
            step="0.1"
            label="Preparation Weight"
          />
        </div>
      </div>

      <!-- IPI Weights -->
      <div class="card">
        <h3 class="text-lg font-semibold mb-4">IPI Weights</h3>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <BaseInput
            v-model.number="editedConfig.ipiWeights.upgradeValue"
            type="number"
            limit-min="0"
            limit-max="1"
            step="0.1"
            label="Upgrade Value Weight"
          />
          <BaseInput
            v-model.number="editedConfig.ipiWeights.tierBonus"
            type="number"
            limit-min="0"
            limit-max="1"
            step="0.1"
            label="Tier Bonus Weight"
          />
          <BaseInput
            v-model.number="editedConfig.ipiWeights.roleMultiplier"
            type="number"
            limit-min="0"
            limit-max="1"
            step="0.1"
            label="Role Multiplier Weight"
          />
        </div>
      </div>

      <!-- Thresholds -->
      <div class="card">
        <h3 class="text-lg font-semibold mb-4">Eligibility Thresholds</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <BaseInput
              v-model.number="editedConfig.thresholds.eligibilityAttendance"
              type="number"
              limit-min="0"
              limit-max="1"
              step="0.05"
              label="Attendance Threshold"
            />
            <p class="text-xs text-gray-500 mt-1 ml-1">Minimum attendance required for eligibility</p>
          </div>
          <div>
            <BaseInput
              v-model.number="editedConfig.thresholds.eligibilityActivity"
              type="number"
              limit-min="0"
              limit-max="1"
              step="0.05"
              label="Activity Threshold"
            />
            <p class="text-xs text-gray-500 mt-1 ml-1">Minimum activity required for eligibility</p>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="flex items-center justify-end space-x-4">
        <BaseButton variant="secondary" @click="resetChanges">
          Reset
        </BaseButton>
        <BaseButton variant="secondary" @click="previewChanges" :loading="isPreviewLoading">
          {{ isPreviewLoading ? 'Loading...' : 'Preview Changes' }}
        </BaseButton>
        <BaseButton
          @click="saveChanges"
          :loading="updateMutation.isPending.value"
        >
          {{ updateMutation.isPending.value ? 'Saving...' : 'Save Changes' }}
        </BaseButton>
      </div>

      <!-- Preview result -->
      <div v-if="previewResult" class="card bg-blue-900/20 border-blue-700">
        <h3 class="font-semibold text-blue-400 mb-2">Preview Results</h3>
        <pre class="text-sm text-gray-300 overflow-auto">{{ JSON.stringify(previewResult, null, 2) }}</pre>
      </div>
    </div>
  </div>
</template>
