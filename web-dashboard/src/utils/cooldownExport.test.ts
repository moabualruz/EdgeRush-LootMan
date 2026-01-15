import { describe, it, expect } from 'vitest'
import { generateMRTNote, generateWeakAuraData, formatTime } from './cooldownExport'
import type { ExportAssignment } from './cooldownExport'

describe('cooldownExport', () => {
  const sampleAssignments: ExportAssignment[] = [
    {
      playerName: 'Healmaster',
      playerClass: 'PRIEST',
      cooldownName: 'Divine Hymn',
      cooldownSpellId: 64843,
      abilityName: 'Silken Tomb',
      time: 25,
    },
    {
      playerName: 'Treehugger',
      playerClass: 'DRUID',
      cooldownName: 'Tranquility',
      cooldownSpellId: 740,
      abilityName: 'Silken Tomb',
      time: 25,
    },
    {
      playerName: 'Healbot',
      playerClass: 'PALADIN',
      cooldownName: 'Aura Mastery',
      cooldownSpellId: 31821,
      abilityName: 'Royal Condemnation',
      time: 90,
    },
  ]

  describe('formatTime', () => {
    it('should format seconds to mm:ss', () => {
      expect(formatTime(0)).toBe('0:00')
      expect(formatTime(25)).toBe('0:25')
      expect(formatTime(60)).toBe('1:00')
      expect(formatTime(90)).toBe('1:30')
      expect(formatTime(125)).toBe('2:05')
    })
  })

  describe('generateMRTNote', () => {
    it('should generate empty note for no assignments', () => {
      const note = generateMRTNote([])
      expect(note).toContain('Cooldown Assignments')
    })

    it('should group assignments by ability', () => {
      const note = generateMRTNote(sampleAssignments)

      expect(note).toContain('Silken Tomb')
      expect(note).toContain('Royal Condemnation')
    })

    it('should include spell IDs in MRT format', () => {
      const note = generateMRTNote(sampleAssignments)

      expect(note).toContain('{spell:64843}')
      expect(note).toContain('{spell:740}')
      expect(note).toContain('{spell:31821}')
    })

    it('should include player names', () => {
      const note = generateMRTNote(sampleAssignments)

      expect(note).toContain('Healmaster')
      expect(note).toContain('Treehugger')
      expect(note).toContain('Healbot')
    })

    it('should format timestamps correctly', () => {
      const note = generateMRTNote(sampleAssignments)

      expect(note).toContain('0:25')
      expect(note).toContain('1:30')
    })

    it('should sort by time', () => {
      const note = generateMRTNote(sampleAssignments)
      const lines = note.split('\n')

      const silkenIndex = lines.findIndex((l) => l.includes('Silken Tomb'))
      const royalIndex = lines.findIndex((l) => l.includes('Royal Condemnation'))

      expect(silkenIndex).toBeLessThan(royalIndex)
    })
  })

  describe('generateWeakAuraData', () => {
    it('should generate valid JSON', () => {
      const data = generateWeakAuraData(sampleAssignments)

      expect(() => JSON.parse(data)).not.toThrow()
    })

    it('should include assignments array', () => {
      const data = generateWeakAuraData(sampleAssignments)
      const parsed = JSON.parse(data)

      expect(parsed.assignments).toBeDefined()
      expect(parsed.assignments).toHaveLength(3)
    })

    it('should include spell IDs in assignments', () => {
      const data = generateWeakAuraData(sampleAssignments)
      const parsed = JSON.parse(data)

      expect(parsed.assignments[0].spellId).toBe(64843)
    })

    it('should include player names', () => {
      const data = generateWeakAuraData(sampleAssignments)
      const parsed = JSON.parse(data)

      expect(parsed.assignments[0].playerName).toBe('Healmaster')
    })

    it('should include timestamps', () => {
      const data = generateWeakAuraData(sampleAssignments)
      const parsed = JSON.parse(data)

      expect(parsed.assignments[0].time).toBe(25)
    })

    it('should include version info', () => {
      const data = generateWeakAuraData(sampleAssignments)
      const parsed = JSON.parse(data)

      expect(parsed.version).toBe(1)
      expect(parsed.generator).toBe('LootMan')
    })

    it('should generate empty data for no assignments', () => {
      const data = generateWeakAuraData([])
      const parsed = JSON.parse(data)

      expect(parsed.assignments).toHaveLength(0)
    })
  })
})
