import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ToastContainer from './ToastContainer.vue'
import { useToast } from '@/composables/useToast'

describe('ToastContainer', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    const { dismissAll } = useToast()
    dismissAll()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('should render no toasts initially', () => {
    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    expect(wrapper.findAll('[role="alert"]')).toHaveLength(0)
  })

  it('should render success toast', async () => {
    const { success } = useToast()
    success('Success Title', 'Success message')

    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Success Title')
    expect(wrapper.text()).toContain('Success message')
    expect(wrapper.find('.bg-green-900\\/90').exists()).toBe(true)
  })

  it('should render error toast', async () => {
    const { error } = useToast()
    error('Error Title')

    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Error Title')
    expect(wrapper.find('.bg-red-900\\/90').exists()).toBe(true)
  })

  it('should render warning toast', async () => {
    const { warning } = useToast()
    warning('Warning Title')

    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Warning Title')
    expect(wrapper.find('.bg-yellow-900\\/90').exists()).toBe(true)
  })

  it('should render info toast', async () => {
    const { info } = useToast()
    info('Info Title')

    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('Info Title')
    expect(wrapper.find('.bg-blue-900\\/90').exists()).toBe(true)
  })

  it('should show dismiss button for dismissible toasts', async () => {
    const { success } = useToast()
    success('Test')

    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await wrapper.vm.$nextTick()

    const dismissButton = wrapper.find('button[aria-label="Dismiss notification"]')
    expect(dismissButton.exists()).toBe(true)
  })

  it('should dismiss toast when dismiss button is clicked', async () => {
    const { success, toasts } = useToast()
    success('Test')

    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await wrapper.vm.$nextTick()
    expect(toasts.value).toHaveLength(1)

    const dismissButton = wrapper.find('button[aria-label="Dismiss notification"]')
    await dismissButton.trigger('click')

    expect(toasts.value).toHaveLength(0)
  })

  it('should render multiple toasts', async () => {
    const { success, error, warning } = useToast()
    success('Success')
    error('Error')
    warning('Warning')

    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await wrapper.vm.$nextTick()

    const alerts = wrapper.findAll('[role="alert"]')
    expect(alerts).toHaveLength(3)
  })

  it('should have correct icons for each type', async () => {
    const { success, error, warning, info } = useToast()
    success('Success')

    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    await wrapper.vm.$nextTick()

    // Success should have checkmark icon
    expect(wrapper.find('.text-green-400').text()).toBe('✓')
  })

  it('should have ARIA attributes for accessibility', () => {
    const wrapper = mount(ToastContainer, {
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })

    const container = wrapper.find('[aria-live="polite"]')
    expect(container.exists()).toBe(true)
    expect(container.attributes('aria-label')).toBe('Notifications')
  })
})
