<script setup lang="ts">
import { ref, watch } from 'vue'
import { lootApi } from '@/api/loot'
import type { WowItem } from '@/types'

const props = defineProps<{
  modelValue: WowItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [item: WowItem | null]
}>()

const query = ref('')
const results = ref<WowItem[]>([])
const isOpen = ref(false)
const isLoading = ref(false)
const selectedIndex = ref(0)

let debounceTimer: ReturnType<typeof setTimeout> | null = null

async function searchItems() {
  if (query.value.length < 2) {
    results.value = []
    return
  }
  isLoading.value = true
  try {
    results.value = await lootApi.searchItems(query.value)
    selectedIndex.value = 0
  } finally {
    isLoading.value = false
  }
}

function debouncedSearch() {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(searchItems, 300)
}

watch(query, () => {
  debouncedSearch()
  isOpen.value = true
})

function selectItem(item: WowItem) {
  emit('update:modelValue', item)
  query.value = item.name
  isOpen.value = false
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    selectedIndex.value = Math.min(selectedIndex.value + 1, results.value.length - 1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    selectedIndex.value = Math.max(selectedIndex.value - 1, 0)
  } else if (e.key === 'Enter' && results.value[selectedIndex.value]) {
    e.preventDefault()
    selectItem(results.value[selectedIndex.value])
  } else if (e.key === 'Escape') {
    isOpen.value = false
  }
}

function handleBlur() {
  // Delay to allow click events on dropdown items
  setTimeout(() => {
    isOpen.value = false
  }, 200)
}

const qualityColors: Record<WowItem['quality'], string> = {
  POOR: 'text-gray-400',
  COMMON: 'text-white',
  UNCOMMON: 'text-green-400',
  RARE: 'text-blue-400',
  EPIC: 'text-purple-400',
  LEGENDARY: 'text-orange-400',
}
</script>

<template>
  <div class="relative">
    <input
      v-model="query"
      type="text"
      placeholder="Search for an item..."
      class="input w-full"
      @keydown="handleKeydown"
      @blur="handleBlur"
      @focus="isOpen = results.length > 0"
    />

    <!-- Loading indicator -->
    <div
      v-if="isLoading"
      class="absolute right-3 top-1/2 -translate-y-1/2"
    >
      <svg class="animate-spin h-4 w-4 text-gray-400" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
      </svg>
    </div>

    <!-- Dropdown results -->
    <div
      v-if="isOpen && results.length > 0"
      class="absolute z-50 mt-1 w-full bg-gray-800 border border-gray-700 rounded-lg shadow-xl max-h-60 overflow-auto"
    >
      <button
        v-for="(item, index) in results"
        :key="item.id"
        type="button"
        :class="[
          'w-full px-4 py-2 text-left flex items-center gap-3 hover:bg-gray-700 transition-colors',
          index === selectedIndex ? 'bg-gray-700' : '',
        ]"
        @mousedown.prevent="selectItem(item)"
      >
        <img
          v-if="item.iconUrl"
          :src="item.iconUrl"
          :alt="item.name"
          class="w-6 h-6 rounded"
        />
        <span :class="qualityColors[item.quality]">{{ item.name }}</span>
      </button>
    </div>

    <!-- No results -->
    <div
      v-if="isOpen && query.length >= 2 && !isLoading && results.length === 0"
      class="absolute z-50 mt-1 w-full bg-gray-800 border border-gray-700 rounded-lg shadow-xl p-4 text-center text-gray-400"
    >
      No items found
    </div>
  </div>
</template>
