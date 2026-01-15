import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StepTimeline from './StepTimeline.vue'
import type { PlanStep } from '@/api/raidplan'

describe('StepTimeline', () => {
  const createStep = (order: number, notes?: string): PlanStep => ({
    order,
    notes,
    markers: [],
    shapes: [],
  })

  const defaultProps = {
    steps: [createStep(0, 'Phase 1'), createStep(1, 'Phase 2'), createStep(2, 'Phase 3')],
    currentStep: 0,
  }

  describe('Rendering', () => {
    it('should render timeline container', () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      expect(wrapper.find('[data-testid="step-timeline"]').exists()).toBe(true)
    })

    it('should display step indicators for each step', () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      const indicators = wrapper.findAll('[data-testid="step-indicator"]')
      expect(indicators).toHaveLength(3)
    })

    it('should show step numbers', () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      expect(wrapper.text()).toContain('1')
      expect(wrapper.text()).toContain('2')
      expect(wrapper.text()).toContain('3')
    })
  })

  describe('Current Step', () => {
    it('should highlight current step', () => {
      const wrapper = mount(StepTimeline, {
        props: { ...defaultProps, currentStep: 1 },
      })

      const indicators = wrapper.findAll('[data-testid="step-indicator"]')
      expect(indicators[0].classes()).not.toContain('active')
      expect(indicators[1].classes()).toContain('active')
      expect(indicators[2].classes()).not.toContain('active')
    })

    it('should show current step notes', () => {
      const wrapper = mount(StepTimeline, {
        props: { ...defaultProps, currentStep: 0 },
      })

      expect(wrapper.text()).toContain('Phase 1')
    })
  })

  describe('Navigation', () => {
    it('should emit step-change when step indicator clicked', async () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      const indicators = wrapper.findAll('[data-testid="step-indicator"]')
      await indicators[2].trigger('click')

      expect(wrapper.emitted('step-change')).toBeTruthy()
      expect(wrapper.emitted('step-change')![0]).toEqual([2])
    })

    it('should show previous button', () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      expect(wrapper.find('[data-testid="prev-button"]').exists()).toBe(true)
    })

    it('should show next button', () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      expect(wrapper.find('[data-testid="next-button"]').exists()).toBe(true)
    })

    it('should navigate to previous step on prev click', async () => {
      const wrapper = mount(StepTimeline, {
        props: { ...defaultProps, currentStep: 1 },
      })

      await wrapper.find('[data-testid="prev-button"]').trigger('click')

      expect(wrapper.emitted('step-change')).toBeTruthy()
      expect(wrapper.emitted('step-change')![0]).toEqual([0])
    })

    it('should navigate to next step on next click', async () => {
      const wrapper = mount(StepTimeline, {
        props: { ...defaultProps, currentStep: 1 },
      })

      await wrapper.find('[data-testid="next-button"]').trigger('click')

      expect(wrapper.emitted('step-change')).toBeTruthy()
      expect(wrapper.emitted('step-change')![0]).toEqual([2])
    })

    it('should disable prev button on first step', () => {
      const wrapper = mount(StepTimeline, {
        props: { ...defaultProps, currentStep: 0 },
      })

      const prevButton = wrapper.find('[data-testid="prev-button"]')
      expect(prevButton.attributes('disabled')).toBeDefined()
    })

    it('should disable next button on last step', () => {
      const wrapper = mount(StepTimeline, {
        props: { ...defaultProps, currentStep: 2 },
      })

      const nextButton = wrapper.find('[data-testid="next-button"]')
      expect(nextButton.attributes('disabled')).toBeDefined()
    })
  })

  describe('Step Management', () => {
    it('should show add step button', () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      expect(wrapper.find('[data-testid="add-step-button"]').exists()).toBe(true)
    })

    it('should emit add-step on add click', async () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      await wrapper.find('[data-testid="add-step-button"]').trigger('click')

      expect(wrapper.emitted('add-step')).toBeTruthy()
    })

    it('should show delete step button', () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      expect(wrapper.find('[data-testid="delete-step-button"]').exists()).toBe(true)
    })

    it('should emit delete-step on delete click', async () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      await wrapper.find('[data-testid="delete-step-button"]').trigger('click')

      expect(wrapper.emitted('delete-step')).toBeTruthy()
      expect(wrapper.emitted('delete-step')![0]).toEqual([0])
    })

    it('should disable delete when only one step exists', () => {
      const wrapper = mount(StepTimeline, {
        props: { steps: [createStep(0)], currentStep: 0 },
      })

      const deleteButton = wrapper.find('[data-testid="delete-step-button"]')
      expect(deleteButton.attributes('disabled')).toBeDefined()
    })
  })

  describe('Empty State', () => {
    it('should handle empty steps array', () => {
      const wrapper = mount(StepTimeline, {
        props: { steps: [], currentStep: 0 },
      })

      const indicators = wrapper.findAll('[data-testid="step-indicator"]')
      expect(indicators).toHaveLength(0)
    })
  })

  describe('Step Notes Display', () => {
    it('should display step notes preview', () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      expect(wrapper.find('[data-testid="step-notes"]').exists()).toBe(true)
    })

    it('should emit notes-edit when notes clicked', async () => {
      const wrapper = mount(StepTimeline, {
        props: defaultProps,
      })

      await wrapper.find('[data-testid="step-notes"]').trigger('click')

      expect(wrapper.emitted('notes-edit')).toBeTruthy()
    })

    it('should show placeholder when no notes', () => {
      const wrapper = mount(StepTimeline, {
        props: { steps: [createStep(0)], currentStep: 0 },
      })

      expect(wrapper.text()).toContain('Click to add notes')
    })
  })
})
