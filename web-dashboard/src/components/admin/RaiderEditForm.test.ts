import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import RaiderEditForm from './RaiderEditForm.vue'
import { adminApi } from '@/api/admin'

// Mock admin API
vi.mock('@/api/admin', () => ({
  adminApi: {
    updateRaider: vi.fn(),
  },
}))

// Mock TanStack Query
vi.mock('@tanstack/vue-query', () => ({
  useMutation: vi.fn(({ mutationFn, onSuccess, onError }) => ({
    mutate: vi.fn(async (data) => {
      try {
        const result = await mutationFn(data)
        onSuccess?.(result)
        return result
      } catch (e) {
        onError?.(e)
        throw e
      }
    }),
    mutateAsync: vi.fn(async (data) => {
      try {
        const result = await mutationFn(data)
        onSuccess?.(result)
        return result
      } catch (e) {
        onError?.(e)
        throw e
      }
    }),
    isPending: ref(false),
    isError: ref(false),
    error: ref(null),
  })),
  useQueryClient: vi.fn(() => ({
    invalidateQueries: vi.fn(),
  })),
}))

// Mock useToast
vi.mock('@/composables/useToast', () => ({
  useToast: vi.fn(() => ({
    success: vi.fn(),
    error: vi.fn(),
  })),
}))

describe('RaiderEditForm', () => {
  const defaultProps = {
    guildId: 'test-guild',
    raiderId: 123,
    characterName: 'TestWarrior',
    currentRank: 'Raider',
    isActive: true,
  }

  const mountComponent = (props = defaultProps) => {
    return mount(RaiderEditForm, {
      props,
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render form with current values', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-testid="rank-select"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="status-toggle"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('TestWarrior')
  })

  it('should display current rank in select', () => {
    const wrapper = mountComponent()

    const select = wrapper.find('[data-testid="rank-select"]')
    expect((select.element as HTMLSelectElement).value).toBe('Raider')
  })

  it('should display status toggle with current state', () => {
    const wrapper = mountComponent()

    const toggle = wrapper.find('[data-testid="status-toggle"]')
    expect((toggle.element as HTMLInputElement).checked).toBe(true)
  })

  it('should call updateRaider API when form is submitted', async () => {
    vi.mocked(adminApi.updateRaider).mockResolvedValue({
      raiderId: 123,
      rank: 'Officer',
      isActive: true,
    })

    const wrapper = mountComponent()

    // Change rank
    await wrapper.find('[data-testid="rank-select"]').setValue('Officer')

    // Submit form
    await wrapper.find('form').trigger('submit.prevent')

    expect(adminApi.updateRaider).toHaveBeenCalledWith('test-guild', 123, {
      rank: 'Officer',
      isActive: true,
    })
  })

  it('should emit close event when cancel is clicked', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[data-testid="cancel-button"]').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('should emit close event after successful save', async () => {
    vi.mocked(adminApi.updateRaider).mockResolvedValue({
      raiderId: 123,
      rank: 'Officer',
      isActive: true,
    })

    const wrapper = mountComponent()
    await wrapper.find('form').trigger('submit.prevent')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('should show available ranks in dropdown', () => {
    const wrapper = mountComponent()

    const options = wrapper.findAll('[data-testid="rank-select"] option')
    expect(options.length).toBeGreaterThan(0)
    expect(wrapper.text()).toContain('Raider')
    expect(wrapper.text()).toContain('Trial')
    expect(wrapper.text()).toContain('Officer')
  })
})
