<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <div class="mb-8 animate-fade-in-up">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold tracking-tight text-white mb-2 text-glow">
          {{ title }}
        </h1>
        <p v-if="subtitle" class="text-muted-foreground">
          {{ subtitle }}
        </p>
      </div>
      <slot name="actions"></slot>
    </div>
    <slot name="meta"></slot>
  </div>
</template>
