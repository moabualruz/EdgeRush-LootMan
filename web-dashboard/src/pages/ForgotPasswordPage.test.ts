import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ForgotPasswordPage from './ForgotPasswordPage.vue'
import { authApi } from '@/api/auth'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
  RouterLink: {
    template: '<a><slot /></a>',
  },
}))

// Mock auth API
vi.mock('@/api/auth', () => ({
  authApi: {
    forgotPassword: vi.fn(),
  },
}))

describe('ForgotPasswordPage', () => {
  const mountComponent = () => {
    return mount(ForgotPasswordPage, {
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

  it('should render email input and submit button', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Reset Password')
  })

  it('should validate email format before enabling submit', async () => {
    const wrapper = mountComponent()
    await flushPromises()

    const submitBtn = wrapper.find('button[type="submit"]')
    
    // Empty email - disabled
    expect(submitBtn.attributes('disabled')).toBeDefined()

    // Invalid email - still disabled
    await wrapper.find('input[type="email"]').setValue('notanemail')
    expect(submitBtn.attributes('disabled')).toBeDefined()

    // Valid email - enabled
    await wrapper.find('input[type="email"]').setValue('user@example.com')
    expect(submitBtn.attributes('disabled')).toBeUndefined()
  })

  it('should show loading state during submission', async () => {
    vi.mocked(authApi.forgotPassword).mockImplementation(() => new Promise(() => {})) // Never resolves
    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.find('input[type="email"]').setValue('user@example.com')
    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('Sending')
  })

  it('should show success message after submission', async () => {
    vi.mocked(authApi.forgotPassword).mockResolvedValueOnce(undefined)
    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.find('input[type="email"]').setValue('user@example.com')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('check your email')
  })

  it('should show error message on failure', async () => {
    vi.mocked(authApi.forgotPassword).mockRejectedValueOnce(new Error('User not found'))
    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.find('input[type="email"]').setValue('unknown@example.com')
    await wrapper.find('form').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('User not found')
  })
})
