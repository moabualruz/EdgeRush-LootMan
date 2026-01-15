<script setup lang="ts">
/**
 * SkeletonTable - Table loading skeleton.
 *
 * Displays a skeleton loader that mimics a data table.
 */
import Skeleton from './Skeleton.vue'

export interface SkeletonTableProps {
  rows?: number
  columns?: number
  showHeader?: boolean
}

withDefaults(defineProps<SkeletonTableProps>(), {
  rows: 5,
  columns: 4,
  showHeader: true,
})
</script>

<template>
  <div class="bg-gray-800/50 backdrop-blur-sm rounded-xl border border-gray-700/50 overflow-hidden">
    <!-- Header row -->
    <div v-if="showHeader" class="bg-gray-900/50 px-4 py-3 border-b border-gray-700/50">
      <div class="flex gap-4">
        <Skeleton
          v-for="i in columns"
          :key="`header-${i}`"
          height="1.25rem"
          :width="i === 1 ? '30%' : '20%'"
        />
      </div>
    </div>

    <!-- Body rows -->
    <div class="divide-y divide-gray-700/30">
      <div
        v-for="row in rows"
        :key="`row-${row}`"
        class="px-4 py-3"
      >
        <div class="flex gap-4 items-center">
          <Skeleton
            v-for="col in columns"
            :key="`cell-${row}-${col}`"
            height="1rem"
            :width="col === 1 ? '25%' : '15%'"
          />
        </div>
      </div>
    </div>
  </div>
</template>
