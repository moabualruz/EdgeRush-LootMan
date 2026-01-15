import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import AttendancePage from './AttendancePage.vue'
import { attendanceApi, type AttendanceReport, type AttendanceRecord } from '@/api/attendance'

// Mock the APIs
vi.mock('@/api/attendance', () => ({
  attendanceApi: {
    getMyAttendance: vi.fn(),
  },
}))

describe('AttendancePage', () => {
  const mockRecords: AttendanceRecord[] = [
    {
      id: 1,
      raiderId: 1,
      raidId: 101,
      raidName: 'Nerub-ar Palace',
      raidDate: '2026-01-13T20:00:00Z',
      status: 'PRESENT',
      notes: 'On time',
    },
    {
      id: 2,
      raiderId: 1,
      raidId: 102,
      raidName: 'Nerub-ar Palace',
      raidDate: '2026-01-10T20:00:00Z',
      status: 'LATE',
      arrivalTime: '2026-01-10T20:15:00Z',
      notes: 'Traffic',
    },
    {
      id: 3,
      raiderId: 1,
      raidId: 103,
      raidName: 'Nerub-ar Palace',
      raidDate: '2026-01-06T20:00:00Z',
      status: 'EXCUSED',
      notes: 'Family event',
    },
  ]

  const mockAttendanceData: AttendanceReport = {
    raiderId: 1,
    characterName: 'TestRaider',
    totalRaids: 10,
    attendedRaids: 8,
    lateRaids: 2,
    excusedRaids: 1,
    attendanceRate: 0.89,
    lastRaidDate: '2026-01-13T20:00:00Z',
    streak: 3,
    records: mockRecords,
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(AttendancePage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          SkeletonCard: true,
          SkeletonTable: true,
          ProgressBar: true,
          DonutChart: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(attendanceApi.getMyAttendance).mockResolvedValue(mockAttendanceData)
  })

  it('should render page title "Attendance"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Attendance')
  })

  it('should display attendance percentage', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // 89% attendance rate
    expect(wrapper.text()).toContain('89%')
    expect(wrapper.text()).toContain('Attendance Rate')
  })

  it('should show raids attended count', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('8/10')
    expect(wrapper.text()).toContain('Raids Attended')
  })

  it('should show ACS breakdown section', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Attendance Commitment Score (ACS)')
    expect(wrapper.text()).toContain('1 excused')
  })

  it('should toggle between list and calendar view', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Default is list view - should show table
    expect(wrapper.find('table').exists()).toBe(true)

    // Click calendar view button
    const calendarButton = wrapper.findAll('button').find((b) => b.text() === 'Calendar View')
    await calendarButton?.trigger('click')

    // Should now show calendar grid (day headers)
    expect(wrapper.text()).toContain('Sun')
    expect(wrapper.text()).toContain('Mon')
    expect(wrapper.text()).toContain('Tue')
  })

  it('should display attendance by status type in list', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Check for status labels
    expect(wrapper.text()).toContain('Present')
    expect(wrapper.text()).toContain('Late')
    expect(wrapper.text()).toContain('Excused')
  })

  it('should handle loading state with skeleton', () => {
    // Make API never resolve to keep loading state
    vi.mocked(attendanceApi.getMyAttendance).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.findComponent({ name: 'SkeletonCard' }).exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(attendanceApi.getMyAttendance).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load attendance data')
  })

  it('should display late arrivals count', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('2')
    expect(wrapper.text()).toContain('Late Arrivals')
  })

  it('should display current streak', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('3')
    expect(wrapper.text()).toContain('Current Streak')
  })

  it('should display character name', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('TestRaider')
  })

  it('should show empty state when no records', async () => {
    vi.mocked(attendanceApi.getMyAttendance).mockResolvedValue({
      ...mockAttendanceData,
      records: [],
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No attendance records found')
  })
})
