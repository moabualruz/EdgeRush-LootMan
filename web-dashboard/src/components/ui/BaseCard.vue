<script setup lang="ts">
defineProps<{
  title?: string
  loading?: boolean
}>()
</script>

<template>
  <div class="glass-card p-6 border-white/10 flex flex-col bg-gradient-to-br from-black/40 to-black/20">
    <div v-if="title || $slots.header" class="flex items-center justify-between mb-6">
      <h2 v-if="title" class="text-lg font-semibold text-white flex items-center gap-2">
        <slot name="icon"></slot>
        {{ title }}
      </h2>
      <slot name="header"></slot>
    </div>
    
    <div v-if="loading" class="flex justify-center items-center py-12">
      <div class="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
    </div>
    
    <slot v-else></slot>
  </div>
</template>
