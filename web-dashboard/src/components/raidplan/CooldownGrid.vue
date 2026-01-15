<script setup lang="ts">
/**
 * CooldownGrid - Raid cooldown assignment grid.
 *
 * Displays raid roster with their cooldowns and allows assignment
 * to boss abilities on a timeline.
 */
import { ref, computed } from 'vue'

// Types
export interface RosterMember {
  id: number
  name: string
  class: string
  spec: string
  role: 'TANK' | 'HEALER' | 'DPS'
}

export interface Cooldown {
  id: string
  name: string
  spellId: number
  duration: number
  cooldownTime: number
  icon: string
}

export interface BossAbility {
  id: string
  name: string
  time: number
  damage: 'HIGH' | 'MEDIUM' | 'LOW'
  requiresCooldown: boolean
}

export interface CooldownAssignment {
  playerId: number
  cooldownId: string
  abilityId: string
  time: number
}

export interface CooldownGridProps {
  roster: RosterMember[]
  cooldowns: Record<string, Cooldown[]>
  bossAbilities: BossAbility[]
  assignments: CooldownAssignment[]
  fightDuration: number
}

const props = defineProps<CooldownGridProps>()

const emit = defineEmits<{
  'assign-cooldown': [assignment: CooldownAssignment]
  'remove-assignment': [assignment: CooldownAssignment]
  'export-mrt': [note: string]
  'export-weakaura': [data: string]
}>()

// State
const showExportMenu = ref(false)
const roleFilter = ref<'ALL' | 'TANK' | 'HEALER' | 'DPS'>('ALL')
const draggedCooldown = ref<{ playerId: number; cooldownId: string } | null>(null)

// Class colors
const classColors: Record<string, string> = {
  WARRIOR: '#C79C6E',
  PALADIN: '#F58CBA',
  HUNTER: '#ABD473',
  ROGUE: '#FFF569',
  PRIEST: '#FFFFFF',
  SHAMAN: '#0070DE',
  MAGE: '#69CCF0',
  WARLOCK: '#9482C9',
  MONK: '#00FF96',
  DRUID: '#FF7D0A',
  DEMON_HUNTER: '#A330C9',
  DEATH_KNIGHT: '#C41F3B',
  EVOKER: '#33937F',
}

// Computed
const filteredRoster = computed(() => {
  if (roleFilter.value === 'ALL') return props.roster
  return props.roster.filter((m) => m.role === roleFilter.value)
})

const timeMarkers = computed(() => {
  const markers: number[] = []
  for (let t = 30; t <= props.fightDuration; t += 30) {
    markers.push(t)
  }
  return markers
})

const cooldownWarnings = computed(() => {
  const warnings: Set<string> = new Set()

  // Check for cooldown overlap violations
  props.assignments.forEach((assignment, index) => {
    const cooldown = getCooldownById(assignment.cooldownId)
    if (!cooldown) return

    // Check if same cooldown is used again before it's ready
    props.assignments.forEach((other, otherIndex) => {
      if (index >= otherIndex) return
      if (
        assignment.playerId === other.playerId &&
        assignment.cooldownId === other.cooldownId
      ) {
        const timeDiff = Math.abs(assignment.time - other.time)
        if (timeDiff < cooldown.cooldownTime) {
          warnings.add(`${assignment.playerId}-${assignment.cooldownId}-${assignment.time}`)
          warnings.add(`${other.playerId}-${other.cooldownId}-${other.time}`)
        }
      }
    })
  })

  return warnings
})

const uncoveredAbilities = computed(() => {
  return props.bossAbilities
    .filter((ability) => ability.requiresCooldown)
    .filter((ability) => !props.assignments.some((a) => a.abilityId === ability.id))
    .map((a) => a.id)
})

// Methods
function formatTime(seconds: number): string {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

function getClassColor(className: string): string {
  return classColors[className] || '#FFFFFF'
}

function getPlayerCooldowns(player: RosterMember): Cooldown[] {
  return props.cooldowns[player.class] || []
}

function getCooldownById(cooldownId: string): Cooldown | undefined {
  for (const cooldowns of Object.values(props.cooldowns)) {
    const found = cooldowns.find((c) => c.id === cooldownId)
    if (found) return found
  }
  return undefined
}

function getTimelinePosition(time: number): string {
  return `${(time / props.fightDuration) * 100}%`
}

function handleDragStart(playerId: number, cooldownId: string) {
  draggedCooldown.value = { playerId, cooldownId }
}

function handleDragEnd() {
  draggedCooldown.value = null
}

function handleDrop(ability: BossAbility) {
  if (!draggedCooldown.value) return

  emit('assign-cooldown', {
    playerId: draggedCooldown.value.playerId,
    cooldownId: draggedCooldown.value.cooldownId,
    abilityId: ability.id,
    time: ability.time,
  })

  draggedCooldown.value = null
}

function handleRemoveAssignment(assignment: CooldownAssignment) {
  emit('remove-assignment', assignment)
}

function hasWarning(assignment: CooldownAssignment): boolean {
  return cooldownWarnings.value.has(
    `${assignment.playerId}-${assignment.cooldownId}-${assignment.time}`
  )
}

function getAssignmentPlayer(assignment: CooldownAssignment): RosterMember | undefined {
  return props.roster.find((r) => r.id === assignment.playerId)
}

function setRoleFilter(role: 'ALL' | 'TANK' | 'HEALER' | 'DPS') {
  roleFilter.value = role
}

function toggleExportMenu() {
  showExportMenu.value = !showExportMenu.value
}

function exportToMRT() {
  // Generate MRT note format
  const lines: string[] = []
  lines.push('|cff00ff00--- Cooldown Assignments ---|r')

  props.bossAbilities.forEach((ability) => {
    const abilityAssignments = props.assignments.filter((a) => a.abilityId === ability.id)
    if (abilityAssignments.length > 0) {
      lines.push(`|cffff9900${formatTime(ability.time)} - ${ability.name}:|r`)
      abilityAssignments.forEach((assignment) => {
        const player = getAssignmentPlayer(assignment)
        const cooldown = getCooldownById(assignment.cooldownId)
        if (player && cooldown) {
          lines.push(`  {spell:${cooldown.spellId}} ${player.name}`)
        }
      })
    }
  })

  emit('export-mrt', lines.join('\n'))
  showExportMenu.value = false
}

function exportToWeakAura() {
  // Generate simplified WeakAura data
  const waData = {
    assignments: props.assignments.map((a) => {
      const cooldown = getCooldownById(a.cooldownId)
      const player = getAssignmentPlayer(a)
      return {
        time: a.time,
        spellId: cooldown?.spellId,
        playerName: player?.name,
      }
    }),
  }

  emit('export-weakaura', JSON.stringify(waData))
  showExportMenu.value = false
}
</script>

<template>
  <div data-testid="cooldown-grid" class="cooldown-grid bg-gray-800 rounded-lg p-4">
    <!-- Empty States -->
    <div v-if="roster.length === 0" class="text-center py-8 text-gray-400">
      No roster available
    </div>

    <div v-else-if="bossAbilities.length === 0" class="text-center py-8 text-gray-400">
      No boss abilities defined
    </div>

    <template v-else>
      <!-- Header with filters and export -->
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-400">Filter:</span>
          <button
            :class="['filter-btn', { active: roleFilter === 'ALL' }]"
            @click="setRoleFilter('ALL')"
          >
            All
          </button>
          <button
            data-testid="filter-healers"
            :class="['filter-btn', { active: roleFilter === 'HEALER' }]"
            @click="setRoleFilter('HEALER')"
          >
            Healers
          </button>
          <button
            :class="['filter-btn', { active: roleFilter === 'TANK' }]"
            @click="setRoleFilter('TANK')"
          >
            Tanks
          </button>
          <button
            :class="['filter-btn', { active: roleFilter === 'DPS' }]"
            @click="setRoleFilter('DPS')"
          >
            DPS
          </button>
        </div>

        <div class="relative">
          <button
            data-testid="export-button"
            class="btn-secondary"
            @click="toggleExportMenu"
          >
            Export
          </button>
          <div
            v-if="showExportMenu"
            class="absolute right-0 top-full mt-1 bg-gray-700 rounded shadow-lg z-10"
          >
            <button
              data-testid="export-mrt"
              class="block w-full px-4 py-2 text-left text-sm hover:bg-gray-600"
              @click="exportToMRT"
            >
              MRT Note
            </button>
            <button
              data-testid="export-weakaura"
              class="block w-full px-4 py-2 text-left text-sm hover:bg-gray-600"
              @click="exportToWeakAura"
            >
              WeakAura
            </button>
          </div>
        </div>
      </div>

      <!-- Boss Timeline -->
      <div data-testid="boss-timeline" class="boss-timeline mb-4 relative h-16 bg-gray-900 rounded">
        <!-- Time markers -->
        <div
          v-for="time in timeMarkers"
          :key="time"
          class="absolute top-0 h-full border-l border-gray-700 text-xs text-gray-500"
          :style="{ left: getTimelinePosition(time) }"
        >
          <span class="ml-1">{{ formatTime(time) }}</span>
        </div>

        <!-- Boss abilities -->
        <div
          v-for="ability in bossAbilities"
          :key="ability.id"
          data-testid="ability-marker"
          :class="[
            'absolute top-6 transform -translate-x-1/2 cursor-pointer',
            { uncovered: uncoveredAbilities.includes(ability.id) },
          ]"
          :style="{ left: getTimelinePosition(ability.time) }"
          :title="`${ability.name} at ${formatTime(ability.time)}`"
          @dragover.prevent
          @drop="handleDrop(ability)"
        >
          <div
            :class="[
              'w-4 h-4 rounded',
              ability.damage === 'HIGH' ? 'bg-red-500' : ability.damage === 'MEDIUM' ? 'bg-yellow-500' : 'bg-green-500',
            ]"
          />
          <span class="text-xs whitespace-nowrap">{{ ability.name }}</span>
        </div>

        <!-- Assigned cooldowns on timeline -->
        <div
          v-for="(assignment, index) in assignments"
          :key="`assignment-${index}`"
          data-testid="assigned-cooldown"
          :class="['absolute top-1 cursor-pointer', { 'ring-2 ring-red-500': hasWarning(assignment) }]"
          :style="{ left: getTimelinePosition(assignment.time) }"
          @click="handleRemoveAssignment(assignment)"
        >
          <div
            class="w-6 h-6 rounded bg-blue-600 flex items-center justify-center text-xs"
            :style="{ backgroundColor: getClassColor(getAssignmentPlayer(assignment)?.class || '') }"
            :title="`${getAssignmentPlayer(assignment)?.name} - ${getCooldownById(assignment.cooldownId)?.name}`"
          >
            {{ getAssignmentPlayer(assignment)?.name?.charAt(0) }}
          </div>
        </div>

        <!-- Warnings -->
        <div
          v-for="(assignment, index) in assignments.filter(a => hasWarning(a))"
          :key="`warning-${index}`"
          data-testid="cooldown-warning"
          class="absolute -top-2 text-red-500 text-xs"
          :style="{ left: getTimelinePosition(assignment.time) }"
        >
          !
        </div>
      </div>

      <!-- Roster Grid -->
      <div class="roster-grid space-y-2">
        <div
          v-for="member in filteredRoster"
          :key="member.id"
          data-testid="roster-row"
          :class="['roster-row flex items-center gap-4 p-2 bg-gray-900 rounded', { hidden: roleFilter !== 'ALL' && member.role !== roleFilter }]"
        >
          <!-- Player info -->
          <div class="w-32 flex-shrink-0">
            <span
              class="font-medium"
              :style="{ color: getClassColor(member.class) }"
            >
              {{ member.name }}
            </span>
            <div class="text-xs text-gray-500">{{ member.spec }}</div>
          </div>

          <!-- Cooldowns -->
          <div class="flex gap-2 flex-wrap">
            <button
              v-for="cooldown in getPlayerCooldowns(member)"
              :key="cooldown.id"
              data-testid="cooldown-button"
              class="cooldown-btn"
              draggable="true"
              :title="`${cooldown.name} (${cooldown.cooldownTime}s CD)`"
              @dragstart="handleDragStart(member.id, cooldown.id)"
              @dragend="handleDragEnd"
            >
              <span class="text-xs">{{ cooldown.name }}</span>
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.filter-btn {
  @apply px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600 transition-colors;
}

.filter-btn.active {
  @apply bg-blue-600 text-white;
}

.btn-secondary {
  @apply px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded text-white text-sm font-medium transition-colors;
}

.cooldown-btn {
  @apply px-2 py-1 bg-gray-700 hover:bg-gray-600 rounded cursor-grab active:cursor-grabbing transition-colors;
}

.ability-marker.uncovered {
  @apply ring-2 ring-yellow-500 ring-offset-2 ring-offset-gray-900;
}
</style>
