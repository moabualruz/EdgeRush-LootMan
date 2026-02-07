<script setup lang="ts">
defineProps<{
  modelValue: string | number
  label?: string
  id?: string
  options?: { value: string | number; label: string }[]
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
      <select
        :id="id"
        :value="modelValue"
        @input="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
        :disabled="disabled"
        class="w-full px-4 py-3 bg-black/20 border border-white/10 rounded-lg text-white placeholder-muted-foreground/50 focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/50 transition-all disabled:opacity-50 disabled:cursor-not-allowed appearance-none"
        :class="{ 'border-destructive/50 ring-destructive/20': error, 'text-muted-foreground': !modelValue }"
      >
        <option v-if="placeholder" value="" disabled selected>{{ placeholder }}</option>
        <slot>
          <option v-for="option in options" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </slot>
      </select>
      <div class="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-chevron-down"><path d="m6 9 6 6 6-6"/></svg>
      </div>
    </div>
    <p v-if="error" class="mt-1 text-xs text-destructive">{{ error }}</p>
  </div>
</template>
