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

describe('GuildSettingsPage', () => {
  const mockGuildId = 'guild-123'
  const mockSyncConfig = {
    guildId: mockGuildId,
    guildName: 'Test Guild',
    wowauditGuildUri: 'eu/realm/guild',
    wowauditBaseUrl: 'https://wowaudit.com',
    wowauditApiKeyConfigured: true,
    syncEnabled: false, // Originally disabled
    lastSyncAt: null,
    lastSyncStatus: null,
    lastSyncError: null,
    bnetRealmSlug: 'realm-slug',
    bnetGuildNameSlug: 'guild-slug',
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
                activeGuild: { id: mockGuildId, guildName: 'Test Guild', role: 'OFFICER' },
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
