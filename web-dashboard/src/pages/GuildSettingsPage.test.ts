import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import { createTestingPinia } from '@pinia/testing'
import GuildSettingsPage from './GuildSettingsPage.vue'
import * as guildSyncApi from '@/api/guildSync'
import * as guildContextApi from '@/api/guildContext'

// Mock the APIs
vi.mock('@/api/guildSync', () => ({
  fetchGuildSyncConfig: vi.fn(),
  updateGuildSyncConfig: vi.fn(),
  triggerBnetSync: vi.fn(),
  triggerWowauditSync: vi.fn(),
}))

vi.mock('@/api/guildContext', () => ({
  fetchGuildPermissions: vi.fn(),
  fetchPermissionTypes: vi.fn(),
  addGuildPermission: vi.fn(),
  removeGuildPermission: vi.fn(),
}))

// Mock Vue Router
const pushMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
}))

// Mock useToast
const toastSuccessMock = vi.fn()
const toastErrorMock = vi.fn()
vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    success: toastSuccessMock,
    error: toastErrorMock,
  }),
}))

describe('GuildSettingsPage', () => {
  const mockGuildId = 'guild-123'
  const mockSyncConfig = {
    guildId: mockGuildId,
    guildName: 'Test Guild',
    wowauditGuildUri: 'eu/twisting-nether/dod',
    wowauditBaseUrl: 'https://wowaudit.com',
    wowauditApiKeyConfigured: true,
    syncEnabled: false, // Originally disabled
    lastSyncAt: null,
    lastSyncStatus: null,
    lastSyncError: null,
    bnetRealmSlug: 'twisting-nether',
    bnetGuildNameSlug: 'dod',
    bnetRegion: 'eu',
    bnetSyncEnabled: false, // Originally disabled
    bnetLastSyncAt: null,
    bnetLastSyncStatus: null,
    bnetLastSyncError: null,
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(GuildSettingsPage, {
      global: {
        plugins: [
          [VueQueryPlugin, { queryClient }],
          createTestingPinia({
            initialState: {
              guildContext: {
                currentGuildId: mockGuildId,
                activeGuild: { 
                  guildId: mockGuildId, 
                  guildName: 'Test Guild', 
                  role: 'OFFICER',
                  permissions: ['SETTINGS_ACCESS']
                },
                userGuilds: [],
                loading: false,
              },
            },
            stubActions: false,
          }),
        ],
        stubs: {
          ConfigEditor: true, // Stub child component
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(guildSyncApi.fetchGuildSyncConfig).mockResolvedValue(mockSyncConfig)
    vi.mocked(guildContextApi.fetchGuildPermissions).mockResolvedValue([])
    vi.mocked(guildContextApi.fetchPermissionTypes).mockResolvedValue([])
  })

  it('should send syncEnabled=true and bnetSyncEnabled=true when saving config', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Simulate user input
    const inputs = wrapper.findAll('input')
    const keyInput = inputs.find(i => i.attributes('type') === 'password')
    expect(keyInput?.exists()).toBe(true)
    await keyInput?.setValue('new-api-key')

    // Find and click save button
    const buttons = wrapper.findAll('button')
    const saveButton = buttons.find(b => b.text().includes('Save All Settings'))
    expect(saveButton?.exists()).toBe(true)
    
    await saveButton?.trigger('click')
    await flushPromises()

    // Check payload
    expect(guildSyncApi.updateGuildSyncConfig).toHaveBeenCalledWith(
      mockGuildId,
      expect.objectContaining({
        syncEnabled: true,
        bnetSyncEnabled: true,
        wowauditApiKey: 'new-api-key'
      })
    )
  })
})
