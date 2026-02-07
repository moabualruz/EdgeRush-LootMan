<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  variant?: 'primary' | 'secondary' | 'ghost' | 'destructive'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  block?: boolean
  to?: string
}>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  block: false
})

const classes = computed(() => {
  const base = 'inline-flex items-center justify-center rounded-lg font-semibold transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed transform active:scale-95'
  
  const variants = {
    primary: 'bg-primary hover:bg-primary-600 text-primary-foreground shadow-lg shadow-primary/25 hover:shadow-primary/40 hover:-translate-y-0.5',
    secondary: 'bg-[#148EFF]/10 hover:bg-[#148EFF]/20 border border-[#148EFF]/20 text-[#148EFF] hover:text-white',
    ghost: 'bg-transparent hover:bg-white/5 text-muted-foreground hover:text-white',
    destructive: 'bg-destructive hover:bg-destructive/90 text-destructive-foreground shadow-lg shadow-destructive/25'
  }

  const sizes = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base'
  }

  return [
    base,
    variants[props.variant],
    sizes[props.size],
    props.block ? 'w-full' : '',
    props.loading ? 'cursor-wait' : ''
  ].join(' ')
})
</script>

<template>
  <component
    :is="to ? 'router-link' : 'button'"
    :to="to"
    :class="classes"
    :disabled="disabled || loading"
    v-bind="$attrs"
  >
    <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
    <slot name="icon-left" v-else></slot>
    <slot></slot>
    <slot name="icon-right"></slot>
  </component>
</template>
