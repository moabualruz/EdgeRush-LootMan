<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import {
  discordApi,
  type DiscordNotificationConfig,
  type UpsertNotificationConfigRequest,
} from '@/api/discord'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'

const queryClient = useQueryClient()

// Edit modal state
const showEditModal = ref(false)
const editingConfig = ref<DiscordNotificationConfig | null>(null)
const isCreating = ref(false)

// Form state
const formDiscordServerId = ref('')
const formNotificationType = ref('')
const formChannelId = ref('')
const formEnabled = ref(true)
const formMentionRoleId = ref('')

// Query for configs
const { data: configsData, isLoading, error, refetch } = useQuery({
  queryKey: ['discordConfigs', GUILD_ID],
  queryFn: () => discordApi.getConfigs(GUILD_ID),
})

// Mutations
const upsertMutation = useMutation({
  mutationFn: (request: UpsertNotificationConfigRequest) =>
    discordApi.upsertConfig(GUILD_ID, request),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['discordConfigs'] })
    closeModal()
  },
})

const updateMutation = useMutation({
  mutationFn: ({ configId, request }: { configId: number; request: { channelId?: string; enabled?: boolean; mentionRoleId?: string | null } }) =>
    discordApi.updateConfig(GUILD_ID, configId, request),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['discordConfigs'] })
    closeModal()
  },
})

const deleteMutation = useMutation({
  mutationFn: (configId: number) => discordApi.deleteConfig(GUILD_ID, configId),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['discordConfigs'] })
  },
})

const testMutation = useMutation({
  mutationFn: (type: string) => discordApi.testNotification(GUILD_ID, type),
})

// Computed
const configs = computed(() => configsData.value?.configs ?? [])
const availableTypes = computed(() => configsData.value?.availableTypes ?? [])
const unconfiguredTypes = computed(() => {
  const configuredTypes = new Set(configs.value.map((c) => c.notificationType))
  return availableTypes.value.filter((t) => !configuredTypes.has(t))
})

// Type display helpers
const typeDescriptions: Record<string, string> = {
  LOOT_AWARD: 'When loot is awarded to a raider',
  RDF_EXPIRY: 'When RDF penalty expires for a raider',
  PENALTY: 'When a behavioral penalty is applied',
  LOOT_BAN: 'When a loot ban is created or modified',
  SYNC_COMPLETE: 'When data sync completes',
}

function getTypeDescription(type: string): string {
  return typeDescriptions[type] ?? 'Notification type'
}

function getTypeColor(type: string): string {
  const colors: Record<string, string> = {
    LOOT_AWARD: 'text-green-400',
    RDF_EXPIRY: 'text-yellow-400',
    PENALTY: 'text-red-400',
    LOOT_BAN: 'text-red-600',
    SYNC_COMPLETE: 'text-blue-400',
  }
  return colors[type] ?? 'text-gray-400'
}

// Modal actions
function openCreateModal() {
  isCreating.value = true
  editingConfig.value = null
  formDiscordServerId.value = ''
  formNotificationType.value = unconfiguredTypes.value[0] ?? ''
  formChannelId.value = ''
  formEnabled.value = true
  formMentionRoleId.value = ''
  showEditModal.value = true
}

function openEditModal(config: DiscordNotificationConfig) {
  isCreating.value = false
  editingConfig.value = config
  formDiscordServerId.value = config.discordServerId
  formNotificationType.value = config.notificationType
  formChannelId.value = config.channelId
  formEnabled.value = config.enabled
  formMentionRoleId.value = config.mentionRoleId ?? ''
  showEditModal.value = true
}

function closeModal() {
  showEditModal.value = false
  editingConfig.value = null
  isCreating.value = false
}

function saveConfig() {
  if (isCreating.value) {
    upsertMutation.mutate({
      discordServerId: formDiscordServerId.value,
      notificationType: formNotificationType.value,
      channelId: formChannelId.value,
      enabled: formEnabled.value,
      mentionRoleId: formMentionRoleId.value || null,
    })
  } else if (editingConfig.value) {
    updateMutation.mutate({
      configId: editingConfig.value.id,
      request: {
        channelId: formChannelId.value,
        enabled: formEnabled.value,
        mentionRoleId: formMentionRoleId.value || null,
      },
    })
  }
}

function deleteConfig(config: DiscordNotificationConfig) {
  if (confirm(`Delete notification configuration for ${config.notificationType}?`)) {
    deleteMutation.mutate(config.id)
  }
}

function toggleEnabled(config: DiscordNotificationConfig) {
  updateMutation.mutate({
    configId: config.id,
    request: { enabled: !config.enabled },
  })
}

function testNotification(type: string) {
  testMutation.mutate(type)
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold">Discord Notifications</h1>
        <p class="text-gray-400 text-sm mt-1">
          Configure which Discord channels receive notifications
        </p>
      </div>
      <div class="flex items-center space-x-4">
        <button
          @click="refetch()"
          class="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg text-sm"
        >
          Refresh
        </button>
        <button
          v-if="unconfiguredTypes.length > 0"
          @click="openCreateModal"
          class="bg-primary-600 hover:bg-primary-500 px-4 py-2 rounded-lg text-sm font-medium"
        >
          Add Configuration
        </button>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="card bg-red-900/20 border-red-700">
      <p class="text-red-400">Failed to load configurations. Please try again.</p>
    </div>

    <!-- Empty state -->
    <div v-else-if="configs.length === 0" class="card text-center py-12">
      <p class="text-gray-400 mb-4">No notification configurations set up yet.</p>
      <button
        @click="openCreateModal"
        class="bg-primary-600 hover:bg-primary-500 px-4 py-2 rounded-lg text-sm font-medium"
      >
        Create First Configuration
      </button>
    </div>

    <!-- Configurations list -->
    <div v-else class="space-y-4">
      <div
        v-for="config in configs"
        :key="config.id"
        class="card"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-4">
            <div
              :class="[
                'w-3 h-3 rounded-full',
                config.enabled ? 'bg-green-500' : 'bg-gray-500'
              ]"
            ></div>
            <div>
              <div class="flex items-center space-x-2">
                <span :class="['font-medium', getTypeColor(config.notificationType)]">
                  {{ config.notificationType.replace('_', ' ') }}
                </span>
                <span
                  v-if="!config.enabled"
                  class="text-xs bg-gray-700 text-gray-400 px-2 py-0.5 rounded"
                >
                  Disabled
                </span>
              </div>
              <p class="text-sm text-gray-400">
                {{ getTypeDescription(config.notificationType) }}
              </p>
            </div>
          </div>

          <div class="flex items-center space-x-4">
            <div class="text-right">
              <div class="text-sm text-gray-400">Channel ID</div>
              <div class="font-mono text-sm">{{ config.channelId }}</div>
            </div>
            <div v-if="config.mentionRoleId" class="text-right">
              <div class="text-sm text-gray-400">Mention Role</div>
              <div class="font-mono text-sm">{{ config.mentionRoleId }}</div>
            </div>

            <div class="flex items-center space-x-2">
              <button
                @click="testNotification(config.notificationType)"
                :disabled="testMutation.isPending.value || !config.enabled"
                class="text-blue-400 hover:text-blue-300 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Test
              </button>
              <button
                @click="toggleEnabled(config)"
                :disabled="updateMutation.isPending.value"
                class="text-yellow-400 hover:text-yellow-300 text-sm disabled:opacity-50"
              >
                {{ config.enabled ? 'Disable' : 'Enable' }}
              </button>
              <button
                @click="openEditModal(config)"
                class="text-gray-400 hover:text-white text-sm"
              >
                Edit
              </button>
              <button
                @click="deleteConfig(config)"
                :disabled="deleteMutation.isPending.value"
                class="text-red-400 hover:text-red-300 text-sm disabled:opacity-50"
              >
                Delete
              </button>
            </div>
          </div>
        </div>

        <!-- Test result -->
        <div
          v-if="testMutation.isSuccess.value && testMutation.variables.value === config.notificationType"
          class="mt-4 p-3 rounded-lg"
          :class="testMutation.data.value?.success ? 'bg-green-900/30' : 'bg-red-900/30'"
        >
          <p :class="testMutation.data.value?.success ? 'text-green-400' : 'text-red-400'">
            {{ testMutation.data.value?.message }}
          </p>
        </div>
      </div>

      <!-- Unconfigured types hint -->
      <div v-if="unconfiguredTypes.length > 0" class="card bg-gray-800/50">
        <p class="text-sm text-gray-400 mb-2">Available notification types not yet configured:</p>
        <div class="flex flex-wrap gap-2">
          <span
            v-for="type in unconfiguredTypes"
            :key="type"
            class="text-xs bg-gray-700 px-2 py-1 rounded"
          >
            {{ type.replace('_', ' ') }}
          </span>
        </div>
      </div>
    </div>

    <!-- Edit/Create Modal -->
    <Teleport to="body">
      <div
        v-if="showEditModal"
        class="fixed inset-0 bg-black/70 flex items-center justify-center z-50"
        @click.self="closeModal"
      >
        <div class="bg-gray-900 rounded-xl max-w-md w-full mx-4">
          <div class="p-6 border-b border-gray-700">
            <div class="flex items-center justify-between">
              <h2 class="text-xl font-bold">
                {{ isCreating ? 'Add Configuration' : 'Edit Configuration' }}
              </h2>
              <button @click="closeModal" class="text-gray-400 hover:text-white">
                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          <div class="p-6 space-y-4">
            <div v-if="isCreating">
              <label class="block text-sm font-medium text-gray-400 mb-1">
                Notification Type
              </label>
              <select
                v-model="formNotificationType"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2"
              >
                <option v-for="type in unconfiguredTypes" :key="type" :value="type">
                  {{ type.replace('_', ' ') }}
                </option>
              </select>
              <p class="text-xs text-gray-500 mt-1">
                {{ getTypeDescription(formNotificationType) }}
              </p>
            </div>
            <div v-else>
              <label class="block text-sm font-medium text-gray-400 mb-1">
                Notification Type
              </label>
              <div class="font-medium">{{ formNotificationType.replace('_', ' ') }}</div>
            </div>

            <div v-if="isCreating">
              <label class="block text-sm font-medium text-gray-400 mb-1">
                Discord Server ID
              </label>
              <input
                v-model="formDiscordServerId"
                type="text"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2"
                placeholder="e.g., 123456789012345678"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-400 mb-1">
                Channel ID
              </label>
              <input
                v-model="formChannelId"
                type="text"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2"
                placeholder="e.g., 987654321098765432"
              />
              <p class="text-xs text-gray-500 mt-1">
                Right-click channel in Discord &rarr; Copy ID
              </p>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-400 mb-1">
                Mention Role ID (optional)
              </label>
              <input
                v-model="formMentionRoleId"
                type="text"
                class="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2"
                placeholder="e.g., 111222333444555666"
              />
              <p class="text-xs text-gray-500 mt-1">
                Role to mention in notifications. Leave empty for no mention.
              </p>
            </div>

            <div class="flex items-center">
              <input
                v-model="formEnabled"
                type="checkbox"
                id="enabled"
                class="w-4 h-4 text-primary-600 bg-gray-800 border-gray-700 rounded focus:ring-primary-500"
              />
              <label for="enabled" class="ml-2 text-sm text-gray-300">
                Enable notifications
              </label>
            </div>
          </div>

          <div class="p-6 border-t border-gray-700 flex justify-end space-x-4">
            <button
              @click="closeModal"
              class="px-4 py-2 text-gray-400 hover:text-white"
            >
              Cancel
            </button>
            <button
              @click="saveConfig"
              :disabled="upsertMutation.isPending.value || updateMutation.isPending.value || !formChannelId"
              class="bg-primary-600 hover:bg-primary-500 disabled:opacity-50 disabled:cursor-not-allowed px-4 py-2 rounded-lg font-medium"
            >
              {{ isCreating ? 'Create' : 'Save' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
