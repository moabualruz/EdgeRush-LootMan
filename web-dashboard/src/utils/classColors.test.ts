import { describe, it, expect } from 'vitest'
import { getClassColor, getClassBackgroundColor } from './classColors'
import type { CharacterClass } from '@/types'

describe('classColors utilities', () => {
  describe('getClassColor', () => {
    it('should return correct color for each class', () => {
      expect(getClassColor('WARRIOR')).toBe('text-class-warrior')
      expect(getClassColor('PALADIN')).toBe('text-class-paladin')
      expect(getClassColor('HUNTER')).toBe('text-class-hunter')
      expect(getClassColor('ROGUE')).toBe('text-class-rogue')
      expect(getClassColor('PRIEST')).toBe('text-class-priest')
      expect(getClassColor('DEATH_KNIGHT')).toBe('text-class-deathknight')
      expect(getClassColor('SHAMAN')).toBe('text-class-shaman')
      expect(getClassColor('MAGE')).toBe('text-class-mage')
      expect(getClassColor('WARLOCK')).toBe('text-class-warlock')
      expect(getClassColor('MONK')).toBe('text-class-monk')
      expect(getClassColor('DRUID')).toBe('text-class-druid')
      expect(getClassColor('DEMON_HUNTER')).toBe('text-class-demonhunter')
      expect(getClassColor('EVOKER')).toBe('text-class-evoker')
    })

    it('should return white for unknown class', () => {
      // @ts-expect-error testing unknown class
      expect(getClassColor('UNKNOWN')).toBe('text-white')
    })
  })

  describe('getClassBackgroundColor', () => {
    it('should convert text color to background color', () => {
      expect(getClassBackgroundColor('WARRIOR')).toBe('bg-class-warrior/20')
      expect(getClassBackgroundColor('MAGE')).toBe('bg-class-mage/20')
    })
  })
})
