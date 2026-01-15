<script setup lang="ts">
/**
 * CooldownsPage - Raid cooldown assignment page.
 *
 * Allows assigning raid cooldowns to boss abilities with export options.
 */
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import CooldownGrid, {
  type RosterMember,
  type Cooldown,
  type BossAbility,
  type CooldownAssignment,
} from '@/components/raidplan/CooldownGrid.vue'
import Skeleton from '@/components/Skeleton.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// State
const isLoading = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const selectedEncounter = ref<string>('')
const assignments = ref<CooldownAssignment[]>([])

// Mock data - in real implementation this would come from API
const encounters = ref([
  { id: '2902', name: 'Queen Ansurek' },
  { id: '2901', name: 'The Silken Court' },
  { id: '2900', name: "Broodtwister Ovi'nax" },
])

const roster = ref<RosterMember[]>([
  { id: 1, name: 'Tankyboi', class: 'WARRIOR', spec: 'Protection', role: 'TANK' },
  { id: 2, name: 'Offtank', class: 'PALADIN', spec: 'Protection', role: 'TANK' },
  { id: 3, name: 'Healmaster', class: 'PRIEST', spec: 'Holy', role: 'HEALER' },
  { id: 4, name: 'Healbot', class: 'PALADIN', spec: 'Holy', role: 'HEALER' },
  { id: 5, name: 'Treehugger', class: 'DRUID', spec: 'Restoration', role: 'HEALER' },
  { id: 6, name: 'Mistweaver', class: 'MONK', spec: 'Mistweaver', role: 'HEALER' },
  { id: 7, name: 'Bigdps', class: 'MAGE', spec: 'Fire', role: 'DPS' },
  { id: 8, name: 'Stabby', class: 'ROGUE', spec: 'Assassination', role: 'DPS' },
])

const cooldowns = ref<Record<string, Cooldown[]>>({
  WARRIOR: [
    { id: 'rallying-cry', name: 'Rallying Cry', spellId: 97462, duration: 10, cooldownTime: 180, icon: 'ability_warrior_rallyingcry' },
  ],
  PALADIN: [
    { id: 'aura-mastery', name: 'Aura Mastery', spellId: 31821, duration: 8, cooldownTime: 180, icon: 'spell_holy_auramastery' },
    { id: 'divine-toll', name: 'Divine Toll', spellId: 375576, duration: 0, cooldownTime: 60, icon: 'ability_bastion_paladin' },
  ],
  PRIEST: [
    { id: 'divine-hymn', name: 'Divine Hymn', spellId: 64843, duration: 8, cooldownTime: 180, icon: 'spell_holy_divinehymn' },
    { id: 'barrier', name: 'Power Word: Barrier', spellId: 62618, duration: 10, cooldownTime: 180, icon: 'spell_holy_powerwordbarrier' },
  ],
  DRUID: [
    { id: 'tranquility', name: 'Tranquility', spellId: 740, duration: 8, cooldownTime: 180, icon: 'spell_nature_tranquility' },
  ],
  MONK: [
    { id: 'revival', name: 'Revival', spellId: 115310, duration: 0, cooldownTime: 180, icon: 'spell_monk_revival' },
  ],
  MAGE: [],
  ROGUE: [],
})

const bossAbilities = ref<BossAbility[]>([
  { id: 'ability-1', name: 'Silken Tomb', time: 25, damage: 'HIGH', requiresCooldown: true },
  { id: 'ability-2', name: 'Venomous Rain', time: 55, damage: 'MEDIUM', requiresCooldown: true },
  { id: 'ability-3', name: 'Royal Condemnation', time: 90, damage: 'HIGH', requiresCooldown: true },
  { id: 'ability-4', name: 'Liquefy', time: 130, damage: 'HIGH', requiresCooldown: true },
  { id: 'ability-5', name: 'Frothing Gluttony', time: 165, damage: 'HIGH', requiresCooldown: true },
])

// Computed
const fightDuration = computed(() => 180)

// Methods
function handleAssignCooldown(assignment: CooldownAssignment) {
  assignments.value.push(assignment)
}

function handleRemoveAssignment(assignment: CooldownAssignment) {
  const index = assignments.value.findIndex(
    (a) =>
      a.playerId === assignment.playerId &&
      a.cooldownId === assignment.cooldownId &&
      a.abilityId === assignment.abilityId
  )
  if (index !== -1) {
    assignments.value.splice(index, 1)
  }
}

function handleExportMRT(note: string) {
  navigator.clipboard.writeText(note)
  showNotification('MRT note copied to clipboard!')
}

function handleExportWeakAura(data: string) {
  navigator.clipboard.writeText(data)
  showNotification('WeakAura data copied to clipboard!')
}

function showNotification(message: string) {
  toastMessage.value = message
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

function save() {
  showNotification('Assignments saved!')
}

function reset() {
  assignments.value = []
  showNotification('Assignments reset')
}

function goBack() {
  router.back()
}
</script>

<template>
  <div class="cooldowns-page min-h-screen bg-gray-900 text-white p-6">
    <!-- Loading State -->
    <div v-if="isLoading">
      <Skeleton class="h-8 w-64 mb-4" />
      <Skeleton class="h-96" />
    </div>

    <template v-else>
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold">Cooldown Assignments</h1>
          <p class="text-gray-400 text-sm">Assign raid cooldowns to boss abilities</p>
        </div>
        <div class="flex items-center gap-2">
          <button
            data-testid="reset-button"
            class="btn-secondary"
            @click="reset"
          >
            Reset
          </button>
          <button
            data-testid="save-button"
            class="btn-primary"
            @click="save"
          >
            Save
          </button>
        </div>
      </div>

      <!-- Encounter Selector -->
      <div class="mb-6">
        <label class="block text-sm text-gray-400 mb-2">Select Encounter</label>
        <select
          v-model="selectedEncounter"
          data-testid="encounter-selector"
          class="input w-64"
        >
          <option value="">Choose an encounter...</option>
          <option
            v-for="encounter in encounters"
            :key="encounter.id"
            :value="encounter.id"
          >
            {{ encounter.name }}
          </option>
        </select>
      </div>

      <!-- Cooldown Grid -->
      <CooldownGrid
        :roster="roster"
        :cooldowns="cooldowns"
        :boss-abilities="bossAbilities"
        :assignments="assignments"
        :fight-duration="fightDuration"
        @assign-cooldown="handleAssignCooldown"
        @remove-assignment="handleRemoveAssignment"
        @export-mrt="handleExportMRT"
        @export-weakaura="handleExportWeakAura"
      />
    </template>

    <!-- Toast Notification -->
    <div
      v-if="showToast"
      data-testid="toast"
      class="fixed bottom-4 right-4 bg-green-600 text-white px-4 py-2 rounded shadow-lg z-50"
    >
      {{ toastMessage }}
    </div>
  </div>
</template>

<style scoped>
.btn-primary {
  @apply px-4 py-2 bg-blue-600 hover:bg-blue-500 rounded text-white text-sm font-medium transition-colors;
}

.btn-secondary {
  @apply px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded text-white text-sm font-medium transition-colors;
}

.input {
  @apply bg-gray-700 border border-gray-600 rounded px-3 py-2 text-white focus:outline-none focus:border-blue-500;
}
</style>
