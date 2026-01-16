<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
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
import type { GuildPermission, PermissionTypeInfo } from '@/types'

const router = useRouter()
const guildContextStore = useGuildContextStore()
const queryClient = useQueryClient()

const activeTab = ref<'wowaudit' | 'flps' | 'permissions'>('wowaudit')

const tabs = [
  { key: 'wowaudit', label: 'WoWAudit Integration' },
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

// WoWAudit config state
const wowauditApiKey = ref('')
const wowauditGuildUri = ref('')
const wowauditSaving = ref(false)

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

async function saveWowauditConfig() {
  wowauditSaving.value = true
  // TODO: Implement WoWAudit config save API
  await new Promise((resolve) => setTimeout(resolve, 1000))
  wowauditSaving.value = false
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
      <!-- WoWAudit Configuration -->
      <div v-if="activeTab === 'wowaudit'" class="space-y-6">
        <div class="card">
          <h3 class="text-lg font-semibold mb-4">WoWAudit API Configuration</h3>
          <p class="text-sm text-gray-400 mb-4">
            Configure your WoWAudit integration to sync character, attendance, and loot data.
          </p>

          <div class="space-y-4">
            <div>
              <label class="label">API Key</label>
              <input
                v-model="wowauditApiKey"
                type="password"
                placeholder="Enter your WoWAudit API key"
                class="input"
              />
              <p class="text-xs text-gray-500 mt-1">
                You can find your API key in the WoWAudit dashboard under Settings.
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
                The URI format is: region/realm/guild-name (all lowercase, hyphens for spaces)
              </p>
            </div>

            <div class="flex justify-end">
              <button
                @click="saveWowauditConfig"
                class="btn-primary"
                :disabled="wowauditSaving"
              >
                {{ wowauditSaving ? 'Saving...' : 'Save Configuration' }}
              </button>
            </div>
          </div>
        </div>

        <div class="card bg-blue-900/20 border-blue-700">
          <h4 class="font-semibold text-blue-400 mb-2">Sync Status</h4>
          <p class="text-sm text-gray-300">
            WoWAudit sync runs automatically every hour. You can also trigger a manual sync from the
            <RouterLink to="/admin/sync" class="text-primary-400 hover:underline">Sync History</RouterLink>
            page.
          </p>
        </div>
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
