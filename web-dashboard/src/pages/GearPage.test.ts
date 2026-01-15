import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import GearPage from './GearPage.vue'
import { gearApi, type RaiderGear, type VaultOptions, type GearItem, type VaultSlot } from '@/api/gear'

// Mock the APIs
vi.mock('@/api/gear', () => ({
  gearApi: {
    getMyGear: vi.fn(),
    getMyVaultOptions: vi.fn(),
  },
}))

describe('GearPage', () => {
  const mockGearItems: GearItem[] = [
    {
      id: 1,
      itemId: 12345,
      itemName: 'Helm of the Fallen Hero',
      slot: 'HEAD',
      itemLevel: 639,
      quality: 'EPIC',
      enchantId: 100,
      enchantName: 'Incandescent Essence',
      gems: [],
      socketCount: 0,
      isTierPiece: true,
      upgradeLevel: 2,
      maxUpgradeLevel: 4,
    },
    {
      id: 2,
      itemId: 12346,
      itemName: 'Cloak of Shadows',
      slot: 'BACK',
      itemLevel: 626,
      quality: 'EPIC',
      gems: [],
      socketCount: 0,
      isTierPiece: false,
    },
    {
      id: 3,
      itemId: 12347,
      itemName: 'Ring of Power',
      slot: 'FINGER_1',
      itemLevel: 620,
      quality: 'EPIC',
      gems: [{ gemId: 1, gemName: 'Masterful Jewel', color: 'Blue' }],
      socketCount: 1,
      isTierPiece: false,
    },
  ]

  const mockGearData: RaiderGear = {
    raiderId: 1,
    characterName: 'TestRaider',
    averageItemLevel: 630.5,
    equippedItemLevel: 628.2,
    items: mockGearItems,
    missingEnchants: ['BACK'],
    missingGems: [],
    tierPieceCount: 2,
    lastUpdated: '2026-01-15T12:00:00Z',
  }

  const mockVaultSlots: VaultSlot[] = [
    { id: 1, raiderId: 1, slotType: 'RAID', slotIndex: 0, itemName: 'Vault Helm', itemLevel: 639, isSelected: false, unlocked: true, progress: 2, progressRequired: 2 },
    { id: 2, raiderId: 1, slotType: 'RAID', slotIndex: 1, unlocked: false, isSelected: false, progress: 1, progressRequired: 4 },
    { id: 3, raiderId: 1, slotType: 'RAID', slotIndex: 2, unlocked: false, isSelected: false, progress: 0, progressRequired: 6 },
  ]

  const mockVaultData: VaultOptions = {
    raiderId: 1,
    weekOf: '2026-01-13',
    raid: mockVaultSlots,
    mythicPlus: [
      { id: 4, raiderId: 1, slotType: 'MYTHIC_PLUS', slotIndex: 0, unlocked: true, isSelected: false, progress: 1, progressRequired: 1 },
      { id: 5, raiderId: 1, slotType: 'MYTHIC_PLUS', slotIndex: 1, unlocked: false, isSelected: false, progress: 2, progressRequired: 4 },
      { id: 6, raiderId: 1, slotType: 'MYTHIC_PLUS', slotIndex: 2, unlocked: false, isSelected: false, progress: 0, progressRequired: 8 },
    ],
    pvp: [
      { id: 7, raiderId: 1, slotType: 'PVP', slotIndex: 0, unlocked: false, isSelected: false, progress: 0, progressRequired: 1250 },
      { id: 8, raiderId: 1, slotType: 'PVP', slotIndex: 1, unlocked: false, isSelected: false, progress: 0, progressRequired: 2500 },
      { id: 9, raiderId: 1, slotType: 'PVP', slotIndex: 2, unlocked: false, isSelected: false, progress: 0, progressRequired: 5000 },
    ],
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(GearPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(gearApi.getMyGear).mockResolvedValue(mockGearData)
    vi.mocked(gearApi.getMyVaultOptions).mockResolvedValue(mockVaultData)
  })

  it('should render page title "Gear"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Gear')
  })

  it('should display character name', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('TestRaider')
  })

  it('should show equipped gear by slot', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Equipped Gear')
    expect(wrapper.text()).toContain('Helm of the Fallen Hero')
    expect(wrapper.text()).toContain('Cloak of Shadows')
    expect(wrapper.text()).toContain('Ring of Power')
  })

  it('should display Great Vault options', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Great Vault Options')
    expect(wrapper.text()).toContain('Raid')
    expect(wrapper.text()).toContain('Mythic+')
    expect(wrapper.text()).toContain('PvP')
  })

  it('should highlight missing enchants with warning', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Equipment Issues')
    expect(wrapper.text()).toContain('Missing enchant on Back')
  })

  it('should show item level', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('639')
    expect(wrapper.text()).toContain('626')
    expect(wrapper.text()).toContain('620')
  })

  it('should display equipped and average item level in summary', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('628.2')
    expect(wrapper.text()).toContain('Equipped iLvl')
    expect(wrapper.text()).toContain('630.5')
    expect(wrapper.text()).toContain('Average iLvl')
  })

  it('should show tier piece count', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('2/5')
    expect(wrapper.text()).toContain('Tier Pieces')
  })

  it('should mark tier pieces with [T] indicator', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('[T]')
  })

  it('should show loading state', () => {
    vi.mocked(gearApi.getMyGear).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(gearApi.getMyGear).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load gear data')
  })

  it('should show missing enchants/gems count', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('1')
    expect(wrapper.text()).toContain('Missing Enchants/Gems')
  })

  it('should not show warning when no missing enchants/gems', async () => {
    vi.mocked(gearApi.getMyGear).mockResolvedValue({
      ...mockGearData,
      missingEnchants: [],
      missingGems: [],
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).not.toContain('Equipment Issues')
  })

  it('should display vault unlock progress', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Should show locked slots with progress
    expect(wrapper.text()).toContain('Locked')
    expect(wrapper.text()).toContain('1/4')
  })
})
