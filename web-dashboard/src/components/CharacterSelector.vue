<script setup lang="ts">
import { ref, computed } from 'vue'
import { useGuildContextStore } from '@/stores/guildContext'

const guildContextStore = useGuildContextStore()

const isOpen = ref(false)

const currentCharacter = computed(() => {
  if (!guildContextStore.activeGuild) return null
  return {
    name: guildContextStore.activeGuild.characterName,
    realm: guildContextStore.activeGuild.characterRealm,
    guildName: guildContextStore.activeGuild.guildName,
    characterClass: guildContextStore.activeGuild.characterClass,
  }
})

function toggleDropdown() {
  isOpen.value = !isOpen.value
}

function closeDropdown() {
  isOpen.value = false
}

async function selectCharacter(mappingId: number) {
  // If we have guild contexts, use the API to switch
  if (guildContextStore.guilds.length > 0) {
    await guildContextStore.switchCharacter(mappingId)
  } else {
    // Otherwise, just select the Battle.net character locally
    guildContextStore.selectBattlenetCharacter(mappingId)
  }
  closeDropdown()
}

// WoW class colors
const classColors: Record<string, string> = {
  WARRIOR: 'text-amber-700',
  PALADIN: 'text-pink-400',
  HUNTER: 'text-green-500',
  ROGUE: 'text-yellow-400',
  PRIEST: 'text-white',
  DEATH_KNIGHT: 'text-red-500',
  SHAMAN: 'text-blue-400',
  MAGE: 'text-cyan-300',
  WARLOCK: 'text-purple-400',
  MONK: 'text-emerald-400',
  DRUID: 'text-orange-400',
  DEMON_HUNTER: 'text-purple-600',
  EVOKER: 'text-teal-400',
}

function getClassColor(characterClass: string): string {
  return classColors[characterClass] || 'text-gray-300'
}
</script>

<template>
  <div class="relative">
    <!-- Trigger button -->
    <button
      @click="toggleDropdown"
      class="flex items-center w-full px-3 py-2 text-left rounded-md bg-gray-700 hover:bg-gray-600 transition-colors"
      :disabled="guildContextStore.loading"
    >
      <template v-if="currentCharacter">
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium truncate" :class="getClassColor(currentCharacter.characterClass)">
            {{ currentCharacter.name }}
          </p>
          <p class="text-xs text-gray-400 truncate">
            {{ currentCharacter.guildName }}
          </p>
        </div>
        <svg
          class="flex-shrink-0 w-4 h-4 ml-2 text-gray-400 transition-transform"
          :class="{ 'rotate-180': isOpen }"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
        </svg>
      </template>
      <template v-else-if="guildContextStore.loading">
        <div class="animate-spin w-4 h-4 border-2 border-primary-500 border-t-transparent rounded-full"></div>
        <span class="ml-2 text-sm text-gray-400">Loading...</span>
      </template>
      <template v-else>
        <span class="text-sm text-gray-400">No character selected</span>
      </template>
    </button>

    <!-- Dropdown menu -->
    <div
      v-if="isOpen && guildContextStore.allCharacters.length > 0"
      class="absolute left-0 right-0 z-50 mt-1 bg-gray-800 border border-gray-700 rounded-md shadow-lg overflow-hidden"
    >
      <div class="max-h-64 overflow-y-auto">
        <button
          v-for="character in guildContextStore.allCharacters"
          :key="character.characterMappingId"
          @click="selectCharacter(character.characterMappingId)"
          class="flex items-center w-full px-3 py-2 text-left hover:bg-gray-700 transition-colors"
          :class="{ 'bg-gray-700/50': character.isActive || character.characterName === currentCharacter?.name }"
        >
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium truncate" :class="getClassColor(character.characterClass)">
              {{ character.characterName }}
              <span class="text-gray-500 text-xs">({{ character.characterRealm }})</span>
            </p>
            <p class="text-xs text-gray-400 truncate">
              {{ character.guildName }}
              <span v-if="character.rank" class="text-gray-500">- {{ character.rank }}</span>
            </p>
          </div>
          <svg
            v-if="character.isActive || character.characterName === currentCharacter?.name"
            class="flex-shrink-0 w-4 h-4 text-primary-500"
            fill="currentColor"
            viewBox="0 0 20 20"
          >
            <path
              fill-rule="evenodd"
              d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
              clip-rule="evenodd"
            />
          </svg>
        </button>
      </div>
    </div>

    <!-- Click outside to close -->
    <div
      v-if="isOpen"
      class="fixed inset-0 z-40"
      @click="closeDropdown"
    />
  </div>
</template>
