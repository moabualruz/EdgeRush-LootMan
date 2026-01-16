<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { gameDataApi, type BlizzardRaid, type BlizzardMap } from '@/api/gameData'

const props = defineProps<{
  initialEncounterId?: number
}>()

const emit = defineEmits<{
  'select': [encounterId: number, encounterName: string]
}>()

const raids = ref<BlizzardRaid[]>([])
const maps = ref<BlizzardMap[]>([])
const selectedRaidId = ref<number | null>(null)
const selectedMapId = ref<number | null>(props.initialEncounterId ?? null)
const loadingRaids = ref(false)
const loadingMaps = ref(false)

onMounted(async () => {
  loadingRaids.value = true
  try {
    raids.value = await gameDataApi.getRaids()
    // Auto-select first raid if available
    if (raids.value.length > 0) {
        selectedRaidId.value = raids.value[0].id
    }
  } catch (e) {
    console.error("Failed to load raids", e)
  } finally {
    loadingRaids.value = false
  }
})

watch(selectedRaidId, async (newId) => {
  if (!newId) return
  loadingMaps.value = true
  maps.value = []
  try {
    maps.value = await gameDataApi.getRaidMaps(newId)
    // If initialEncounterId is set, try to find it in this list to set selectedMapId
    if (props.initialEncounterId && maps.value.find(m => m.id === props.initialEncounterId)) {
        selectedMapId.value = props.initialEncounterId
    } else {
         selectedMapId.value = null
    }
  } catch (e) {
    console.error("Failed to load maps", e)
  } finally {
    loadingMaps.value = false
  }
})

function handleMapSelect(mapId: number) {
    selectedMapId.value = mapId
    const map = maps.value.find(m => m.id === mapId)
    if (map) {
        emit('select', map.id, map.name)
    }
}
</script>

<template>
  <div class="space-y-4">
    <div>
      <label class="block text-sm font-medium text-gray-400 mb-1">Raid Instance</label>
      <select 
        v-model="selectedRaidId" 
        class="w-full bg-black/40 border border-white/10 rounded px-3 py-2 text-white focus:outline-none focus:border-primary"
        :disabled="loadingRaids"
      >
        <option v-if="loadingRaids" :value="null">Loading...</option>
        <option v-for="raid in raids" :key="raid.id" :value="raid.id">{{ raid.name }}</option>
      </select>
    </div>

    <div>
      <label class="block text-sm font-medium text-gray-400 mb-1">Encounter / Map</label>
      <select 
        v-model="selectedMapId"
        @change="handleMapSelect(($event.target as HTMLSelectElement).value ? Number(($event.target as HTMLSelectElement).value) : 0)"
        class="w-full bg-black/40 border border-white/10 rounded px-3 py-2 text-white focus:outline-none focus:border-primary"
        :disabled="loadingMaps || !selectedRaidId"
      >
        <option v-if="loadingMaps" :value="null">Loading...</option>
        <option :value="null" disabled>Select an encounter</option>
        <option v-for="map in maps" :key="map.id" :value="map.id">{{ map.name }}</option>
      </select>
    </div>
  </div>
</template>
