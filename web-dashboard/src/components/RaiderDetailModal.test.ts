import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RaiderDetailModal from './RaiderDetailModal.vue'
import type { FlpsScore } from '@/types'

describe('RaiderDetailModal', () => {
  const mockRaider: FlpsScore = {
    raiderId: 1,
    characterName: 'TestWarrior',
    characterClass: 'WARRIOR',
    role: 'TANK',
    flps: 0.875,
    rms: {
      value: 0.85,
      acs: 92,
      mas: 78,
      eps: 85,
    },
    ipi: {
      value: 0.9,
      uv: 15.5,
      tierBonus: 0.1,
      roleMultiplier: 1.0,
    },
    rdf: 0.95,
    eligible: true,
  }

  const mockIneligibleRaider: FlpsScore = {
    ...mockRaider,
    raiderId: 2,
    characterName: 'IneligibleMage',
    characterClass: 'MAGE',
    role: 'DPS',
    eligible: false,
    ineligibilityReasons: ['Attendance below 60%', 'Recent inactivity'],
  }

  const mountComponent = (props: { isOpen: boolean; raider: FlpsScore | null }) => {
    return mount(RaiderDetailModal, {
      props,
      global: {
        stubs: {
          Teleport: true,
        },
      },
    })
  }

  it('should not render when isOpen is false', () => {
    const wrapper = mountComponent({ isOpen: false, raider: mockRaider })
    
    expect(wrapper.find('[data-testid="raider-detail-modal"]').exists()).toBe(false)
  })

  it('should render modal when isOpen is true with raider data', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    expect(wrapper.find('[data-testid="raider-detail-modal"]').exists()).toBe(true)
  })

  it('should display character name in header', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    expect(wrapper.text()).toContain('TestWarrior')
  })

  it('should display role badge', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    expect(wrapper.text()).toContain('TANK')
  })

  it('should display FLPS score prominently', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    // Score should be formatted
    expect(wrapper.text()).toContain('0.875')
  })

  it('should display RMS breakdown values', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    // ACS, MAS, EPS values
    expect(wrapper.text()).toContain('92')
    expect(wrapper.text()).toContain('78')
    expect(wrapper.text()).toContain('85')
  })

  it('should display IPI breakdown values', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    // UV value
    expect(wrapper.text()).toContain('15.5')
  })

  it('should display RDF value', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    expect(wrapper.text()).toContain('0.95')
  })

  it('should show eligible status for eligible raiders', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    expect(wrapper.text()).toContain('Eligible')
    // Should show checkmark
    expect(wrapper.find('.text-green-400, .text-green-500').exists()).toBe(true)
  })

  it('should show ineligible status with reasons', () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockIneligibleRaider })
    
    expect(wrapper.text()).toContain('Ineligible')
    expect(wrapper.text()).toContain('Attendance below 60%')
    expect(wrapper.text()).toContain('Recent inactivity')
  })

  it('should emit close event when clicking close button', async () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    await wrapper.find('[data-testid="close-button"]').trigger('click')
    
    expect(wrapper.emitted('close')).toBeTruthy()
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('should emit close event when clicking backdrop', async () => {
    const wrapper = mountComponent({ isOpen: true, raider: mockRaider })
    
    await wrapper.find('[data-testid="modal-backdrop"]').trigger('click')
    
    expect(wrapper.emitted('close')).toBeTruthy()
  })
})
