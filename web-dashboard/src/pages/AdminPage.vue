<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { flpsApi } from '@/api/flps'
import { adminApi } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'
import { useGuildContextStore } from '@/stores/guildContext'
import ConfigEditor from '@/components/admin/ConfigEditor.vue'
import BehavioralActionsPanel from '@/components/admin/BehavioralActionsPanel.vue'
import LootBansPanel from '@/components/admin/LootBansPanel.vue'

const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()
const guildId = computed(() => guildContextStore.currentGuildId || authStore.user?.guildId)

const activeTab = ref<'config' | 'actions' | 'bans'>('config')

const tabs = [
  { key: 'config', label: 'FLPS Configuration' },
  { key: 'actions', label: 'Behavioral Actions' },
  { key: 'bans', label: 'Loot Bans' },
] as const
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">Admin Panel</h1>

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
              : 'border-transparent text-gray-400 hover:text-gray-300 hover:border-gray-600'
          ]"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- Tab content -->
    <div v-if="guildId">
      <ConfigEditor v-if="activeTab === 'config'" :guild-id="guildId" />
      <BehavioralActionsPanel v-else-if="activeTab === 'actions'" :guild-id="guildId" />
      <LootBansPanel v-else-if="activeTab === 'bans'" :guild-id="guildId" />
    </div>
    <div v-else class="text-center py-8 text-muted-foreground">
      Please select a guild to view admin settings.
    </div>
  </div>
</template>
