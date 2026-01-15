import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import LoginPage from './LoginPage.vue'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
  useRoute: () => ({
    query: {},
  }),
}))

describe('LoginPage', () => {
  const mountComponent = () => {
    return mount(LoginPage, {
      global: {
        stubs: {
          // No stubs needed for this simple page
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('should render login page title "LootMan"', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('h1').text()).toBe('LootMan')
  })

  it('should render sign in heading', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Sign in to continue')
  })

  it('should have Discord OAuth button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const discordButton = wrapper.findAll('button').find((b) => b.text().includes('Discord'))
    expect(discordButton).toBeDefined()
    expect(discordButton?.text()).toContain('Sign in with Discord')
  })

  it('should have Battle.net OAuth button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const battlenetButton = wrapper.findAll('button').find((b) => b.text().includes('Battle.net'))
    expect(battlenetButton).toBeDefined()
    expect(battlenetButton?.text()).toContain('Sign in with Battle.net')
  })

  it('should display guild branding text', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Fair Loot Priority Score Dashboard')
  })

  it('should display helper text about FLPS', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.text()).toContain('Sign in to view your FLPS score and guild leaderboard')
  })

  it('should have Discord button with correct background color', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const discordButton = wrapper.findAll('button').find((b) => b.text().includes('Discord'))
    expect(discordButton?.classes()).toContain('bg-[#5865F2]')
  })

  it('should have Battle.net button with correct background color', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const battlenetButton = wrapper.findAll('button').find((b) => b.text().includes('Battle.net'))
    expect(battlenetButton?.classes()).toContain('bg-[#148EFF]')
  })
})
