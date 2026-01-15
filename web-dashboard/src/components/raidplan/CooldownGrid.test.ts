import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CooldownGrid from './CooldownGrid.vue'

// Types for cooldown grid
interface RosterMember {
  id: number
  name: string
  class: string
  spec: string
  role: 'TANK' | 'HEALER' | 'DPS'
}

interface Cooldown {
  id: string
  name: string
  spellId: number
  duration: number
  cooldownTime: number
  icon: string
}

interface BossAbility {
  id: string
  name: string
  time: number
  damage: 'HIGH' | 'MEDIUM' | 'LOW'
  requiresCooldown: boolean
}

interface CooldownAssignment {
  playerId: number
  cooldownId: string
  abilityId: string
  time: number
}

describe('CooldownGrid', () => {
  const mockRoster: RosterMember[] = [
    { id: 1, name: 'Tankyboi', class: 'WARRIOR', spec: 'Protection', role: 'TANK' },
    { id: 2, name: 'Healmaster', class: 'PRIEST', spec: 'Holy', role: 'HEALER' },
    { id: 3, name: 'Healbot', class: 'PALADIN', spec: 'Holy', role: 'HEALER' },
    { id: 4, name: 'Bigdps', class: 'MAGE', spec: 'Fire', role: 'DPS' },
  ]

  const mockCooldowns: Record<string, Cooldown[]> = {
    WARRIOR: [
      { id: 'rallying-cry', name: 'Rallying Cry', spellId: 97462, duration: 10, cooldownTime: 180, icon: 'ability_warrior_rallyingcry' },
    ],
    PRIEST: [
      { id: 'divine-hymn', name: 'Divine Hymn', spellId: 64843, duration: 8, cooldownTime: 180, icon: 'spell_holy_divinehymn' },
      { id: 'barrier', name: 'Power Word: Barrier', spellId: 62618, duration: 10, cooldownTime: 180, icon: 'spell_holy_powerwordbarrier' },
    ],
    PALADIN: [
      { id: 'aura-mastery', name: 'Aura Mastery', spellId: 31821, duration: 8, cooldownTime: 180, icon: 'spell_holy_auramastery' },
      { id: 'divine-toll', name: 'Divine Toll', spellId: 375576, duration: 0, cooldownTime: 60, icon: 'ability_bastion_paladin' },
    ],
    MAGE: [],
  }

  const mockBossAbilities: BossAbility[] = [
    { id: 'ability-1', name: 'Massive AoE', time: 30, damage: 'HIGH', requiresCooldown: true },
    { id: 'ability-2', name: 'Raid Damage', time: 60, damage: 'MEDIUM', requiresCooldown: true },
    { id: 'ability-3', name: 'Heavy Hit', time: 90, damage: 'HIGH', requiresCooldown: true },
    { id: 'ability-4', name: 'Enrage', time: 120, damage: 'HIGH', requiresCooldown: true },
  ]

  const defaultProps = {
    roster: mockRoster,
    cooldowns: mockCooldowns,
    bossAbilities: mockBossAbilities,
    assignments: [] as CooldownAssignment[],
    fightDuration: 180,
  }

  describe('Rendering', () => {
    it('should render cooldown grid container', () => {
      const wrapper = mount(CooldownGrid, { props: defaultProps })
      expect(wrapper.find('[data-testid="cooldown-grid"]').exists()).toBe(true)
    })

    it('should display roster with class colors', () => {
      const wrapper = mount(CooldownGrid, { props: defaultProps })

      const rosterRows = wrapper.findAll('[data-testid="roster-row"]')
      expect(rosterRows).toHaveLength(4)

      // Check class colors are applied
      expect(wrapper.html()).toContain('Tankyboi')
      expect(wrapper.html()).toContain('Healmaster')
    })

    it('should show available cooldowns per player', () => {
      const wrapper = mount(CooldownGrid, { props: defaultProps })

      // Priest should have 2 cooldowns shown
      const priestRow = wrapper.findAll('[data-testid="roster-row"]')[1]
      const cooldownButtons = priestRow.findAll('[data-testid="cooldown-button"]')
      expect(cooldownButtons.length).toBeGreaterThanOrEqual(2)
    })

    it('should show boss ability timeline', () => {
      const wrapper = mount(CooldownGrid, { props: defaultProps })

      const timeline = wrapper.find('[data-testid="boss-timeline"]')
      expect(timeline.exists()).toBe(true)

      const abilityMarkers = wrapper.findAll('[data-testid="ability-marker"]')
      expect(abilityMarkers).toHaveLength(4)
    })

    it('should show time markers on timeline', () => {
      const wrapper = mount(CooldownGrid, { props: defaultProps })

      expect(wrapper.text()).toContain('0:30')
      expect(wrapper.text()).toContain('1:00')
      expect(wrapper.text()).toContain('1:30')
    })
  })

  describe('Cooldown Assignment', () => {
    it('should emit assign-cooldown when cooldown dropped on timeline', async () => {
      const wrapper = mount(CooldownGrid, { props: defaultProps })

      // Simulate drag start on a cooldown button
      const cooldownButton = wrapper.find('[data-testid="cooldown-button"]')
      await cooldownButton.trigger('dragstart')

      // Simulate drop on ability
      const abilityMarker = wrapper.find('[data-testid="ability-marker"]')
      await abilityMarker.trigger('drop')

      expect(wrapper.emitted('assign-cooldown')).toBeTruthy()
    })

    it('should display assigned cooldowns on timeline', () => {
      const assignments: CooldownAssignment[] = [
        { playerId: 2, cooldownId: 'divine-hymn', abilityId: 'ability-1', time: 30 },
      ]

      const wrapper = mount(CooldownGrid, {
        props: { ...defaultProps, assignments },
      })

      const assignedCooldowns = wrapper.findAll('[data-testid="assigned-cooldown"]')
      expect(assignedCooldowns).toHaveLength(1)
    })

    it('should emit remove-assignment when assigned cooldown clicked', async () => {
      const assignments: CooldownAssignment[] = [
        { playerId: 2, cooldownId: 'divine-hymn', abilityId: 'ability-1', time: 30 },
      ]

      const wrapper = mount(CooldownGrid, {
        props: { ...defaultProps, assignments },
      })

      const assignedCooldown = wrapper.find('[data-testid="assigned-cooldown"]')
      await assignedCooldown.trigger('click')

      expect(wrapper.emitted('remove-assignment')).toBeTruthy()
    })
  })

  describe('Cooldown Validation', () => {
    it('should show warning on overlapping cooldowns', () => {
      const assignments: CooldownAssignment[] = [
        { playerId: 2, cooldownId: 'divine-hymn', abilityId: 'ability-1', time: 30 },
        { playerId: 2, cooldownId: 'divine-hymn', abilityId: 'ability-2', time: 60 }, // Too soon - 180s CD
      ]

      const wrapper = mount(CooldownGrid, {
        props: { ...defaultProps, assignments },
      })

      const warnings = wrapper.findAll('[data-testid="cooldown-warning"]')
      expect(warnings.length).toBeGreaterThan(0)
    })

    it('should highlight abilities without cooldown coverage', () => {
      const wrapper = mount(CooldownGrid, {
        props: { ...defaultProps, assignments: [] },
      })

      const uncoveredAbilities = wrapper.findAll('[data-testid="ability-marker"].uncovered')
      expect(uncoveredAbilities.length).toBeGreaterThan(0)
    })
  })

  describe('Role Filtering', () => {
    it('should filter roster by role', async () => {
      const wrapper = mount(CooldownGrid, { props: defaultProps })

      const filterButton = wrapper.find('[data-testid="filter-healers"]')
      await filterButton.trigger('click')

      const visibleRows = wrapper.findAll('[data-testid="roster-row"]:not(.hidden)')
      expect(visibleRows.length).toBeLessThan(4)
    })
  })

  describe('Export Functionality', () => {
    it('should show export button', () => {
      const wrapper = mount(CooldownGrid, { props: defaultProps })
      expect(wrapper.find('[data-testid="export-button"]').exists()).toBe(true)
    })

    it('should emit export-mrt when MRT export clicked', async () => {
      const assignments: CooldownAssignment[] = [
        { playerId: 2, cooldownId: 'divine-hymn', abilityId: 'ability-1', time: 30 },
      ]

      const wrapper = mount(CooldownGrid, {
        props: { ...defaultProps, assignments },
      })

      await wrapper.find('[data-testid="export-button"]').trigger('click')
      await wrapper.find('[data-testid="export-mrt"]').trigger('click')

      expect(wrapper.emitted('export-mrt')).toBeTruthy()
    })

    it('should emit export-weakaura when WeakAura export clicked', async () => {
      const assignments: CooldownAssignment[] = [
        { playerId: 2, cooldownId: 'divine-hymn', abilityId: 'ability-1', time: 30 },
      ]

      const wrapper = mount(CooldownGrid, {
        props: { ...defaultProps, assignments },
      })

      await wrapper.find('[data-testid="export-button"]').trigger('click')
      await wrapper.find('[data-testid="export-weakaura"]').trigger('click')

      expect(wrapper.emitted('export-weakaura')).toBeTruthy()
    })
  })

  describe('Empty States', () => {
    it('should show message when roster is empty', () => {
      const wrapper = mount(CooldownGrid, {
        props: { ...defaultProps, roster: [] },
      })

      expect(wrapper.text()).toContain('No roster')
    })

    it('should show message when no boss abilities defined', () => {
      const wrapper = mount(CooldownGrid, {
        props: { ...defaultProps, bossAbilities: [] },
      })

      expect(wrapper.text()).toContain('No boss abilities')
    })
  })
})
