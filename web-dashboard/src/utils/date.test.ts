import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { formatDate, formatDateTime, formatRelativeTime, isExpired } from './date'

describe('date utilities', () => {
  describe('formatDate', () => {
    it('should format date correctly', () => {
      const result = formatDate('2024-01-15T10:00:00Z')
      expect(result).toMatch(/Jan 15, 2024/)
    })

    it('should handle different dates', () => {
      const result = formatDate('2023-12-25T00:00:00Z')
      expect(result).toMatch(/Dec 2[45], 2023/) // Account for timezone differences
    })
  })

  describe('formatDateTime', () => {
    it('should format date and time correctly', () => {
      const result = formatDateTime('2024-01-15T14:30:00Z')
      expect(result).toContain('Jan')
      expect(result).toContain('2024')
    })
  })

  describe('formatRelativeTime', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should return "Expired" for past dates', () => {
      const result = formatRelativeTime('2024-01-10T12:00:00Z')
      expect(result).toBe('Expired')
    })

    it('should return "Today" for same moment', () => {
      // Exact same time gives diffDays = 0
      const result = formatRelativeTime('2024-01-15T12:00:00Z')
      expect(result).toBe('Today')
    })

    it('should return "Tomorrow" for next day', () => {
      const result = formatRelativeTime('2024-01-16T12:00:00Z')
      expect(result).toBe('Tomorrow')
    })

    it('should return days for less than a week', () => {
      const result = formatRelativeTime('2024-01-20T12:00:00Z')
      expect(result).toBe('5 days')
    })

    it('should return weeks for less than a month', () => {
      const result = formatRelativeTime('2024-01-29T12:00:00Z')
      expect(result).toBe('2 weeks')
    })

    it('should return months for longer periods', () => {
      const result = formatRelativeTime('2024-03-15T12:00:00Z')
      expect(result).toBe('2 months')
    })
  })

  describe('isExpired', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should return true for past dates', () => {
      expect(isExpired('2024-01-10T12:00:00Z')).toBe(true)
    })

    it('should return false for future dates', () => {
      expect(isExpired('2024-01-20T12:00:00Z')).toBe(false)
    })
  })
})
