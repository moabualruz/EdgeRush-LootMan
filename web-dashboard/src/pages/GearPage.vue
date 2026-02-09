<script setup lang="ts">
import { computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { gearApi, type GearSlot, type ItemQuality, type GearItem, type VaultSlot } from '@/api/gear'
import { formatRelativeTime } from '@/utils/date'
import { useAuthStore } from '@/stores/auth'
import { useGuildContextStore } from '@/stores/guildContext'

const authStore = useAuthStore()
const guildContextStore = useGuildContextStore()
const guildId = computed(() => guildContextStore.currentGuildId || authStore.user?.guildId)

// Queries
const { data: gearData, isLoading: gearLoading, error: gearError } = useQuery({
  queryKey: ['myGear', guildId],
  queryFn: () => gearApi.getMyGear(guildId.value!),
  enabled: computed(() => !!guildId.value),
})

const { data: vaultData, isLoading: vaultLoading } = useQuery({
  queryKey: ['myVault', guildId],
  queryFn: () => gearApi.getMyVaultOptions(guildId.value!),
  enabled: computed(() => !!guildId.value),
})

// Slot display order
const slotOrder: GearSlot[] = [
  'HEAD', 'NECK', 'SHOULDER', 'BACK', 'CHEST', 'WRIST',
  'HANDS', 'WAIST', 'LEGS', 'FEET',
  'FINGER_1', 'FINGER_2', 'TRINKET_1', 'TRINKET_2',
  'MAIN_HAND', 'OFF_HAND',
]

const orderedItems = computed(() => {
  if (!gearData.value?.items) return []
  const itemMap = new Map(gearData.value.items.map(item => [item.slot, item]))
  return slotOrder.map(slot => ({
    slot,
    item: itemMap.get(slot),
  }))
})

// Helper functions
function getQualityColor(quality: ItemQuality): string {
  switch (quality) {
    case 'LEGENDARY':
      return 'text-orange-400'
    case 'ARTIFACT':
      return 'text-yellow-300'
    case 'EPIC':
      return 'text-purple-400'
    case 'RARE':
      return 'text-blue-400'
    case 'UNCOMMON':
      return 'text-green-400'
    case 'COMMON':
      return 'text-gray-300'
    default:
      return 'text-gray-500'
  }
}

function getQualityBorder(quality: ItemQuality): string {
  switch (quality) {
    case 'LEGENDARY':
      return 'border-orange-400'
    case 'ARTIFACT':
      return 'border-yellow-300'
    case 'EPIC':
      return 'border-purple-400'
    case 'RARE':
      return 'border-blue-400'
    case 'UNCOMMON':
      return 'border-green-400'
    default:
      return 'border-gray-600'
  }
}

function getSlotName(slot: GearSlot): string {
  return slot.replace(/_/g, ' ').replace(/\d+$/, match => ` ${match}`)
    .split(' ')
    .map(word => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ')
}

function getIlvlColor(ilvl: number): string {
  if (ilvl >= 639) return 'text-purple-400' // Mythic
  if (ilvl >= 626) return 'text-orange-400' // Heroic
  if (ilvl >= 613) return 'text-blue-400' // Normal
  return 'text-gray-400'
}

function hasIssue(item: GearItem | undefined, slot: GearSlot): boolean {
  if (!item) return false
  if (!gearData.value) return false
  return (gearData.value.missingEnchants ?? []).includes(slot) || (gearData.value.missingGems ?? []).includes(slot)
}

function getVaultProgress(slot: VaultSlot): string {
  if (slot.unlocked) return 'Unlocked'
  return `${slot.progress}/${slot.progressRequired}`
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Gear</h1>
      <div v-if="gearData" class="text-sm text-gray-400">
        {{ gearData.characterName }}
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="gearLoading" class="flex items-center justify-center py-12">
      <div class="animate-spin w-8 h-8 border-2 border-primary-500 border-t-transparent rounded-full"></div>
    </div>

    <!-- Error state -->
    <div v-else-if="gearError" class="alert alert-error">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
      <div>
        <h5 class="alert-title">Error Loading Gear</h5>
        <div class="alert-description">Failed to load gear data. Please try again later.</div>
      </div>
    </div>

    <!-- Content -->
    <div v-else-if="gearData" class="space-y-6">
      <!-- Summary Cards -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="card text-center">
          <div :class="['text-3xl font-bold', getIlvlColor(gearData.equippedItemLevel ?? 0)]">
            {{ (gearData.equippedItemLevel ?? 0).toFixed(1) }}
          </div>
          <div class="text-sm text-gray-400 mt-1">Equipped iLvl</div>
        </div>
        <div class="card text-center">
          <div class="text-3xl font-bold text-primary-400">
            {{ (gearData.averageItemLevel ?? 0).toFixed(1) }}
          </div>
          <div class="text-sm text-gray-400 mt-1">Average iLvl</div>
        </div>
        <div class="card text-center">
          <div class="text-3xl font-bold text-purple-400">
            {{ gearData.tierPieceCount ?? 0 }}/5
          </div>
          <div class="text-sm text-gray-400 mt-1">Tier Pieces</div>
        </div>
        <div class="card text-center">
          <div :class="['text-3xl font-bold', (gearData.missingEnchants?.length ?? 0) + (gearData.missingGems?.length ?? 0) > 0 ? 'text-red-400' : 'text-green-400']">
            {{ (gearData.missingEnchants?.length ?? 0) + (gearData.missingGems?.length ?? 0) }}
          </div>
          <div class="text-sm text-gray-400 mt-1">Missing Enchants/Gems</div>
        </div>
      </div>

      <!-- Warnings -->
      <div v-if="(gearData.missingEnchants?.length ?? 0) > 0 || (gearData.missingGems?.length ?? 0) > 0" class="alert alert-warning border-l-yellow-500 bg-yellow-950/20 text-yellow-200 border-y border-r border-yellow-500/20">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-5 w-5 text-yellow-500"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
        <div>
           <h5 class="alert-title text-yellow-500">Equipment Issues</h5>
           <ul class="text-sm mt-1 space-y-1 opacity-90">
              <li v-for="slot in (gearData.missingEnchants ?? [])" :key="`enchant-${slot}`">
                Missing enchant on {{ getSlotName(slot) }}
              </li>
              <li v-for="slot in (gearData.missingGems ?? [])" :key="`gem-${slot}`">
                Missing gem on {{ getSlotName(slot) }}
              </li>
            </ul>
        </div>
      </div>

      <!-- Gear Grid -->
      <div class="card">
        <h2 class="text-lg font-semibold mb-4">Equipped Gear</h2>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div
            v-for="{ slot, item } in orderedItems"
            :key="slot"
            :class="[
              'p-3 rounded-lg border-2 transition-colors',
              item ? getQualityBorder(item.quality) : 'border-gray-700',
              hasIssue(item, slot) ? 'bg-red-900/10' : 'bg-gray-800/30'
            ]"
          >
            <div class="flex items-center justify-between">
              <div class="flex-1 min-w-0">
                <div class="text-xs text-gray-500 uppercase tracking-wide">
                  {{ getSlotName(slot) }}
                </div>
                <div v-if="item" class="mt-1">
                  <div :class="['font-medium truncate', getQualityColor(item.quality)]">
                    {{ item.itemName }}
                    <span v-if="item.isTierPiece" class="ml-1 text-xs text-purple-400">[T]</span>
                  </div>
                  <div class="flex items-center space-x-2 text-xs text-gray-400 mt-0.5">
                    <span v-if="item.enchantName" class="text-green-400">{{ item.enchantName }}</span>
                    <span v-else-if="(gearData.missingEnchants ?? []).includes(slot)" class="text-red-400">No Enchant</span>
                    <span v-if="item.gems.length > 0" class="text-blue-400">
                      {{ item.gems.length }}/{{ item.socketCount }} gems
                    </span>
                    <span v-else-if="item.socketCount > 0 && (gearData.missingGems ?? []).includes(slot)" class="text-red-400">
                      No Gems
                    </span>
                  </div>
                </div>
                <div v-else class="text-gray-500 mt-1">Empty slot</div>
              </div>

              <div v-if="item" class="text-right ml-3">
                <div :class="['text-lg font-bold', getIlvlColor(item.itemLevel)]">
                  {{ item.itemLevel }}
                </div>
                <div v-if="item.upgradeLevel && item.maxUpgradeLevel" class="text-xs text-gray-500">
                  {{ item.upgradeLevel }}/{{ item.maxUpgradeLevel }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Great Vault -->
      <div v-if="vaultData" class="card">
        <h2 class="text-lg font-semibold mb-4">Great Vault Options</h2>
        <div class="text-xs text-gray-500 mb-4">Week of {{ vaultData.weekOf }}</div>

        <div class="space-y-6">
          <!-- Raid Vault -->
          <div>
            <h3 class="text-sm font-medium text-gray-400 mb-3">Raid</h3>
            <div class="grid grid-cols-3 gap-3">
              <div
                v-for="slot in vaultData.raid"
                :key="slot.id"
                :class="[
                  'p-3 rounded-lg border text-center',
                  slot.unlocked ? 'border-purple-500 bg-purple-900/20' : 'border-gray-700 bg-gray-800/30'
                ]"
              >
                <div v-if="slot.unlocked && slot.itemName">
                  <div class="text-purple-400 font-medium truncate">{{ slot.itemName }}</div>
                  <div class="text-lg font-bold">{{ slot.itemLevel }}</div>
                </div>
                <div v-else-if="slot.unlocked">
                  <div class="text-green-400">Available</div>
                  <div class="text-lg font-bold">???</div>
                </div>
                <div v-else>
                  <div class="text-gray-500">Locked</div>
                  <div class="text-sm text-gray-600">{{ getVaultProgress(slot) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- M+ Vault -->
          <div>
            <h3 class="text-sm font-medium text-gray-400 mb-3">Mythic+</h3>
            <div class="grid grid-cols-3 gap-3">
              <div
                v-for="slot in vaultData.mythicPlus"
                :key="slot.id"
                :class="[
                  'p-3 rounded-lg border text-center',
                  slot.unlocked ? 'border-orange-500 bg-orange-900/20' : 'border-gray-700 bg-gray-800/30'
                ]"
              >
                <div v-if="slot.unlocked && slot.itemName">
                  <div class="text-orange-400 font-medium truncate">{{ slot.itemName }}</div>
                  <div class="text-lg font-bold">{{ slot.itemLevel }}</div>
                </div>
                <div v-else-if="slot.unlocked">
                  <div class="text-green-400">Available</div>
                  <div class="text-lg font-bold">???</div>
                </div>
                <div v-else>
                  <div class="text-gray-500">Locked</div>
                  <div class="text-sm text-gray-600">{{ getVaultProgress(slot) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- PvP Vault -->
          <div>
            <h3 class="text-sm font-medium text-gray-400 mb-3">PvP</h3>
            <div class="grid grid-cols-3 gap-3">
              <div
                v-for="slot in vaultData.pvp"
                :key="slot.id"
                :class="[
                  'p-3 rounded-lg border text-center',
                  slot.unlocked ? 'border-red-500 bg-red-900/20' : 'border-gray-700 bg-gray-800/30'
                ]"
              >
                <div v-if="slot.unlocked && slot.itemName">
                  <div class="text-red-400 font-medium truncate">{{ slot.itemName }}</div>
                  <div class="text-lg font-bold">{{ slot.itemLevel }}</div>
                </div>
                <div v-else-if="slot.unlocked">
                  <div class="text-green-400">Available</div>
                  <div class="text-lg font-bold">???</div>
                </div>
                <div v-else>
                  <div class="text-gray-500">Locked</div>
                  <div class="text-sm text-gray-600">{{ getVaultProgress(slot) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Last Updated -->
      <div class="text-sm text-gray-500 text-center">
        Last updated: {{ formatRelativeTime(gearData.lastUpdated) }}
      </div>
    </div>
  </div>
</template>
