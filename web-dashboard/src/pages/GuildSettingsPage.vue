<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { useGuildContextStore } from '@/stores/guildContext'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseSelect from '@/components/ui/BaseSelect.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import ConfigEditor from '@/components/admin/ConfigEditor.vue'
import {
  fetchGuildPermissions,
  addGuildPermission,
  removeGuildPermission,
  fetchPermissionTypes,
} from '@/api/guildContext'
import {
  fetchGuildSyncConfig,
  updateGuildSyncConfig,
  triggerBnetSync,
  triggerWowauditSync,
  type GuildSyncConfig,
} from '@/api/guildSync'
import type { GuildPermission, PermissionTypeInfo } from '@/types'

const router = useRouter()
const guildContextStore = useGuildContextStore()
const authStore = useAuthStore()
const queryClient = useQueryClient()
const toast = useToast()

const activeTab = ref<'sync' | 'flps' | 'permissions'>('sync')

const tabs = [
  { key: 'sync', label: 'Data Sync' },
  { key: 'flps', label: 'FLPS Configuration' },
  { key: 'permissions', label: 'Permissions' },
] as const

const guildId = computed(() => guildContextStore.currentGuildId ?? '')
const guildName = computed(() => guildContextStore.activeGuild?.guildName ?? 'Guild')

// Check permission on mount
onMounted(() => {
  if (!guildContextStore.canAccessSettings) {
    router.push('/dashboard')
  }
})

// Sync config query
const { data: syncConfig, isLoading: syncConfigLoading, refetch: refetchSyncConfig } = useQuery({
  queryKey: ['guildSyncConfig', guildId],
  queryFn: () => fetchGuildSyncConfig(guildId.value),
  enabled: computed(() => !!guildId.value && authStore.isAuthenticated),
})

// Sync config form state
const wowauditApiKey = ref('')
const wowauditGuildUri = ref('eu/twisting-nether/dod')
const bnetRealmSlug = ref('twisting-nether')
const bnetGuildNameSlug = ref('dod')
const bnetRegion = ref('eu')
const syncSaving = ref(false)
const bnetSyncing = ref(false)
const wowauditSyncing = ref(false)

// Populate form when sync config loads
watch(syncConfig, (config) => {
  if (config) {
    wowauditGuildUri.value = config.wowauditGuildUri ?? 'eu/twisting-nether/dod'
    bnetRealmSlug.value = config.bnetRealmSlug ?? 'twisting-nether'
    bnetGuildNameSlug.value = config.bnetGuildNameSlug ?? 'dod'
    bnetRegion.value = config.bnetRegion ?? 'eu'
  }
})

// Permissions queries
const { data: permissions, isLoading: permissionsLoading } = useQuery({
  queryKey: ['guildPermissions', guildId],
  queryFn: () => fetchGuildPermissions(guildId.value),
  enabled: computed(() => !!guildId.value && authStore.isAuthenticated),
})

const { data: permissionTypes } = useQuery({
  queryKey: ['permissionTypes'],
  queryFn: fetchPermissionTypes,
})

// Add permission form
const newPermissionRank = ref('')
const newPermissionType = ref('')

const addPermissionMutation = useMutation({
  mutationFn: (params: { rankName: string; permissionType: string }) =>
    addGuildPermission(guildId.value, params.rankName, params.permissionType),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['guildPermissions', guildId.value] })
    newPermissionRank.value = ''
    newPermissionType.value = ''
  },
})

const removePermissionMutation = useMutation({
  mutationFn: (permissionId: number) => removeGuildPermission(guildId.value, permissionId),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ['guildPermissions', guildId.value] })
  },
})

function handleAddPermission() {
  if (newPermissionRank.value && newPermissionType.value) {
    addPermissionMutation.mutate({
      rankName: newPermissionRank.value,
      permissionType: newPermissionType.value,
    })
  }
}

function handleRemovePermission(permission: GuildPermission) {
  removePermissionMutation.mutate(permission.id)
}

async function saveSyncConfig() {
  syncSaving.value = true
  try {
    await updateGuildSyncConfig(guildId.value, {
      wowauditGuildUri: wowauditGuildUri.value || undefined,
      wowauditApiKey: wowauditApiKey.value || undefined,
      bnetRealmSlug: bnetRealmSlug.value || undefined,
      bnetGuildNameSlug: bnetGuildNameSlug.value || undefined,
      bnetRegion: bnetRegion.value || undefined,
      // Automatically enable sync when saving configuration
      syncEnabled: true,
      bnetSyncEnabled: true,
    })
    wowauditApiKey.value = '' // Clear the API key field after save
    toast.success('Settings Saved', 'Configuration saved successfully!')
    refetchSyncConfig()
  } catch (error) {
    toast.error('Save Failed', 'Failed to save configuration. Please try again.')
  } finally {
    syncSaving.value = false
  }
}

async function handleBnetSync() {
  bnetSyncing.value = true
  try {
    const result = await triggerBnetSync(guildId.value)
    if (result.success) {
      toast.success('Sync Started', result.message)
      // Refresh the guild context to pick up new raiders
      guildContextStore.fetchGuilds()
    } else {
      toast.error('Sync Failed', result.message)
    }
    refetchSyncConfig()
  } catch (error) {
    toast.error('Sync Error', 'Failed to trigger sync. Please try again.')
  } finally {
    bnetSyncing.value = false
  }
}

async function handleWowauditSync() {
  wowauditSyncing.value = true
  try {
    const result = await triggerWowauditSync(guildId.value)
    if (result.success) {
      toast.success('Sync Started', result.message)
      // Refresh the guild context to pick up new raiders
      guildContextStore.fetchGuilds()
    } else {
      toast.error('Sync Failed', result.message)
    }
    refetchSyncConfig()
  } catch (error) {
    toast.error('Sync Error', 'Failed to trigger WoWAudit sync. Please try again.')
  } finally {
    wowauditSyncing.value = false
  }
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return 'Never'
  return new Date(dateStr).toLocaleString()
}

// Group permissions by rank for display
const permissionsByRank = computed(() => {
  if (!permissions.value) return new Map<string, GuildPermission[]>()

  const grouped = new Map<string, GuildPermission[]>()
  for (const permission of permissions.value) {
    const existing = grouped.get(permission.rankName) || []
    existing.push(permission)
    grouped.set(permission.rankName, existing)
  }
  return grouped
})

function getPermissionDescription(type: string): string {
  const typeInfo = permissionTypes.value?.find((t) => t.name === type)
  return typeInfo?.description ?? type
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-2">Guild Settings</h1>
    <p class="text-gray-400 mb-6">{{ guildName }}</p>

    <!-- Tab navigation -->
    <div class="border-b border-gray-700 mb-6">
      <nav class="flex space-x-4">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-4 py-2 border-b-2 font-medium text-sm transition-colors',
            activeTab === tab.key
              ? 'border-primary-500 text-primary-400'
              : 'border-transparent text-gray-400 hover:text-gray-300 hover:border-gray-600',
          ]"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- Tab content -->
    <div>
      <!-- Data Sync Configuration -->
      <div v-if="activeTab === 'sync'" class="space-y-6">
        <!-- Message display (REMOVED) -->

        <!-- Loading state -->
        <div v-if="syncConfigLoading" class="flex items-center justify-center py-8">
          <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
        </div>

        <template v-else>
          <!-- Battle.net Guild Roster Sync -->
          <div class="card">
            <h3 class="text-lg font-semibold mb-4">Battle.net Guild Roster</h3>
            <p class="text-sm text-gray-400 mb-4">
              Sync your complete guild roster directly from Battle.net. This will import all guild members with their ranks.
            </p>

            <div class="space-y-4">
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <BaseSelect
                  v-model="bnetRegion"
                  label="Region"
                  :options="[
                    { value: 'eu', label: 'Europe (EU)' },
                    { value: 'us', label: 'Americas (US)' },
                    { value: 'kr', label: 'Korea (KR)' },
                    { value: 'tw', label: 'Taiwan (TW)' }
                  ]"
                />

                <BaseInput
                  v-model="bnetRealmSlug"
                  label="Realm Slug"
                  placeholder="e.g., twisting-nether"
                  hint="Lowercase, hyphens for spaces"
                />

                <BaseInput
                  v-model="bnetGuildNameSlug"
                  label="Guild Name Slug"
                  placeholder="e.g., dod"
                  hint="Lowercase, hyphens for spaces"
                />
              </div>

              <!-- Sync status -->
              <div v-if="syncConfig" class="p-4 bg-gray-700/50 rounded-lg">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                  <div>
                    <p class="text-gray-400">Last Sync</p>
                    <p class="font-medium">{{ formatDate(syncConfig.bnetLastSyncAt) }}</p>
                  </div>
                  <div>
                    <p class="text-gray-400">Status</p>
                    <p :class="[
                      'font-medium',
                      syncConfig.bnetLastSyncStatus === 'SUCCESS' ? 'text-green-400' :
                      syncConfig.bnetLastSyncStatus === 'FAILED' ? 'text-red-400' :
                      syncConfig.bnetLastSyncStatus === 'IN_PROGRESS' ? 'text-yellow-400' : 'text-gray-400'
                    ]">
                      {{ syncConfig.bnetLastSyncStatus ?? 'Never synced' }}
                    </p>
                  </div>
                  <div v-if="syncConfig.bnetLastSyncError" class="col-span-2">
                    <p class="text-gray-400">Error</p>
                    <p class="text-red-400 text-xs">{{ syncConfig.bnetLastSyncError }}</p>
                  </div>
                </div>
              </div>

              <div class="flex justify-end gap-4">
                <BaseButton
                  variant="secondary"
                  @click="handleBnetSync"
                  :loading="bnetSyncing"
                  :disabled="!bnetRealmSlug || !bnetGuildNameSlug"
                >
                  {{ bnetSyncing ? 'Syncing...' : 'Sync Now' }}
                </BaseButton>
              </div>
            </div>
          </div>

          <!-- WoWAudit Configuration -->
          <div class="card">
            <h3 class="text-lg font-semibold mb-4">WoWAudit Integration</h3>
            <p class="text-sm text-gray-400 mb-4">
              Configure WoWAudit to sync attendance, loot history, and wishlist data.
            </p>

            <div class="space-y-4">
              <div>
                <BaseInput
                  v-model="wowauditApiKey"
                  type="password"
                  label="API Key"
                  placeholder="Enter your WoWAudit API key (leave blank to keep existing)"
                />
                <p class="text-xs text-gray-500 mt-1 ml-1">
                  {{ syncConfig?.wowauditApiKeyConfigured ? 'API key is configured.' : 'No API key configured.' }}
                </p>
              </div>

              <div>
                <BaseInput
                  v-model="wowauditGuildUri"
                  label="Guild URI"
                  placeholder="e.g., eu/tarren-mill/your-guild-name"
                />
                <p class="text-xs text-gray-500 mt-1 ml-1">
                  Format: region/realm/guild-name (all lowercase, hyphens for spaces)
                </p>
              </div>

              <!-- WoWAudit sync status -->
              <div v-if="syncConfig" class="p-4 bg-gray-700/50 rounded-lg">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                  <div>
                    <p class="text-gray-400">Last Sync</p>
                    <p class="font-medium">{{ formatDate(syncConfig.lastSyncAt) }}</p>
                  </div>
                  <div>
                    <p class="text-gray-400">Status</p>
                    <p :class="[
                      'font-medium',
                      syncConfig.lastSyncStatus === 'SUCCESS' ? 'text-green-400' :
                      syncConfig.lastSyncStatus === 'FAILED' ? 'text-red-400' :
                      syncConfig.lastSyncStatus === 'IN_PROGRESS' ? 'text-yellow-400' : 'text-gray-400'
                    ]">
                      {{ syncConfig.lastSyncStatus ?? 'Never synced' }}
                    </p>
                  </div>
                  <div v-if="syncConfig.lastSyncError" class="col-span-2">
                    <p class="text-gray-400">Error</p>
                    <p class="text-red-400 text-xs">{{ syncConfig.lastSyncError }}</p>
                  </div>
                </div>
              </div>

              <div class="flex justify-end gap-4">
                <BaseButton
                  variant="secondary"
                  @click="handleWowauditSync"
                  :loading="wowauditSyncing"
                  :disabled="!wowauditGuildUri || !syncConfig?.wowauditApiKeyConfigured"
                >
                  {{ wowauditSyncing ? 'Syncing...' : 'Sync Now' }}
                </BaseButton>
              </div>
            </div>
          </div>

          <!-- Save button -->
          <div class="flex justify-end">
            <BaseButton
              @click="saveSyncConfig"
              :loading="syncSaving"
            >
              {{ syncSaving ? 'Saving...' : 'Save All Settings' }}
            </BaseButton>
          </div>
        </template>
      </div>

      <!-- FLPS Configuration -->
      <ConfigEditor v-else-if="activeTab === 'flps'" :guild-id="guildId" />

      <!-- Permissions Configuration -->
      <div v-else-if="activeTab === 'permissions'" class="space-y-6">
        <div class="card">
          <h3 class="text-lg font-semibold mb-4">Access Permissions</h3>
          <p class="text-sm text-gray-400 mb-4">
            Configure which ranks have access to various features. Permissions are based on WoWAudit rank data.
          </p>

          <!-- Add new permission -->
          <div class="flex flex-col md:flex-row gap-4 p-4 bg-gray-700/50 rounded-lg mb-6 items-end">
             <div class="flex-1 w-full">
              <BaseInput
                v-model="newPermissionRank"
                label="Rank Name"
                placeholder="e.g., Guild Master, Officer, Raider"
              />
            </div>
            <div class="flex-1 w-full">
              <BaseSelect
                v-model="newPermissionType"
                label="Permission Type"
                placeholder="Select permission..."
              >
                <option v-for="type in permissionTypes" :key="type.name" :value="type.name">
                  {{ type.name }} - {{ type.description }}
                </option>
              </BaseSelect>
            </div>
            <div class="flex-none">
              <BaseButton
                @click="handleAddPermission"
                :loading="addPermissionMutation.isPending.value"
                :disabled="!newPermissionRank || !newPermissionType"
              >
                Add Permission
              </BaseButton>
            </div>
          </div>

          <!-- Loading state -->
          <div v-if="permissionsLoading" class="flex items-center justify-center py-8">
            <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
          </div>

          <!-- Permissions list -->
          <div v-else-if="permissionsByRank.size > 0" class="space-y-4">
            <div
              v-for="[rankName, rankPermissions] in permissionsByRank"
              :key="rankName"
              class="border border-gray-700 rounded-lg overflow-hidden"
            >
              <div class="bg-gray-700/50 px-4 py-2 font-medium">
                {{ rankName }}
              </div>
              <div class="divide-y divide-gray-700">
                <div
                  v-for="permission in rankPermissions"
                  :key="permission.id"
                  class="flex items-center justify-between px-4 py-3"
                >
                  <div>
                    <p class="font-medium">{{ permission.permissionType }}</p>
                    <p class="text-sm text-gray-400">{{ getPermissionDescription(permission.permissionType) }}</p>
                  </div>
                  <button
                    @click="handleRemovePermission(permission)"
                    class="text-red-400 hover:text-red-300 text-sm"
                    :disabled="removePermissionMutation.isPending.value"
                  >
                    Remove
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty state -->
          <div v-else class="text-center py-8 text-gray-400">
            <p>No permissions configured yet.</p>
            <p class="text-sm mt-1">Add permissions above to control access to guild features.</p>
          </div>
        </div>

        <!-- Permission type descriptions -->
        <div class="card">
          <h4 class="font-semibold mb-4">Available Permission Types</h4>
          <div class="space-y-3">
            <div v-for="type in permissionTypes" :key="type.name" class="flex items-start">
              <span class="font-mono text-sm text-primary-400 w-48 flex-shrink-0">{{ type.name }}</span>
              <span class="text-gray-400 text-sm">{{ type.description }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
