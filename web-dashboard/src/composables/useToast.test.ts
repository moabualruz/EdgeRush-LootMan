import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useToast } from './useToast'

describe('useToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    // Clear all toasts before each test
    const { dismissAll } = useToast()
    dismissAll()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  describe('basic functionality', () => {
    it('should add a toast with success type', () => {
      const { success, toasts } = useToast()

      success('Test Success')

      expect(toasts.value).toHaveLength(1)
      expect(toasts.value[0].type).toBe('success')
      expect(toasts.value[0].title).toBe('Test Success')
    })

    it('should add a toast with error type', () => {
      const { error, toasts } = useToast()

      error('Test Error', 'Error details')

      expect(toasts.value).toHaveLength(1)
      expect(toasts.value[0].type).toBe('error')
      expect(toasts.value[0].title).toBe('Test Error')
      expect(toasts.value[0].message).toBe('Error details')
    })

    it('should add a toast with warning type', () => {
      const { warning, toasts } = useToast()

      warning('Test Warning')

      expect(toasts.value).toHaveLength(1)
      expect(toasts.value[0].type).toBe('warning')
    })

    it('should add a toast with info type', () => {
      const { info, toasts } = useToast()

      info('Test Info')

      expect(toasts.value).toHaveLength(1)
      expect(toasts.value[0].type).toBe('info')
    })

    it('should add a custom toast with show()', () => {
      const { show, toasts } = useToast()

      show({
        type: 'success',
        title: 'Custom Toast',
        message: 'Custom message',
        duration: 3000,
        dismissible: false,
      })

      expect(toasts.value).toHaveLength(1)
      expect(toasts.value[0].dismissible).toBe(false)
      expect(toasts.value[0].duration).toBe(3000)
    })
  })

  describe('dismiss functionality', () => {
    it('should dismiss a toast by ID', () => {
      const { success, dismiss, toasts } = useToast()

      const id = success('Test')
      expect(toasts.value).toHaveLength(1)

      dismiss(id)
      expect(toasts.value).toHaveLength(0)
    })

    it('should dismiss all toasts', () => {
      const { success, error, dismissAll, toasts } = useToast()

      success('Toast 1')
      error('Toast 2')
      success('Toast 3')

      expect(toasts.value).toHaveLength(3)

      dismissAll()
      expect(toasts.value).toHaveLength(0)
    })

    it('should not throw when dismissing non-existent toast', () => {
      const { dismiss } = useToast()

      expect(() => dismiss('non-existent-id')).not.toThrow()
    })
  })

  describe('auto-dismiss', () => {
    it('should auto-dismiss after duration', () => {
      const { success, toasts } = useToast()

      success('Test', undefined, 3000)
      expect(toasts.value).toHaveLength(1)

      vi.advanceTimersByTime(3000)
      expect(toasts.value).toHaveLength(0)
    })

    it('should use default duration for success', () => {
      const { success, toasts } = useToast()

      success('Test')

      // Default duration is 5000ms
      vi.advanceTimersByTime(4999)
      expect(toasts.value).toHaveLength(1)

      vi.advanceTimersByTime(1)
      expect(toasts.value).toHaveLength(0)
    })

    it('should use longer duration for errors by default', () => {
      const { error, toasts } = useToast()

      error('Test')

      // Error default duration is 8000ms
      vi.advanceTimersByTime(7999)
      expect(toasts.value).toHaveLength(1)

      vi.advanceTimersByTime(1)
      expect(toasts.value).toHaveLength(0)
    })
  })

  describe('queue management', () => {
    it('should limit visible toasts to max', () => {
      const { success, toasts } = useToast()

      // Add 7 toasts (max is 5)
      for (let i = 0; i < 7; i++) {
        success(`Toast ${i}`)
      }

      expect(toasts.value).toHaveLength(5)
      // Should keep the most recent ones
      expect(toasts.value[0].title).toBe('Toast 2')
      expect(toasts.value[4].title).toBe('Toast 6')
    })
  })

  describe('toast properties', () => {
    it('should generate unique IDs', () => {
      const { success, toasts } = useToast()

      success('Toast 1')
      success('Toast 2')

      expect(toasts.value[0].id).not.toBe(toasts.value[1].id)
    })

    it('should include createdAt timestamp', () => {
      const { success, toasts } = useToast()

      const before = Date.now()
      success('Test')
      const after = Date.now()

      expect(toasts.value[0].createdAt).toBeGreaterThanOrEqual(before)
      expect(toasts.value[0].createdAt).toBeLessThanOrEqual(after)
    })

    it('should return toast ID from add functions', () => {
      const { success, error, warning, info } = useToast()

      const id1 = success('Test 1')
      const id2 = error('Test 2')
      const id3 = warning('Test 3')
      const id4 = info('Test 4')

      expect(id1).toMatch(/^toast-/)
      expect(id2).toMatch(/^toast-/)
      expect(id3).toMatch(/^toast-/)
      expect(id4).toMatch(/^toast-/)
    })
  })
})
