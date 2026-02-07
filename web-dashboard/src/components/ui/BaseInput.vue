<script setup lang="ts">
defineProps<{
  modelValue: string | number
  label?: string
  id?: string
  type?: string
  placeholder?: string
  error?: string
  required?: boolean
  disabled?: boolean
}>()

defineEmits<{
  (e: 'update:modelValue', value: string | number): void
}>()
</script>

<template>
  <div class="group">
    <label v-if="label" :for="id" class="block text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-1.5 ml-1">
      {{ label }}
      <span v-if="required" class="text-destructive">*</span>
    </label>
    <div class="relative">
      <input
        :id="id"
        :type="type || 'text'"
        :value="modelValue"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        :placeholder="placeholder"
        :disabled="disabled"
        class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white placeholder-muted-foreground/50 focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        :class="{ 'border-destructive/50 ring-destructive/20': error }"
      />
      <div v-if="$slots.append" class="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none">
        <slot name="append"></slot>
      </div>
    </div>
    <p v-if="error" class="mt-1 text-xs text-destructive">{{ error }}</p>
  </div>
</template>
