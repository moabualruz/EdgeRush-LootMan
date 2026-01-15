<script setup lang="ts">
/**
 * RaidCalendar - Calendar view for raid planning and scheduling.
 *
 * Features:
 * - Month navigation
 * - Raid indicators on scheduled days
 * - Click to view raid details
 * - Color coding by difficulty
 * - Today highlighting
 */
import { ref, computed, watch } from 'vue'
import type { Raid } from '@/api/raids'

const props = withDefaults(
  defineProps<{
    raids: Raid[]
    selectedDate?: Date
    showLegend?: boolean
  }>(),
  {
    showLegend: true,
  }
)

const emit = defineEmits<{
  'raid-click': [raid: Raid]
  'date-select': [date: Date]
  'month-change': [date: Date]
}>()

// Current month view
const currentMonth = ref(new Date())

// Calendar calculations
const monthYear = computed(() => {
  return currentMonth.value.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
})

const calendarDays = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startPadding = firstDay.getDay()

  const days: Array<{
    date: Date
    isCurrentMonth: boolean
    isToday: boolean
    isSelected: boolean
    raids: Raid[]
  }> = []

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  // Previous month padding
  for (let i = startPadding - 1; i >= 0; i--) {
    const date = new Date(year, month, -i)
    days.push({
      date,
      isCurrentMonth: false,
      isToday: false,
      isSelected: false,
      raids: getRaidsForDate(date),
    })
  }

  // Current month days
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(year, month, day)
    date.setHours(0, 0, 0, 0)
    const isToday = date.getTime() === today.getTime()
    const isSelected = props.selectedDate
      ? date.toDateString() === props.selectedDate.toDateString()
      : false

    days.push({
      date,
      isCurrentMonth: true,
      isToday,
      isSelected,
      raids: getRaidsForDate(date),
    })
  }

  // Next month padding to fill 6 rows
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month + 1, i)
    days.push({
      date,
      isCurrentMonth: false,
      isToday: false,
      isSelected: false,
      raids: getRaidsForDate(date),
    })
  }

  return days
})

// Helper functions
function getRaidsForDate(date: Date): Raid[] {
  const dateStr = date.toISOString().split('T')[0]
  return props.raids.filter((raid) => {
    const raidDate = new Date(raid.scheduledAt).toISOString().split('T')[0]
    return raidDate === dateStr
  })
}

function getDifficultyColor(difficulty: Raid['difficulty']): string {
  switch (difficulty) {
    case 'MYTHIC':
      return 'bg-purple-500'
    case 'HEROIC':
      return 'bg-orange-500'
    case 'NORMAL':
      return 'bg-green-500'
    default:
      return 'bg-gray-500'
  }
}

function getDifficultyBorder(difficulty: Raid['difficulty']): string {
  switch (difficulty) {
    case 'MYTHIC':
      return 'border-purple-500'
    case 'HEROIC':
      return 'border-orange-500'
    case 'NORMAL':
      return 'border-green-500'
    default:
      return 'border-gray-500'
  }
}

function getStatusOpacity(status: Raid['status']): string {
  switch (status) {
    case 'COMPLETED':
      return 'opacity-60'
    case 'CANCELLED':
      return 'opacity-40 line-through'
    default:
      return ''
  }
}

// Navigation
function previousMonth() {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() - 1,
    1
  )
  emit('month-change', currentMonth.value)
}

function nextMonth() {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() + 1,
    1
  )
  emit('month-change', currentMonth.value)
}

function goToToday() {
  currentMonth.value = new Date()
  emit('month-change', currentMonth.value)
}

function selectDate(date: Date) {
  emit('date-select', date)
}

function clickRaid(raid: Raid, event: Event) {
  event.stopPropagation()
  emit('raid-click', raid)
}

function formatRaidTime(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' })
}
</script>

<template>
  <div class="raid-calendar">
    <!-- Calendar Header -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center space-x-2">
        <button
          @click="previousMonth"
          class="p-2 hover:bg-gray-700 rounded-lg transition-colors"
          title="Previous month"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h3 class="text-lg font-semibold min-w-[180px] text-center month-year">
          {{ monthYear }}
        </h3>
        <button
          @click="nextMonth"
          class="p-2 hover:bg-gray-700 rounded-lg transition-colors"
          title="Next month"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
      <button
        @click="goToToday"
        class="px-3 py-1 text-sm bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors"
      >
        Today
      </button>
    </div>

    <!-- Calendar Grid -->
    <div class="grid grid-cols-7 gap-1">
      <!-- Day headers -->
      <div
        v-for="day in ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']"
        :key="day"
        class="text-center text-sm text-gray-500 py-2 font-medium"
      >
        {{ day }}
      </div>

      <!-- Calendar days -->
      <div
        v-for="(day, index) in calendarDays"
        :key="index"
        @click="selectDate(day.date)"
        :class="[
          'calendar-day min-h-[80px] p-1 rounded-lg cursor-pointer transition-colors relative',
          day.isCurrentMonth ? 'bg-gray-800/30' : 'bg-gray-900/30',
          day.isToday ? 'today ring-2 ring-primary-500' : '',
          day.isSelected ? 'selected-day ring-2 ring-blue-500 bg-blue-900/20' : '',
          'hover:bg-gray-700/30',
        ]"
      >
        <div
          :class="[
            'text-sm font-medium',
            day.isCurrentMonth ? 'text-gray-300' : 'text-gray-600',
            day.isToday ? 'text-primary-400' : '',
          ]"
        >
          {{ day.date.getDate() }}
        </div>

        <!-- Raid indicators -->
        <div class="mt-1 space-y-1">
          <div
            v-for="raid in day.raids.slice(0, 2)"
            :key="raid.id"
            @click="clickRaid(raid, $event)"
            :class="[
              'raid-indicator text-xs px-1 py-0.5 rounded truncate cursor-pointer hover:opacity-80 transition-opacity',
              getDifficultyColor(raid.difficulty),
              getStatusOpacity(raid.status),
            ]"
            :title="`${raid.instanceName} (${raid.difficulty}) - ${formatRaidTime(raid.scheduledAt)}`"
          >
            <span class="text-white font-medium">{{ formatRaidTime(raid.scheduledAt) }}</span>
          </div>
          <div
            v-if="day.raids.length > 2"
            class="text-xs text-gray-400 px-1"
          >
            +{{ day.raids.length - 2 }} more
          </div>
        </div>
      </div>
    </div>

    <!-- Legend -->
    <div v-if="showLegend" class="flex flex-wrap gap-4 mt-4 pt-4 border-t border-gray-700">
      <div class="flex items-center space-x-2">
        <span class="w-3 h-3 rounded-full bg-purple-500"></span>
        <span class="text-sm text-gray-400">Mythic</span>
      </div>
      <div class="flex items-center space-x-2">
        <span class="w-3 h-3 rounded-full bg-orange-500"></span>
        <span class="text-sm text-gray-400">Heroic</span>
      </div>
      <div class="flex items-center space-x-2">
        <span class="w-3 h-3 rounded-full bg-green-500"></span>
        <span class="text-sm text-gray-400">Normal</span>
      </div>
      <div class="flex items-center space-x-2">
        <span class="w-3 h-3 rounded-full bg-primary-500"></span>
        <span class="text-sm text-gray-400">Today</span>
      </div>
    </div>

    <!-- Upcoming Raids List -->
    <div v-if="raids.length > 0" class="mt-6">
      <h4 class="text-sm font-medium text-gray-400 mb-3">Upcoming This Week</h4>
      <div class="space-y-2">
        <div
          v-for="raid in raids.slice(0, 5)"
          :key="raid.id"
          @click="emit('raid-click', raid)"
          :class="[
            'flex items-center justify-between p-2 rounded-lg cursor-pointer hover:bg-gray-800/50 transition-colors border-l-4',
            getDifficultyBorder(raid.difficulty),
            getStatusOpacity(raid.status),
          ]"
        >
          <div>
            <div class="font-medium text-sm">{{ raid.instanceName }}</div>
            <div class="text-xs text-gray-400">
              {{ raid.teamName }} - {{ new Date(raid.scheduledAt).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' }) }}
            </div>
          </div>
          <div class="text-right">
            <div class="text-sm">{{ formatRaidTime(raid.scheduledAt) }}</div>
            <div
              :class="[
                'text-xs',
                raid.signupCount >= raid.maxPlayers ? 'text-green-400' : 'text-gray-400',
              ]"
            >
              {{ raid.signupCount }}/{{ raid.maxPlayers }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
