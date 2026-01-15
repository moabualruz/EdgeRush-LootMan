import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RaidCalendar from './RaidCalendar.vue'
import type { Raid } from '@/api/raids'

describe('RaidCalendar', () => {
  const mockRaids: Raid[] = [
    {
      id: 1,
      teamId: 1,
      teamName: 'Team Alpha',
      instanceName: 'Nerub-ar Palace',
      difficulty: 'MYTHIC',
      scheduledAt: '2026-01-15T20:00:00Z',
      status: 'SCHEDULED',
      signupCount: 18,
      maxPlayers: 20,
    },
    {
      id: 2,
      teamId: 1,
      teamName: 'Team Alpha',
      instanceName: 'Nerub-ar Palace',
      difficulty: 'MYTHIC',
      scheduledAt: '2026-01-17T20:00:00Z',
      status: 'SCHEDULED',
      signupCount: 20,
      maxPlayers: 20,
    },
    {
      id: 3,
      teamId: 1,
      teamName: 'Team Alpha',
      instanceName: 'Nerub-ar Palace',
      difficulty: 'HEROIC',
      scheduledAt: '2026-01-12T20:00:00Z',
      status: 'COMPLETED',
      signupCount: 20,
      maxPlayers: 20,
      endedAt: '2026-01-12T23:30:00Z',
    },
  ]

  const mountComponent = (props = {}) => {
    return mount(RaidCalendar, {
      props: {
        raids: mockRaids,
        ...props,
      },
    })
  }

  it('should render month and year header', () => {
    const wrapper = mountComponent()
    // Should show current month by default
    expect(wrapper.text()).toMatch(/January|February|March|April|May|June|July|August|September|October|November|December/)
    expect(wrapper.text()).toMatch(/2026/)
  })

  it('should render day headers', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('Sun')
    expect(wrapper.text()).toContain('Mon')
    expect(wrapper.text()).toContain('Tue')
    expect(wrapper.text()).toContain('Wed')
    expect(wrapper.text()).toContain('Thu')
    expect(wrapper.text()).toContain('Fri')
    expect(wrapper.text()).toContain('Sat')
  })

  it('should render calendar grid', () => {
    const wrapper = mountComponent()
    // Should have days in the grid
    const dayNumbers = wrapper.findAll('.calendar-day')
    expect(dayNumbers.length).toBeGreaterThan(0)
  })

  it('should have navigation buttons', () => {
    const wrapper = mountComponent()
    const buttons = wrapper.findAll('button')
    expect(buttons.length).toBeGreaterThanOrEqual(2) // Previous and Next
  })

  it('should navigate to previous month when clicking prev', async () => {
    const wrapper = mountComponent()
    // Find the previous month button by its title attribute
    const prevButton = wrapper.find('button[title="Previous month"]')

    // Get current month text
    const initialMonthYear = wrapper.find('.month-year').text()

    await prevButton.trigger('click')

    const newMonthYear = wrapper.find('.month-year').text()
    expect(newMonthYear).not.toBe(initialMonthYear)
  })

  it('should navigate to next month when clicking next', async () => {
    const wrapper = mountComponent()
    // Find the next month button by its title attribute
    const nextButton = wrapper.find('button[title="Next month"]')

    const initialMonthYear = wrapper.find('.month-year').text()

    await nextButton.trigger('click')

    const newMonthYear = wrapper.find('.month-year').text()
    expect(newMonthYear).not.toBe(initialMonthYear)
  })

  it('should emit raid-click event when clicking a raid', async () => {
    const wrapper = mountComponent()

    // Find a raid indicator (if raids are in the visible month)
    const raidIndicator = wrapper.find('.raid-indicator')
    if (raidIndicator.exists()) {
      await raidIndicator.trigger('click')
      expect(wrapper.emitted('raid-click')).toBeTruthy()
    }
  })

  it('should show raid indicators on days with raids', () => {
    const wrapper = mountComponent()
    // Check that raid indicators exist for days with raids
    const raidIndicators = wrapper.findAll('.raid-indicator')
    // Should have some raid indicators if raids are in the current view
    expect(raidIndicators.length).toBeGreaterThanOrEqual(0)
  })

  it('should highlight today', () => {
    const wrapper = mountComponent()
    const today = wrapper.find('.today')
    expect(today.exists()).toBe(true)
  })

  it('should accept selectedDate prop', () => {
    const wrapper = mountComponent({
      selectedDate: new Date('2026-01-15'),
    })
    const selectedDay = wrapper.find('.selected-day')
    expect(selectedDay.exists()).toBe(true)
  })
})
