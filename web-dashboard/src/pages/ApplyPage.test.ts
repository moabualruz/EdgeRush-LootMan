import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query'
import { ref, readonly } from 'vue'
import ApplyPage from './ApplyPage.vue'

// Mock the API module
vi.mock('@/api/applications', () => ({
  applicationsApi: {
    submitApplication: vi.fn(),
    getMyApplication: vi.fn(),
  },
}))

// Mock the recruitment API module
vi.mock('@/api/recruitment', () => ({
  recruitmentApi: {
    lookupCharacterFull: vi.fn(),
    submitApplication: vi.fn(),
  },
}))

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
  }),
  useRoute: () => ({
    query: {},
  }),
}))

// Mock the toast composable
vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
  }),
}))

// Mock the character lookup composable
const mockCharacterData = ref(null)
const mockIsLoading = ref(false)
const mockError = ref(null)
const mockHasSearched = ref(false)

vi.mock('@/composables/useCharacterLookup', () => ({
  useCharacterLookup: () => ({
    lookupCharacter: vi.fn().mockResolvedValue(null),
    debouncedLookup: vi.fn(),
    characterData: readonly(mockCharacterData),
    isLoading: readonly(mockIsLoading),
    error: readonly(mockError),
    hasSearched: readonly(mockHasSearched),
    hasRaiderIOData: ref(false),
    hasWarcraftLogsData: ref(false),
    reset: vi.fn(),
  }),
}))

// Mock chart components
vi.mock('@/components/charts', () => ({
  ProgressBar: {
    name: 'ProgressBar',
    template: '<div class="mock-progress-bar"></div>',
    props: ['value', 'max', 'color', 'height', 'showLabel'],
  },
}))

describe('ApplyPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Reset mock refs
    mockCharacterData.value = null
    mockIsLoading.value = false
    mockError.value = null
    mockHasSearched.value = false
  })

  const mountComponent = () => {
    const queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    })

    return mount(ApplyPage, {
      global: {
        plugins: [[VueQueryPlugin, { queryClient }]],
        stubs: {
          teleport: true,
          SkeletonCard: true,
        },
      },
    })
  }

  it('should render the application form header', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('Apply to EdgeRush')
  })

  it('should display step indicators', () => {
    const wrapper = mountComponent()
    // Should have multiple steps visible
    expect(wrapper.text()).toContain('About You')
    expect(wrapper.text()).toContain('Character')
  })

  it('should start on the first step', () => {
    const wrapper = mountComponent()
    // First step should be "About You" section
    expect(wrapper.text()).toContain('Name')
    expect(wrapper.text()).toContain('Age')
    expect(wrapper.text()).toContain('Timezone')
  })

  it('should have a next button', () => {
    const wrapper = mountComponent()
    const nextButton = wrapper.find('button[type="button"]')
    expect(nextButton.exists()).toBe(true)
    expect(wrapper.text()).toContain('Next')
  })

  it('should have form validation for required fields', () => {
    const wrapper = mountComponent()
    // Name field should be required
    const nameInput = wrapper.find('input[name="name"]')
    expect(nameInput.exists()).toBe(true)
  })

  it('should show current step number', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toMatch(/Step\s*1/)
  })

  it('should display availability checkboxes', () => {
    const wrapper = mountComponent()
    // Should have raid day checkboxes
    expect(wrapper.text()).toContain('Raid Availability')
  })

  it('should have character selection step', async () => {
    const wrapper = mountComponent()

    // Fill required fields
    await wrapper.find('input[name="name"]').setValue('TestPlayer')
    await wrapper.find('input[name="age"]').setValue('25')

    // The character step should be in the step list
    expect(wrapper.text()).toContain('Character')
  })

  it('should show all step names in indicators', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('About You')
    expect(wrapper.text()).toContain('Character')
    expect(wrapper.text()).toContain('Guild History')
    expect(wrapper.text()).toContain('Motivation')
    expect(wrapper.text()).toContain('Review')
  })

  it('should have raid day toggle buttons', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('Wednesday')
    expect(wrapper.text()).toContain('Sunday')
    expect(wrapper.text()).toContain('Monday')
  })

  it('should toggle raid day availability when clicking', async () => {
    const wrapper = mountComponent()
    const wednesdayButton = wrapper
      .findAll('button')
      .find((b) => b.text() === 'Wednesday')

    expect(wednesdayButton).toBeDefined()
    // Initially should not be selected (gray background)
    expect(wednesdayButton?.classes()).toContain('bg-gray-700')

    // Click to select
    await wednesdayButton?.trigger('click')

    // Now should be selected (primary background)
    expect(wednesdayButton?.classes()).toContain('bg-primary-600')
  })

  it('should have timezone selection', () => {
    const wrapper = mountComponent()
    const timezoneSelect = wrapper.find('select')
    expect(timezoneSelect.exists()).toBe(true)
    expect(wrapper.text()).toContain('Europe - CET')
  })

  it('should show info card about next steps', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('What happens next?')
    expect(wrapper.text()).toContain('officers will review')
  })

  it('should have fetch character data button on step 2', () => {
    const wrapper = mountComponent()
    // The button text should be present (even on step 1, it's in the DOM but hidden)
    expect(wrapper.text()).toContain('Fetch Character Data')
  })

  it('should show character data instructions when no data fetched', () => {
    const wrapper = mountComponent()
    expect(wrapper.text()).toContain('Enter your character name and realm')
    expect(wrapper.text()).toContain('Raider.IO score')
    expect(wrapper.text()).toContain('Warcraft Logs')
  })
})
