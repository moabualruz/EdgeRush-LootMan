import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'
import DiscordConfigPage from './DiscordConfigPage.vue'
import {
  discordApi,
  type DiscordNotificationConfig,
  type GuildNotificationConfigsResponse,
} from '@/api/discord'

// Mock the APIs
vi.mock('@/api/discord', () => ({
  discordApi: {
    getConfigs: vi.fn(),
    upsertConfig: vi.fn(),
    updateConfig: vi.fn(),
    deleteConfig: vi.fn(),
    testNotification: vi.fn(),
  },
}))

describe('DiscordConfigPage', () => {
  const mockConfigs: DiscordNotificationConfig[] = [
    {
      id: 1,
      guildId: 'test-guild',
      discordServerId: '123456789012345678',
      notificationType: 'LOOT_AWARD',
      channelId: '987654321098765432',
      enabled: true,
      mentionRoleId: '111222333444555666',
      createdAt: '2026-01-10T10:00:00Z',
      updatedAt: null,
    },
    {
      id: 2,
      guildId: 'test-guild',
      discordServerId: '123456789012345678',
      notificationType: 'RDF_EXPIRY',
      channelId: '555666777888999000',
      enabled: false,
      mentionRoleId: null,
      createdAt: '2026-01-08T14:00:00Z',
      updatedAt: '2026-01-09T10:00:00Z',
    },
  ]

  const mockConfigsResponse: GuildNotificationConfigsResponse = {
    guildId: 'test-guild',
    configs: mockConfigs,
    availableTypes: ['LOOT_AWARD', 'RDF_EXPIRY', 'PENALTY', 'LOOT_BAN', 'SYNC_COMPLETE'],
  }

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(DiscordConfigPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          Teleport: true,
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(discordApi.getConfigs).mockResolvedValue(mockConfigsResponse)
  })

  it('should render page title "Discord Notifications"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('Discord Notifications')
  })

  it('should display subtitle description', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Configure which Discord channels receive notifications')
  })

  it('should display notification configs list', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('LOOT AWARD')
    expect(wrapper.text()).toContain('RDF EXPIRY')
  })

  it('should display channel IDs for each config', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('987654321098765432')
    expect(wrapper.text()).toContain('555666777888999000')
  })

  it('should show "Disabled" badge for disabled configs', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Disabled')
  })

  it('should display notification type descriptions', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('When loot is awarded to a raider')
    expect(wrapper.text()).toContain('When RDF penalty expires for a raider')
  })

  it('should show action buttons for each config', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Test')
    expect(wrapper.text()).toContain('Edit')
    expect(wrapper.text()).toContain('Delete')
  })

  it('should show Enable/Disable toggle button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Disable')
    expect(wrapper.text()).toContain('Enable')
  })

  it('should display mention role ID when configured', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Mention Role')
    expect(wrapper.text()).toContain('111222333444555666')
  })

  it('should show Add Configuration button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Add Configuration')
  })

  it('should show Refresh button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Refresh')
  })

  it('should display unconfigured notification types', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Available notification types not yet configured')
    expect(wrapper.text()).toContain('PENALTY')
    expect(wrapper.text()).toContain('LOOT BAN')
    expect(wrapper.text()).toContain('SYNC COMPLETE')
  })

  it('should show loading state', () => {
    vi.mocked(discordApi.getConfigs).mockImplementation(() => new Promise(() => {}))

    const wrapper = mountComponent()

    expect(wrapper.find('.animate-spin').exists()).toBe(true)
  })

  it('should show error state when API fails', async () => {
    vi.mocked(discordApi.getConfigs).mockRejectedValue(new Error('API Error'))

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Failed to load configurations')
  })

  it('should show empty state when no configs', async () => {
    vi.mocked(discordApi.getConfigs).mockResolvedValue({
      guildId: 'test-guild',
      configs: [],
      availableTypes: ['LOOT_AWARD', 'RDF_EXPIRY', 'PENALTY', 'LOOT_BAN', 'SYNC_COMPLETE'],
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('No notification configurations set up yet')
    expect(wrapper.text()).toContain('Create First Configuration')
  })

  it('should hide Add Configuration button when all types are configured', async () => {
    vi.mocked(discordApi.getConfigs).mockResolvedValue({
      guildId: 'test-guild',
      configs: [
        { ...mockConfigs[0], notificationType: 'LOOT_AWARD' },
        { ...mockConfigs[0], id: 2, notificationType: 'RDF_EXPIRY' },
        { ...mockConfigs[0], id: 3, notificationType: 'PENALTY' },
        { ...mockConfigs[0], id: 4, notificationType: 'LOOT_BAN' },
        { ...mockConfigs[0], id: 5, notificationType: 'SYNC_COMPLETE' },
      ],
      availableTypes: ['LOOT_AWARD', 'RDF_EXPIRY', 'PENALTY', 'LOOT_BAN', 'SYNC_COMPLETE'],
    })

    const wrapper = mountComponent()
    await flushPromises()

    // When all types are configured, there should be no "Add Configuration" button in the header
    const buttons = wrapper.findAll('button')
    const addButton = buttons.find((b) => b.text() === 'Add Configuration')
    expect(addButton).toBeUndefined()
  })

  it('should use correct color for LOOT_AWARD notification type', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const lootAwardElement = wrapper.find('.text-green-400')
    expect(lootAwardElement.exists()).toBe(true)
  })

  it('should show green indicator for enabled configs', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const greenIndicators = wrapper.findAll('.bg-green-500')
    expect(greenIndicators.length).toBeGreaterThan(0)
  })

  it('should show gray indicator for disabled configs', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const grayIndicators = wrapper.findAll('.bg-gray-500')
    expect(grayIndicators.length).toBeGreaterThan(0)
  })
})
