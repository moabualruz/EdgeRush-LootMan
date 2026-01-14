import { vi } from 'vitest'
import { config } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
Object.defineProperty(window, 'localStorage', { value: localStorageMock })

// Mock import.meta.env
vi.stubGlobal('import.meta', {
  env: {
    VITE_GUILD_ID: 'test-guild',
    VITE_API_URL: 'http://localhost:8080',
  },
})

// Setup Pinia for each test
beforeEach(() => {
  setActivePinia(createPinia())
})

// Global test utilities
config.global.stubs = {
  RouterLink: {
    template: '<a><slot /></a>',
  },
  RouterView: {
    template: '<div data-testid="router-view"><slot /></div>',
  },
}
