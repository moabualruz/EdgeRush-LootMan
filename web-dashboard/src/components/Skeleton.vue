<script setup lang="ts">
/**
 * Skeleton - Base loading skeleton component.
 *
 * A versatile skeleton loader that can be used for various UI elements.
 * Supports different shapes (rectangle, circle, text) and customizable dimensions.
 */
export interface SkeletonProps {
  width?: string
  height?: string
  shape?: 'rectangle' | 'circle' | 'text'
  animated?: boolean
}

withDefaults(defineProps<SkeletonProps>(), {
  width: '100%',
  height: '1rem',
  shape: 'rectangle',
  animated: true,
})
</script>

<template>
  <div
    class="skeleton"
    :class="[
      shape === 'circle' ? 'rounded-full' : shape === 'text' ? 'rounded' : 'rounded-md',
      animated ? 'skeleton-animated' : ''
    ]"
    :style="{
      width: shape === 'circle' ? height : width,
      height: height,
    }"
  />
</template>

<style scoped>
.skeleton {
  background: linear-gradient(
    90deg,
    rgb(55 65 81 / 0.4) 0%,
    rgb(75 85 99 / 0.6) 50%,
    rgb(55 65 81 / 0.4) 100%
  );
  background-size: 200% 100%;
}

.skeleton-animated {
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

@keyframes skeleton-pulse {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
