<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { useGuildContextStore } from '@/stores/guildContext'
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
  type GuildSyncConfig,
} from '@/api/guildSync'
import type { GuildPermission, PermissionTypeInfo } from '@/types'

const router = useRouter()
const guildContextStore = useGuildContextStore()
const queryClient = useQueryClient()

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
  enabled: computed(() => !!guildId.value),
})

// Sync config form state
const wowauditApiKey = ref('')
const wowauditGuildUri = ref('')
const bnetRealmSlug = ref('')
const bnetGuildNameSlug = ref('')
const bnetRegion = ref('eu')
const syncSaving = ref(false)
const bnetSyncing = ref(false)
const syncMessage = ref<{ type: 'success' | 'error'; text: string } | null>(null)

// Populate form when sync config loads
watch(syncConfig, (config) => {
  if (config) {
    wowauditGuildUri.value = config.wowauditGuildUri ?? ''
    bnetRealmSlug.value = config.bnetRealmSlug ?? ''
    bnetGuildNameSlug.value = config.bnetGuildNameSlug ?? ''
    bnetRegion.value = config.bnetRegion ?? 'eu'
  }
})

// Permissions queries
const { data: permissions, isLoading: permissionsLoading } = useQuery({
  queryKey: ['guildPermissions', guildId],
  queryFn: () => fetchGuildPermissions(guildId.value),
  enabled: computed(() => !!guildId.value),
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
  syncMessage.value = null
  try {
    await updateGuildSyncConfig(guildId.value, {
      wowauditGuildUri: wowauditGuildUri.value || undefined,
      wowauditApiKey: wowauditApiKey.value || undefined,
      bnetRealmSlug: bnetRealmSlug.value || undefined,
      bnetGuildNameSlug: bnetGuildNameSlug.value || undefined,
      bnetRegion: bnetRegion.value || undefined,
    })
    wowauditApiKey.value = '' // Clear the API key field after save
    syncMessage.value = { type: 'success', text: 'Configuration saved successfully!' }
    refetchSyncConfig()
  } catch (error) {
    syncMessage.value = { type: 'error', text: 'Failed to save configuration. Please try again.' }
  } finally {
    syncSaving.value = false
  }
}

async function handleBnetSync() {
  bnetSyncing.value = true
  syncMessage.value = null
  try {
    const result = await triggerBnetSync(guildId.value)
    if (result.success) {
      syncMessage.value = { type: 'success', text: result.message }
      // Refresh the guild context to pick up new raiders
      guildContextStore.fetchGuilds()
    } else {
      syncMessage.value = { type: 'error', text: result.message }
    }
    refetchSyncConfig()
  } catch (error) {
    syncMessage.value = { type: 'error', text: 'Failed to trigger sync. Please try again.' }
  } finally {
    bnetSyncing.value = false
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
        <!-- Message display -->
        <div
          v-if="syncMessage"
          :class="[
            'p-4 rounded-lg',
            syncMessage.type === 'success' ? 'bg-green-900/30 border border-green-700 text-green-300' : 'bg-red-900/30 border border-red-700 text-red-300'
          ]"
        >
          {{ syncMessage.text }}
        </div>

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
                <div>
                  <label class="label">Region</label>
                  <select v-model="bnetRegion" class="input">
                    <option value="eu">Europe (EU)</option>
                    <option value="us">Americas (US)</option>
                    <option value="kr">Korea (KR)</option>
                    <option value="tw">Taiwan (TW)</option>
                  </select>
                </div>

                <div>
                  <label class="label">Realm Slug</label>
                  <input
                    v-model="bnetRealmSlug"
                    type="text"
                    placeholder="e.g., twisting-nether"
                    class="input"
                  />
                  <p class="text-xs text-gray-500 mt-1">
                    Lowercase, hyphens for spaces
                  </p>
                </div>

                <div>
                  <label class="label">Guild Name Slug</label>
                  <input
                    v-model="bnetGuildNameSlug"
                    type="text"
                    placeholder="e.g., dod"
                    class="input"
                  />
                  <p class="text-xs text-gray-500 mt-1">
                    Lowercase, hyphens for spaces
                  </p>
                </div>
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
                <button
                  @click="handleBnetSync"
                  class="btn-secondary"
                  :disabled="bnetSyncing || !bnetRealmSlug || !bnetGuildNameSlug"
                >
                  {{ bnetSyncing ? 'Syncing...' : 'Sync Now' }}
                </button>
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
                <label class="label">API Key</label>
                <input
                  v-model="wowauditApiKey"
                  type="password"
                  placeholder="Enter your WoWAudit API key (leave blank to keep existing)"
                  class="input"
                />
                <p class="text-xs text-gray-500 mt-1">
                  {{ syncConfig?.wowauditApiKeyConfigured ? 'API key is configured.' : 'No API key configured.' }}
                </p>
              </div>

              <div>
                <label class="label">Guild URI</label>
                <input
                  v-model="wowauditGuildUri"
                  type="text"
                  placeholder="e.g., eu/tarren-mill/your-guild-name"
                  class="input"
                />
                <p class="text-xs text-gray-500 mt-1">
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
            </div>
          </div>

          <!-- Save button -->
          <div class="flex justify-end">
            <button
              @click="saveSyncConfig"
              class="btn-primary"
              :disabled="syncSaving"
            >
              {{ syncSaving ? 'Saving...' : 'Save All Settings' }}
            </button>
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
          <div class="flex flex-col md:flex-row gap-4 p-4 bg-gray-700/50 rounded-lg mb-6">
            <div class="flex-1">
              <label class="label">Rank Name</label>
              <input
                v-model="newPermissionRank"
                type="text"
                placeholder="e.g., Guild Master, Officer, Raider"
                class="input"
              />
            </div>
            <div class="flex-1">
              <label class="label">Permission Type</label>
              <select v-model="newPermissionType" class="input">
                <option value="">Select permission...</option>
                <option v-for="type in permissionTypes" :key="type.name" :value="type.name">
                  {{ type.name }} - {{ type.description }}
                </option>
              </select>
            </div>
            <div class="flex items-end">
              <button
                @click="handleAddPermission"
                class="btn-primary"
                :disabled="!newPermissionRank || !newPermissionType || addPermissionMutation.isPending.value"
              >
                {{ addPermissionMutation.isPending.value ? 'Adding...' : 'Add Permission' }}
              </button>
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
