import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RecentLoot from './RecentLoot.vue'
import type { LootAward } from '@/types'

// Mock the useWowhead composable
vi.mock('@/composables/useWowhead', () => ({
  useWowhead: vi.fn(() => ({
    isLoaded: { value: true },
    error: { value: null },
    refresh: vi.fn(),
  })),
  getWowheadItemUrl: (itemId: number) => `https://www.wowhead.com/item=${itemId}`,
}))

describe('RecentLoot', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const mockAwards: LootAward[] = [
    {
      id: 1,
      itemId: 1001,
      itemName: 'Epic Sword',
      raiderId: 1,
      characterName: 'TestCharacter',
      awardedAt: '2024-01-15T21:00:00Z',
      flpsAtAward: 0.85,
      rdfExpired: false,
      rdfExpiresAt: '2024-01-22T21:00:00Z',
    },
    {
      id: 2,
      itemId: 1002,
      itemName: 'Legendary Helm',
      raiderId: 1,
      characterName: 'TestCharacter',
      awardedAt: '2024-01-10T21:00:00Z',
      flpsAtAward: 0.92,
      rdfExpired: true,
    },
  ]

  it('should render item names', () => {
    const wrapper = mount(RecentLoot, { props: { awards: mockAwards } })
    expect(wrapper.text()).toContain('Epic Sword')
    expect(wrapper.text()).toContain('Legendary Helm')
  })

  it('should display FLPS at award', () => {
    const wrapper = mount(RecentLoot, { props: { awards: mockAwards } })
    expect(wrapper.text()).toContain('0.850')
  })

  it('should show RDF expired badge for expired items', () => {
    const wrapper = mount(RecentLoot, { props: { awards: mockAwards } })
    expect(wrapper.text()).toContain('RDF Expired')
  })

  it('should show RDF time remaining for non-expired items', () => {
    const wrapper = mount(RecentLoot, { props: { awards: mockAwards } })
    expect(wrapper.text()).toContain('RDF:')
  })

  it('should show empty message when no awards', () => {
    const wrapper = mount(RecentLoot, { props: { awards: [] } })
    expect(wrapper.text()).toContain('No recent loot awards')
  })

  it('should apply correct styling for expired items', () => {
    const wrapper = mount(RecentLoot, { props: { awards: mockAwards } })
    expect(wrapper.find('.bg-green-900\\/50').exists()).toBe(true) // RDF Expired
  })

  it('should apply correct styling for non-expired items', () => {
    const wrapper = mount(RecentLoot, { props: { awards: mockAwards } })
    expect(wrapper.find('.bg-yellow-900\\/50').exists()).toBe(true) // RDF Active
  })
})
