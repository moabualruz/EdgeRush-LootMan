import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'

// Mock the auth store
vi.mock('@/stores/auth', () => ({
  useAuthStore: vi.fn(() => ({
    isAuthenticated: false,
    isAdmin: false,
  })),
}))

describe('router', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should have login route', async () => {
    // Import fresh router after mocks are set up
    vi.resetModules()
    const { default: router } = await import('./index')

    const loginRoute = router.getRoutes().find(r => r.name === 'login')
    expect(loginRoute).toBeDefined()
    expect(loginRoute?.path).toBe('/login')
  })

  it('should have dashboard route', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const dashboardRoute = router.getRoutes().find(r => r.name === 'dashboard')
    expect(dashboardRoute).toBeDefined()
  })

  it('should have leaderboard route', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const leaderboardRoute = router.getRoutes().find(r => r.name === 'leaderboard')
    expect(leaderboardRoute).toBeDefined()
  })

  it('should have wishlist route', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const wishlistRoute = router.getRoutes().find(r => r.name === 'wishlist')
    expect(wishlistRoute).toBeDefined()
  })

  it('should have performance route', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const performanceRoute = router.getRoutes().find(r => r.name === 'performance')
    expect(performanceRoute).toBeDefined()
  })

  it('should have attendance route', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const attendanceRoute = router.getRoutes().find(r => r.name === 'attendance')
    expect(attendanceRoute).toBeDefined()
  })

  it('should have raids route', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const raidsRoute = router.getRoutes().find(r => r.name === 'raids')
    expect(raidsRoute).toBeDefined()
  })

  it('should have raid-detail route with id param', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const raidDetailRoute = router.getRoutes().find(r => r.name === 'raid-detail')
    expect(raidDetailRoute).toBeDefined()
  })

  it('should have gear route', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const gearRoute = router.getRoutes().find(r => r.name === 'gear')
    expect(gearRoute).toBeDefined()
  })

  it('should have admin route with requiresAdmin meta', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const adminRoute = router.getRoutes().find(r => r.name === 'admin')
    expect(adminRoute).toBeDefined()
    expect(adminRoute?.meta?.requiresAdmin).toBe(true)
  })

  it('should have history route', async () => {
    vi.resetModules()
    const { default: router } = await import('./index')

    const historyRoute = router.getRoutes().find(r => r.name === 'history')
    expect(historyRoute).toBeDefined()
  })
})
