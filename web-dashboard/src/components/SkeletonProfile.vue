<script setup lang="ts">
/**
 * SkeletonProfile - Profile/avatar loading skeleton.
 *
 * Displays a skeleton loader that mimics a user profile card.
 */
import Skeleton from './Skeleton.vue'

export interface SkeletonProfileProps {
  showAvatar?: boolean
  avatarSize?: string
  showStats?: boolean
  statsCount?: number
}

withDefaults(defineProps<SkeletonProfileProps>(), {
  showAvatar: true,
  avatarSize: '4rem',
  showStats: true,
  statsCount: 3,
})
</script>

<template>
  <div class="bg-gray-800/50 backdrop-blur-sm rounded-xl border border-gray-700/50 p-6">
    <div class="flex items-start gap-4">
      <!-- Avatar -->
      <Skeleton
        v-if="showAvatar"
        shape="circle"
        :height="avatarSize"
      />

      <!-- Profile info -->
      <div class="flex-1">
        <!-- Name -->
        <Skeleton height="1.5rem" width="50%" class="mb-2" />
        <!-- Subtitle -->
        <Skeleton height="1rem" width="30%" class="mb-4" />

        <!-- Stats row -->
        <div v-if="showStats" class="flex gap-6 mt-4">
          <div v-for="i in statsCount" :key="i" class="text-center">
            <Skeleton height="1.5rem" width="3rem" class="mx-auto mb-1" />
            <Skeleton height="0.75rem" width="4rem" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
