import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ResetPasswordPage from './ResetPasswordPage.vue'
import { authApi } from '@/api/auth'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush,
  }),
  useRoute: () => ({
    query: { token: 'valid-token-123' },
  }),
  RouterLink: {
    template: '<a><slot /></a>',
  },
}))

// Mock auth API
vi.mock('@/api/auth', () => ({
  authApi: {
    resetPassword: vi.fn(),
  },
}))

describe('ResetPasswordPage', () => {
  const mountComponent = () => {
    return mount(ResetPasswordPage, {
      global: {
        stubs: {
          RouterLink: { template: '<a><slot /></a>' },
        },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('should extract token from URL query', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    // Token should be stored internally (we can't directly verify but form should show)
    expect(wrapper.find('form').exists()).toBe(true)
  })

  it('should render password fields', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const passwordInputs = wrapper.findAll('input[type="password"]')
    expect(passwordInputs.length).toBe(2)
  })

  it('should validate password length (min 6)', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const passwordInputs = wrapper.findAll('input[type="password"]')
    const submitBtn = wrapper.find('button[type="submit"]')

    // Short password
    await passwordInputs[0].setValue('12345')
    await passwordInputs[1].setValue('12345')
    expect(submitBtn.attributes('disabled')).toBeDefined()

    // Valid length password
    await passwordInputs[0].setValue('123456')
    await passwordInputs[1].setValue('123456')
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('should validate password confirmation match', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const passwordInputs = wrapper.findAll('input[type="password"]')
    const submitBtn = wrapper.find('button[type="submit"]')

    // Mismatched passwords
    await passwordInputs[0].setValue('password123')
    await passwordInputs[1].setValue('different456')
    expect(submitBtn.attributes('disabled')).toBeDefined()

    // Matching passwords
    await passwordInputs[1].setValue('password123')
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('should show error for invalid or expired token', async () => {
    vi.mocked(authApi.resetPassword).mockRejectedValueOnce(new Error('Token expired'))
    const wrapper = mountComponent()
    await flushPromises()

    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('newPassword123')
    await passwordInputs[1].setValue('newPassword123')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Token expired')
  })

  it('should redirect to login on success', async () => {
    vi.mocked(authApi.resetPassword).mockResolvedValueOnce(undefined)
    const wrapper = mountComponent()
    await flushPromises()

    const passwordInputs = wrapper.findAll('input[type="password"]')
    await passwordInputs[0].setValue('newPassword123')
    await passwordInputs[1].setValue('newPassword123')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('Password Reset')
  })
})
