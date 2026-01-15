<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { attendanceApi, type AttendanceStatus, type AttendanceRecord } from '@/api/attendance'
import { formatDate } from '@/utils/date'
import SkeletonCard from '@/components/SkeletonCard.vue'
import SkeletonTable from '@/components/SkeletonTable.vue'
import { DonutChart, ProgressBar } from '@/components/charts'

const GUILD_ID = import.meta.env.VITE_GUILD_ID || 'default'

// View mode: list or calendar
const viewMode = ref<'list' | 'calendar'>('list')

// Attendance report query
const { data: attendanceData, isLoading, error } = useQuery({
  queryKey: ['myAttendance', GUILD_ID],
  queryFn: () => attendanceApi.getMyAttendance(GUILD_ID),
})

// Status breakdown for donut chart
const statusBreakdown = computed(() => {
  if (!attendanceData.value?.records) return []
  const counts: Record<string, number> = {}
  attendanceData.value.records.forEach(r => {
    counts[r.status] = (counts[r.status] || 0) + 1
  })
  const colors: Record<string, string> = {
    PRESENT: '#22c55e',
    ABSENT: '#ef4444',
    LATE: '#eab308',
    EXCUSED: '#3b82f6',
    BENCH: '#a855f7',
  }
  return Object.entries(counts).map(([status, value]) => ({
    label: getStatusLabel(status as AttendanceStatus),
    value,
    color: colors[status] || '#6b7280',
  }))
})

// Calendar state
const currentMonth = ref(new Date())

// Calendar computed properties
const calendarDays = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startPadding = firstDay.getDay()

  const days: Array<{ date: Date; record?: AttendanceRecord; isCurrentMonth: boolean }> = []

  // Previous month padding
  for (let i = startPadding - 1; i >= 0; i--) {
    const date = new Date(year, month, -i)
    days.push({ date, isCurrentMonth: false })
  }

  // Current month days
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(year, month, day)
    const dateStr = date.toISOString().split('T')[0]
    const record = attendanceData.value?.records.find(r => r.raidDate.split('T')[0] === dateStr)
    days.push({ date, record, isCurrentMonth: true })
  }

  // Next month padding to fill the grid
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month + 1, i)
    days.push({ date, isCurrentMonth: false })
  }

  return days
})

const monthYear = computed(() => {
  return currentMonth.value.toLocaleDateString('en-US', { month: 'long', year: 'numeric' })
})

// Helper functions
function getStatusColor(status: AttendanceStatus): string {
  switch (status) {
    case 'PRESENT':
      return 'bg-green-500'
    case 'ABSENT':
      return 'bg-red-500'
    case 'LATE':
      return 'bg-yellow-500'
    case 'EXCUSED':
      return 'bg-blue-500'
    case 'BENCH':
      return 'bg-purple-500'
    default:
      return 'bg-gray-500'
  }
}

function getStatusTextColor(status: AttendanceStatus): string {
  switch (status) {
    case 'PRESENT':
      return 'text-green-400'
    case 'ABSENT':
      return 'text-red-400'
    case 'LATE':
      return 'text-yellow-400'
    case 'EXCUSED':
      return 'text-blue-400'
    case 'BENCH':
      return 'text-purple-400'
    default:
      return 'text-gray-400'
  }
}

function getStatusLabel(status: AttendanceStatus): string {
  switch (status) {
    case 'PRESENT':
      return 'Present'
    case 'ABSENT':
      return 'Absent'
    case 'LATE':
      return 'Late'
    case 'EXCUSED':
      return 'Excused'
    case 'BENCH':
      return 'Benched'
    default:
      return status
  }
}

function getAttendanceColor(rate: number): string {
  if (rate >= 0.9) return 'text-green-400'
  if (rate >= 0.75) return 'text-yellow-400'
  return 'text-red-400'
}

function previousMonth() {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1, 1)
}

function nextMonth() {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 1)
}

function isToday(date: Date): boolean {
  const today = new Date()
  return date.toDateString() === today.toDateString()
}
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">Attendance</h1>
      <div v-if="attendanceData" class="text-sm text-gray-400">
        {{ attendanceData.characterName }}
      </div>
    </div>

    <!-- Loading state with skeletons -->
    <div v-if="isLoading" class="space-y-6">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <SkeletonCard :lines="1" :show-header="false" />
        <SkeletonCard :lines="1" :show-header="false" />
        <SkeletonCard :lines="1" :show-header="false" />
        <SkeletonCard :lines="1" :show-header="false" />
      </div>
      <SkeletonCard :lines="3" />
      <SkeletonTable :rows="5" :columns="4" />
    </div>

    <!-- Error state -->
    <div v-else-if="error" class="card bg-red-900/20 border-red-700">
      <p class="text-red-400">Failed to load attendance data. Please try again.</p>
    </div>

    <!-- Content -->
    <div v-else-if="attendanceData" class="space-y-6">
      <!-- Summary Cards -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div class="card text-center">
          <div :class="['text-3xl font-bold', getAttendanceColor(attendanceData.attendanceRate)]">
            {{ (attendanceData.attendanceRate * 100).toFixed(0) }}%
          </div>
          <div class="text-sm text-gray-400 mt-1">Attendance Rate</div>
        </div>
        <div class="card text-center">
          <div class="text-3xl font-bold text-primary-400">
            {{ attendanceData.attendedRaids }}/{{ attendanceData.totalRaids }}
          </div>
          <div class="text-sm text-gray-400 mt-1">Raids Attended</div>
        </div>
        <div class="card text-center">
          <div class="text-3xl font-bold text-yellow-400">
            {{ attendanceData.lateRaids }}
          </div>
          <div class="text-sm text-gray-400 mt-1">Late Arrivals</div>
        </div>
        <div class="card text-center">
          <div class="text-3xl font-bold text-blue-400">
            {{ attendanceData.streak }}
          </div>
          <div class="text-sm text-gray-400 mt-1">Current Streak</div>
        </div>
      </div>

      <!-- ACS Breakdown with Chart -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="card">
          <h2 class="text-lg font-semibold mb-4">Attendance Commitment Score (ACS)</h2>
          <div class="space-y-4">
            <ProgressBar
              :value="attendanceData.attendanceRate * 100"
              :max="100"
              label="Attendance Rate"
              :color="attendanceData.attendanceRate >= 0.9 ? '#22c55e' : attendanceData.attendanceRate >= 0.75 ? '#eab308' : '#ef4444'"
            />
            <div class="flex justify-between text-sm pt-2">
              <span class="text-gray-400">Excused absences not counted against you</span>
              <span class="text-blue-400">{{ attendanceData.excusedRaids }} excused</span>
            </div>
          </div>
        </div>

        <!-- Status Breakdown Chart -->
        <div v-if="statusBreakdown.length > 0" class="card">
          <h2 class="text-lg font-semibold mb-4">Status Breakdown</h2>
          <DonutChart
            :data="statusBreakdown"
            :size="160"
            center-label="Records"
          />
        </div>
      </div>

      <!-- View Toggle -->
      <div class="flex items-center space-x-2">
        <button
          @click="viewMode = 'list'"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            viewMode === 'list' ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-400 hover:bg-gray-600'
          ]"
        >
          List View
        </button>
        <button
          @click="viewMode = 'calendar'"
          :class="[
            'px-4 py-2 rounded-lg text-sm font-medium transition-colors',
            viewMode === 'calendar' ? 'bg-primary-600 text-white' : 'bg-gray-700 text-gray-400 hover:bg-gray-600'
          ]"
        >
          Calendar View
        </button>
      </div>

      <!-- List View -->
      <div v-if="viewMode === 'list'" class="card overflow-hidden p-0">
        <table class="w-full">
          <thead class="bg-gray-800/50">
            <tr>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Raid</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Date</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Status</th>
              <th class="text-left px-4 py-3 text-sm font-medium text-gray-400">Notes</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-700">
            <tr v-if="attendanceData.records.length === 0">
              <td colspan="4" class="px-4 py-8 text-center text-gray-400">
                No attendance records found.
              </td>
            </tr>
            <tr
              v-for="record in attendanceData.records"
              :key="record.id"
              class="hover:bg-gray-800/30 transition-colors"
            >
              <td class="px-4 py-3">
                <span class="font-medium">{{ record.raidName }}</span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-400">
                {{ formatDate(record.raidDate) }}
              </td>
              <td class="px-4 py-3">
                <span
                  :class="[
                    'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium',
                    getStatusTextColor(record.status),
                    'bg-opacity-20'
                  ]"
                >
                  <span :class="['w-2 h-2 rounded-full mr-1.5', getStatusColor(record.status)]"></span>
                  {{ getStatusLabel(record.status) }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-gray-500">
                {{ record.notes || '-' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Calendar View -->
      <div v-else class="card">
        <!-- Calendar Header -->
        <div class="flex items-center justify-between mb-4">
          <button @click="previousMonth" class="p-2 hover:bg-gray-700 rounded-lg transition-colors">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <h3 class="text-lg font-semibold">{{ monthYear }}</h3>
          <button @click="nextMonth" class="p-2 hover:bg-gray-700 rounded-lg transition-colors">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>

        <!-- Calendar Grid -->
        <div class="grid grid-cols-7 gap-1">
          <!-- Day headers -->
          <div v-for="day in ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']" :key="day" class="text-center text-sm text-gray-500 py-2">
            {{ day }}
          </div>

          <!-- Calendar days -->
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            :class="[
              'aspect-square p-1 rounded-lg text-center relative',
              day.isCurrentMonth ? 'bg-gray-800/30' : 'bg-gray-900/30',
              isToday(day.date) ? 'ring-2 ring-primary-500' : ''
            ]"
          >
            <div :class="['text-sm', day.isCurrentMonth ? 'text-gray-300' : 'text-gray-600']">
              {{ day.date.getDate() }}
            </div>
            <div
              v-if="day.record"
              :class="['w-3 h-3 rounded-full mx-auto mt-1', getStatusColor(day.record.status)]"
              :title="`${day.record.raidName}: ${getStatusLabel(day.record.status)}`"
            ></div>
          </div>
        </div>

        <!-- Legend -->
        <div class="flex flex-wrap gap-4 mt-4 pt-4 border-t border-gray-700">
          <div v-for="status in ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED', 'BENCH'] as AttendanceStatus[]" :key="status" class="flex items-center space-x-2">
            <span :class="['w-3 h-3 rounded-full', getStatusColor(status)]"></span>
            <span class="text-sm text-gray-400">{{ getStatusLabel(status) }}</span>
          </div>
        </div>
      </div>

      <!-- Last Raid Info -->
      <div v-if="attendanceData.lastRaidDate" class="text-sm text-gray-500 text-center">
        Last raid: {{ formatDate(attendanceData.lastRaidDate) }}
      </div>
    </div>
  </div>
</template>
