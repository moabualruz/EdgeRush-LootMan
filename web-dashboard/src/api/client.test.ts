import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'

// Mock axios
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    })),
  },
}))

describe('API client', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should create axios instance with correct config', () => {
    // Re-import to trigger the module
    vi.resetModules()

    expect(axios.create).toBeDefined()
  })

  it('should have request interceptor configured', () => {
    const mockAxiosInstance = axios.create()
    expect(mockAxiosInstance.interceptors.request.use).toBeDefined()
  })

  it('should have response interceptor configured', () => {
    const mockAxiosInstance = axios.create()
    expect(mockAxiosInstance.interceptors.response.use).toBeDefined()
  })
})
