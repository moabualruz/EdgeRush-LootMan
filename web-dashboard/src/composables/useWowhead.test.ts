import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref, nextTick } from 'vue'
import {
  useWowhead,
  getWowheadItemUrl,
  getWowheadSpellUrl,
  getWowheadNpcUrl,
  getWowheadAchievementUrl,
} from './useWowhead'

// Mock the DOM environment
const mockScript = {
  onload: null as (() => void) | null,
  onerror: null as (() => void) | null,
  src: '',
  async: false,
}

describe('useWowhead', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Reset window mocks
    delete (window as any).$WowheadPower
    delete (window as any).whTooltips

    // Mock createElement for script
    vi.spyOn(document, 'createElement').mockImplementation((tag: string) => {
      if (tag === 'script') {
        return mockScript as unknown as HTMLScriptElement
      }
      return document.createElement(tag)
    })

    vi.spyOn(document.head, 'appendChild').mockImplementation((node: Node) => {
      // Simulate script loading
      setTimeout(() => {
        if (mockScript.onload) mockScript.onload()
      }, 0)
      return node
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('getWowheadItemUrl', () => {
    it('should generate basic item URL', () => {
      const url = getWowheadItemUrl(12345)
      expect(url).toBe('https://www.wowhead.com/item=12345')
    })

    it('should generate item URL with bonus IDs', () => {
      const url = getWowheadItemUrl(12345, [1, 2, 3])
      expect(url).toBe('https://www.wowhead.com/item=12345?bonus=1:2:3')
    })

    it('should handle empty bonus array', () => {
      const url = getWowheadItemUrl(12345, [])
      expect(url).toBe('https://www.wowhead.com/item=12345')
    })
  })

  describe('getWowheadSpellUrl', () => {
    it('should generate spell URL', () => {
      const url = getWowheadSpellUrl(54321)
      expect(url).toBe('https://www.wowhead.com/spell=54321')
    })
  })

  describe('getWowheadNpcUrl', () => {
    it('should generate NPC URL', () => {
      const url = getWowheadNpcUrl(99999)
      expect(url).toBe('https://www.wowhead.com/npc=99999')
    })
  })

  describe('getWowheadAchievementUrl', () => {
    it('should generate achievement URL', () => {
      const url = getWowheadAchievementUrl(11111)
      expect(url).toBe('https://www.wowhead.com/achievement=11111')
    })
  })
})

describe('Wowhead URL generators', () => {
  it('should handle large item IDs', () => {
    const url = getWowheadItemUrl(999999999)
    expect(url).toBe('https://www.wowhead.com/item=999999999')
  })

  it('should handle multiple bonus IDs', () => {
    const bonusIds = [1808, 6652, 7193, 6646]
    const url = getWowheadItemUrl(207172, bonusIds)
    expect(url).toBe('https://www.wowhead.com/item=207172?bonus=1808:6652:7193:6646')
  })
})
