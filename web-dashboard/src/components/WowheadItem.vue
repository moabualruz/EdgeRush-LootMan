<script setup lang="ts">
/**
 * WowheadItem - Displays an item with Wowhead tooltip integration.
 *
 * Renders an item link that shows Wowhead's tooltip on hover, including:
 * - Item icon (optional)
 * - Item name with quality color
 * - Full tooltip with stats, source, etc.
 */
import { computed } from 'vue'
import { getWowheadItemUrl } from '@/composables/useWowhead'

export interface WowheadItemProps {
  itemId: number
  itemName: string
  quality?: ItemQuality
  bonusIds?: number[]
  showIcon?: boolean
  iconSize?: 'tiny' | 'small' | 'medium' | 'large'
}

export type ItemQuality =
  | 'poor'
  | 'common'
  | 'uncommon'
  | 'rare'
  | 'epic'
  | 'legendary'
  | 'artifact'
  | 'heirloom'

const props = withDefaults(defineProps<WowheadItemProps>(), {
  quality: 'epic',
  showIcon: true,
  iconSize: 'small',
})

const itemUrl = computed(() => getWowheadItemUrl(props.itemId, props.bonusIds))

// Quality color classes matching WoW item quality colors
const qualityClasses = computed(() => {
  const colors: Record<ItemQuality, string> = {
    poor: 'text-gray-500',
    common: 'text-white',
    uncommon: 'text-green-400',
    rare: 'text-blue-400',
    epic: 'text-purple-400',
    legendary: 'text-orange-400',
    artifact: 'text-yellow-300',
    heirloom: 'text-cyan-300',
  }
  return colors[props.quality]
})

// Data attributes for Wowhead script
const wowheadData = computed(() => {
  const data: Record<string, string> = {
    'data-wowhead': `item=${props.itemId}`,
  }
  if (props.bonusIds && props.bonusIds.length > 0) {
    data['data-wowhead'] += `&bonus=${props.bonusIds.join(':')}`
  }
  return data
})
</script>

<template>
  <a
    :href="itemUrl"
    target="_blank"
    rel="noopener noreferrer"
    class="wowhead-item inline-flex items-center gap-1 hover:underline"
    :class="qualityClasses"
    v-bind="wowheadData"
  >
    <span class="font-medium">{{ itemName }}</span>
  </a>
</template>

<style scoped>
.wowhead-item {
  text-decoration: none;
  transition: filter 0.15s ease;
}

.wowhead-item:hover {
  filter: brightness(1.2);
}
</style>
