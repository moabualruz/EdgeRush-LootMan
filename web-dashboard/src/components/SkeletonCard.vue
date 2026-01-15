<script setup lang="ts">
/**
 * SkeletonCard - Card-style loading skeleton.
 *
 * Mimics the ScoreCard component's loading state.
 */
import Skeleton from './Skeleton.vue'

export interface SkeletonCardProps {
  showHeader?: boolean
  headerHeight?: string
  lines?: number
  lineHeight?: string
}

withDefaults(defineProps<SkeletonCardProps>(), {
  showHeader: true,
  headerHeight: '1.5rem',
  lines: 3,
  lineHeight: '1rem',
})
</script>

<template>
  <div class="bg-gray-800/50 backdrop-blur-sm rounded-xl border border-gray-700/50 p-6">
    <!-- Header -->
    <Skeleton
      v-if="showHeader"
      :height="headerHeight"
      width="60%"
      class="mb-4"
    />

    <!-- Content lines -->
    <div class="space-y-3">
      <Skeleton
        v-for="i in lines"
        :key="i"
        :height="lineHeight"
        :width="i === lines ? '80%' : '100%'"
      />
    </div>
  </div>
</template>
