import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth'

// Mock the API client
vi.mock('@/api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('should initialize with no user', () => {
    const store = useAuthStore()
    expect(store.user).toBeNull()
    expect(store.isAuthenticated).toBe(false)
  })

  it('should set token and store in localStorage', () => {
    const store = useAuthStore()
    store.setToken('test-token')

    expect(store.token).toBe('test-token')
    expect(localStorage.setItem).toHaveBeenCalledWith('token', 'test-token')
  })

  it('should clear user and token on logout', () => {
    const store = useAuthStore()
    store.setToken('test-token')
    store.logout()

    expect(store.user).toBeNull()
    expect(store.token).toBeNull()
    expect(localStorage.removeItem).toHaveBeenCalledWith('token')
  })

  it('should return false for isAdmin when no user', () => {
    const store = useAuthStore()
    expect(store.isAdmin).toBe(false)
  })

  it('should return true for isAdmin when user is ADMIN', () => {
    const store = useAuthStore()
    store.user = { id: 1, username: 'admin', role: 'ADMIN', linkedCharacters: [] }
    expect(store.isAdmin).toBe(true)
  })

  it('should return true for isAdmin when user is OFFICER', () => {
    const store = useAuthStore()
    store.user = { id: 1, username: 'officer', role: 'OFFICER', linkedCharacters: [] }
    expect(store.isAdmin).toBe(true)
  })

  it('should return false for isAdmin when user is RAIDER', () => {
    const store = useAuthStore()
    store.user = { id: 1, username: 'raider', role: 'RAIDER', linkedCharacters: [] }
    expect(store.isAdmin).toBe(false)
  })

  it('should be authenticated when user and token exist', () => {
    const store = useAuthStore()
    store.setToken('test-token')
    store.user = { id: 1, username: 'test', role: 'RAIDER', linkedCharacters: [] }

    expect(store.isAuthenticated).toBe(true)
  })
})
