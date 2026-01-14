import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ScoreCard from './ScoreCard.vue'

describe('ScoreCard', () => {
  const defaultProps = {
    score: 0.85,
    rank: 5,
    eligible: true,
    characterName: 'TestCharacter',
    characterClass: 'WARRIOR' as const,
  }

  it('should render character name', () => {
    const wrapper = mount(ScoreCard, { props: defaultProps })
    expect(wrapper.text()).toContain('TestCharacter')
  })

  it('should render character class', () => {
    const wrapper = mount(ScoreCard, { props: defaultProps })
    expect(wrapper.text()).toContain('WARRIOR')
  })

  it('should render score value', () => {
    const wrapper = mount(ScoreCard, { props: defaultProps })
    expect(wrapper.text()).toContain('0.850')
  })

  it('should render rank when provided', () => {
    const wrapper = mount(ScoreCard, { props: defaultProps })
    expect(wrapper.text()).toContain('#5')
  })

  it('should not render rank when not provided', () => {
    const wrapper = mount(ScoreCard, {
      props: { ...defaultProps, rank: undefined },
    })
    expect(wrapper.text()).not.toContain('#')
  })

  it('should show eligible status when eligible', () => {
    const wrapper = mount(ScoreCard, { props: defaultProps })
    expect(wrapper.text()).toContain('Eligible')
    expect(wrapper.find('.bg-green-900\\/50').exists()).toBe(true)
  })

  it('should show not eligible status when not eligible', () => {
    const wrapper = mount(ScoreCard, {
      props: { ...defaultProps, eligible: false },
    })
    expect(wrapper.text()).toContain('Not Eligible')
    expect(wrapper.find('.bg-red-900\\/50').exists()).toBe(true)
  })

  it('should apply high score color for scores >= 0.8', () => {
    const wrapper = mount(ScoreCard, {
      props: { ...defaultProps, score: 0.9 },
    })
    expect(wrapper.find('.text-score-high').exists()).toBe(true)
  })

  it('should apply medium score color for scores >= 0.5', () => {
    const wrapper = mount(ScoreCard, {
      props: { ...defaultProps, score: 0.6 },
    })
    expect(wrapper.find('.text-score-medium').exists()).toBe(true)
  })

  it('should apply low score color for scores < 0.5', () => {
    const wrapper = mount(ScoreCard, {
      props: { ...defaultProps, score: 0.3 },
    })
    expect(wrapper.find('.text-score-low').exists()).toBe(true)
  })
})
